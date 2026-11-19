package com.zeroday.cyberprotocol.core.level

/** Central catalog for the first campaign levels. */
object TrainingLevelCatalog {
    private val introductoryPythonFeatures = setOf(
        PythonFeature.VARIABLES,
        PythonFeature.PRINT,
        PythonFeature.CONDITIONALS,
        PythonFeature.FOR_LOOPS,
        PythonFeature.FUNCTIONS,
        PythonFeature.LISTS,
        PythonFeature.TUPLES
    )

    val tutorial01 = LevelDefinition(
        id = "tutorial_01",
        sequence = 1,
        titleId = "Dasar Kendali Robot",
        titleEn = "Robot Control Basics",
        map = List(5) { "....." },
        robotStart = GridPosition(0, 2),
        objects = listOf(
            LevelObjectDefinition(
                id = "energy_cell",
                type = LevelObjectType.ENERGY_CELL,
                position = GridPosition(2, 2),
                completesObjectiveId = "collect_energy",
                timelineEvent = "collect_energy",
                eventMessage = "Energy cell collected. Battery restored by 20%.",
                resourceChanges = mapOf("battery" to 20)
            ),
            LevelObjectDefinition(
                id = "exit",
                type = LevelObjectType.EXIT,
                position = GridPosition(4, 2),
                requiredObjectiveIds = setOf("collect_energy"),
                completesObjectiveId = "open_exit",
                timelineEvent = "open_exit",
                eventMessage = "Exit opened. Training room completed.",
                prerequisiteError = "The exit is offline. Collect the energy cell first."
            )
        ),
        sensors = emptyList(),
        scenarios = emptyList(),
        robotApi = setOf(RobotApi.MOVE, RobotApi.ACTION),
        pythonFeatures = introductoryPythonFeatures,
        objectives = listOf(
            LevelObjectiveDefinition("collect_energy", 1, "energy_collected"),
            LevelObjectiveDefinition("open_exit", 2, "exit_open")
        ),
        defaultPythonSource = "robot.move(\"right\")",
        resources = listOf(
            ResourceDefinition(
                id = "battery",
                initialValue = 32,
                minimum = 0,
                maximum = 100,
                moveCost = 1
            )
        )
    )

    val tutorial02 = LevelDefinition(
        id = "tutorial_02",
        sequence = 2,
        titleId = "Sensor dan Pengambilan Keputusan",
        titleEn = "Sensor & Decision",
        map = List(5) { "....." },
        robotStart = GridPosition(0, 2),
        objects = listOf(
            LevelObjectDefinition(
                id = "blocked_checkpoint",
                type = LevelObjectType.CHECKPOINT,
                position = GridPosition(0, 3),
                scenarioIds = setOf("blocked"),
                requiresSensorScan = true,
                completesObjectiveId = "open_checkpoint",
                timelineEvent = "open_checkpoint",
                eventMessage = "CHECKPOINT UNLOCKED // TUTORIAL 02 COMPLETE"
            ),
            LevelObjectDefinition(
                id = "clear_checkpoint",
                type = LevelObjectType.CHECKPOINT,
                position = GridPosition(1, 2),
                scenarioIds = setOf("clear"),
                requiresSensorScan = true,
                completesObjectiveId = "open_checkpoint",
                timelineEvent = "open_checkpoint",
                eventMessage = "CHECKPOINT UNLOCKED // TUTORIAL 02 COMPLETE"
            ),
            LevelObjectDefinition(
                id = "front_obstacle",
                type = LevelObjectType.OBSTACLE,
                position = GridPosition(1, 2),
                blocksMovement = true,
                scenarioIds = setOf("blocked")
            )
        ),
        sensors = listOf(
            SensorDefinition(
                id = "range_sensor",
                type = SensorType.DISTANCE,
                maximumRange = 1000,
                requiresScanBeforeMovement = true,
                completesObjectiveId = "read_sensor"
            )
        ),
        scenarios = listOf(
            SensorScenarioDefinition(
                id = "blocked",
                sensorReadings = mapOf("range_sensor" to 18),
                obstaclePositions = setOf(GridPosition(1, 2))
            ),
            SensorScenarioDefinition(
                id = "clear",
                sensorReadings = mapOf("range_sensor" to 42)
            )
        ),
        robotApi = setOf(RobotApi.MOVE, RobotApi.ACTION, RobotApi.SCAN_DISTANCE),
        pythonFeatures = introductoryPythonFeatures,
        objectives = listOf(
            LevelObjectiveDefinition("read_sensor", 1, "sensor_scanned"),
            LevelObjectiveDefinition("open_checkpoint", 2, "checkpoint_open")
        ),
        defaultPythonSource = """distance = robot.scan_distance()
if distance > 30:
    robot.move("right")
else:
    robot.move("down")
robot.action()"""
    )

    val all: List<LevelDefinition> = listOf(tutorial01, tutorial02)

    fun find(levelId: String): LevelDefinition? =
        all.firstOrNull { it.id == levelId }

    fun validateAll(): Map<String, List<String>> =
        all.mapNotNull { level ->
            level.validationErrors().takeIf { it.isNotEmpty() }?.let { level.id to it }
        }.toMap()
}
