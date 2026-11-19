package com.zeroday.cyberprotocol.core.level

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.json.JSONObject

class TrainingLevelCatalogTest {
    @Test
    fun catalogContainsTheFirstTwoCampaignLevelsInOrder() {
        assertEquals(listOf("tutorial_01", "tutorial_02"), TrainingLevelCatalog.all.map { it.id })
        assertEquals(listOf(1, 2), TrainingLevelCatalog.all.map { it.sequence })
    }

    @Test
    fun everyCatalogLevelPassesStructuralValidation() {
        assertTrue(TrainingLevelCatalog.validateAll().toString(), TrainingLevelCatalog.validateAll().isEmpty())
    }

    @Test
    fun tutorialOneDescribesItsMapObjectsAndRobotApi() {
        val level = TrainingLevelCatalog.tutorial01

        assertEquals(GridPosition(0, 2), level.robotStart)
        assertEquals(5, level.width)
        assertEquals(5, level.height)
        assertEquals(setOf(RobotApi.MOVE, RobotApi.ACTION), level.robotApi)
        assertEquals(GridPosition(2, 2), level.objects.first { it.id == "energy_cell" }.position)
        assertEquals(GridPosition(4, 2), level.objects.first { it.id == "exit" }.position)
        assertTrue(level.sensors.isEmpty())
    }

    @Test
    fun tutorialTwoDescribesBothSensorScenariosAndTheirSafePads() {
        val level = TrainingLevelCatalog.tutorial02
        val blocked = level.scenarios.first { it.id == "blocked" }
        val clear = level.scenarios.first { it.id == "clear" }

        assertEquals(18, blocked.sensorReadings["range_sensor"])
        assertEquals(42, clear.sensorReadings["range_sensor"])
        assertEquals(setOf(GridPosition(1, 2)), blocked.obstaclePositions)
        assertTrue(clear.obstaclePositions.isEmpty())
        assertEquals(GridPosition(0, 3), level.objects.first { it.id == "blocked_checkpoint" }.position)
        assertEquals(GridPosition(1, 2), level.objects.first { it.id == "clear_checkpoint" }.position)
        assertTrue(level.sensors.single().requiresScanBeforeMovement)
        assertTrue(RobotApi.SCAN_DISTANCE in level.robotApi)
    }

    @Test
    fun malformedMapValidationDoesNotCrashWhenStartRowIsShort() {
        val invalid = TrainingLevelCatalog.tutorial01.copy(
            map = listOf(".....", "...."),
            robotStart = GridPosition(4, 1)
        )

        val errors = invalid.validationErrors()
        assertTrue(errors.any { it.contains("equal width") })
    }

    @Test
    fun sensorScenarioReadingsMustStayWithinSensorRange() {
        val invalid = TrainingLevelCatalog.tutorial02.copy(
            scenarios = listOf(
                SensorScenarioDefinition(
                    id = "out_of_range",
                    sensorReadings = mapOf("range_sensor" to 1001)
                )
            ),
            objects = emptyList()
        )

        assertTrue(invalid.validationErrors().any { it.contains("outside sensor") })
    }

    @Test
    fun tutorialOneSerializesResourceAndInteractionRulesForPython() {
        val json = JSONObject(TrainingLevelCatalog.tutorial01.toRunnerJson())
        val battery = json.getJSONObject("resources").getJSONObject("battery")
        val energyCell = json.getJSONArray("objects").getJSONObject(0)
        val exit = json.getJSONArray("objects").getJSONObject(1)

        assertEquals(32, battery.getInt("initial"))
        assertEquals(1, battery.getInt("move_cost"))
        assertEquals("collect_energy", energyCell.getString("completes_objective"))
        assertEquals(20, energyCell.getJSONObject("resource_changes").getInt("battery"))
        assertEquals("collect_energy", exit.getJSONArray("required_objectives").getString(0))
    }

    @Test
    fun tutorialTwoSerializesSensorScenarioAndCheckpointRulesForPython() {
        val json = JSONObject(TrainingLevelCatalog.tutorial02.toRunnerJson())
        val sensor = json.getJSONArray("sensors").getJSONObject(0)
        val blockedScenario = json.getJSONArray("scenarios").getJSONObject(0)
        val blockedCheckpoint = json.getJSONArray("objects").getJSONObject(0)

        assertEquals("range_sensor", sensor.getString("id"))
        assertEquals("read_sensor", sensor.getString("completes_objective"))
        assertEquals(18, blockedScenario.getJSONObject("sensor_readings").getInt("range_sensor"))
        assertEquals(true, blockedCheckpoint.getBoolean("requires_sensor_scan"))
        assertEquals("open_checkpoint", blockedCheckpoint.getString("completes_objective"))
    }

    @Test
    fun lookupReturnsNullForUnknownLevel() {
        assertNotNull(TrainingLevelCatalog.find("tutorial_01"))
        assertNull(TrainingLevelCatalog.find("tutorial_99"))
    }

    @Test
    fun validationRejectsUnevenMapsAndObjectsOutsideTheMap() {
        val invalid = TrainingLevelCatalog.tutorial01.copy(
            map = listOf(".....", "...."),
            objects = listOf(
                LevelObjectDefinition("outside", LevelObjectType.EXIT, GridPosition(8, 8))
            )
        )

        val errors = invalid.validationErrors()
        assertFalse(errors.isEmpty())
        assertTrue(errors.any { it.contains("equal width") })
        assertTrue(errors.any { it.contains("outside the map") })
    }
}
