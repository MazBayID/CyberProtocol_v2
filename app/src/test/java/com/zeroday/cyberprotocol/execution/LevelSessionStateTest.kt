package com.zeroday.cyberprotocol.execution

import com.zeroday.cyberprotocol.core.level.TrainingLevelCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.json.JSONObject

class LevelSessionStateTest {
    @Test
    fun sessionStateRoundTripsResourcesObjectivesAndSensorScenario() {
        val level = TrainingLevelCatalog.tutorial02
        val original = LevelSessionState(
            robotX = 1,
            robotY = 3,
            resources = mapOf("battery" to 57),
            completedObjectiveIds = setOf("read_sensor"),
            sensorScanned = true,
            sensorReadings = mapOf("range_sensor" to 42),
            scenarioId = "clear",
            completed = false
        )

        val restored = LevelSessionState.fromJson(JSONObject(original.toJson()), level)

        assertEquals(original, restored)
    }

    @Test
    fun sessionStateClampsRobotPositionToLevelBounds() {
        val level = TrainingLevelCatalog.tutorial01
        val json = JSONObject()
            .put("robot_x", 99)
            .put("robot_y", -5)
            .put("resources", JSONObject())
            .put("completed_objectives", org.json.JSONArray())
            .put("sensor_readings", JSONObject())

        val restored = LevelSessionState.fromJson(json, level)

        assertEquals(4, restored.robotX)
        assertEquals(0, restored.robotY)
    }

    @Test
    fun tutorialCatalogDefinitionsRemainValidForSharedExecution() {
        assertTrue(TrainingLevelCatalog.validateAll().toString(), TrainingLevelCatalog.validateAll().isEmpty())
    }
}
