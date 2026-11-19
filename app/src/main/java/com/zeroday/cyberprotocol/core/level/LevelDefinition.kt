package com.zeroday.cyberprotocol.core.level

import org.json.JSONArray
import org.json.JSONObject

/**
 * Data contract for a campaign level.
 *
 * This model is independent from Compose and Chaquopy. It can be validated
 * as ordinary data and serialized for the generic Python runtime.
 */
data class GridPosition(val x: Int, val y: Int)

enum class LevelObjectType {
    ENERGY_CELL,
    EXIT,
    CHECKPOINT,
    OBSTACLE
}

enum class SensorType {
    DISTANCE
}

enum class RobotApi {
    MOVE,
    ACTION,
    SCAN_DISTANCE
}

/** Catalog metadata only; Python syntax enforcement remains a separate runner concern. */
enum class PythonFeature {
    VARIABLES,
    PRINT,
    CONDITIONALS,
    FOR_LOOPS,
    FUNCTIONS,
    LISTS,
    TUPLES
}

data class ResourceDefinition(
    val id: String,
    val initialValue: Int,
    val minimum: Int,
    val maximum: Int,
    val moveCost: Int = 0
)

data class LevelObjectDefinition(
    val id: String,
    val type: LevelObjectType,
    val position: GridPosition,
    val blocksMovement: Boolean = false,
    /** Empty means the object exists in every scenario. */
    val scenarioIds: Set<String> = emptySet(),
    val requiresSensorScan: Boolean = false,
    val requiredObjectiveIds: Set<String> = emptySet(),
    val completesObjectiveId: String? = null,
    val timelineEvent: String? = null,
    val eventMessage: String? = null,
    val prerequisiteError: String? = null,
    val resourceChanges: Map<String, Int> = emptyMap()
)

data class SensorScenarioDefinition(
    val id: String,
    val sensorReadings: Map<String, Int>,
    val obstaclePositions: Set<GridPosition> = emptySet()
)

data class SensorDefinition(
    val id: String,
    val type: SensorType,
    val maximumRange: Int,
    val requiresScanBeforeMovement: Boolean = false,
    val completesObjectiveId: String? = null
)

data class LevelObjectiveDefinition(
    val id: String,
    val order: Int,
    /** Stable event key emitted by the level runtime, not localized UI text. */
    val completionEvent: String
)

data class LevelDefinition(
    val id: String,
    val sequence: Int,
    val titleId: String,
    val titleEn: String,
    val map: List<String>,
    val robotStart: GridPosition,
    val objects: List<LevelObjectDefinition>,
    val sensors: List<SensorDefinition>,
    val scenarios: List<SensorScenarioDefinition>,
    val robotApi: Set<RobotApi>,
    val pythonFeatures: Set<PythonFeature>,
    val objectives: List<LevelObjectiveDefinition>,
    val defaultPythonSource: String,
    val resources: List<ResourceDefinition> = emptyList()
) {
    val width: Int
        get() = map.firstOrNull()?.length ?: 0

    val height: Int
        get() = map.size

    fun validationErrors(): List<String> {
        val errors = mutableListOf<String>()
        if (id.isBlank()) errors += "Level id must not be blank."
        if (sequence < 1) errors += "Level sequence must be positive."
        if (titleId.isBlank() || titleEn.isBlank()) errors += "Both localized titles are required."
        if (map.isEmpty() || map.firstOrNull().isNullOrEmpty()) {
            errors += "Map must contain at least one non-empty row."
            return errors
        }

        val expectedWidth = map.first().length
        val rectangularMap = map.all { it.length == expectedWidth }
        if (!rectangularMap) errors += "Map rows must have equal width."
        if (map.any { row -> row.any { it !in VALID_MAP_TILES } }) {
            errors += "Map contains an unsupported tile symbol."
        }
        if (!robotStart.isInside(expectedWidth, map.size)) {
            errors += "Robot start is outside the map."
        } else if (rectangularMap && map[robotStart.y][robotStart.x] == WALL_TILE) {
            errors += "Robot start cannot be inside a wall."
        }

        if (objects.any { it.id.isBlank() }) errors += "Object ids must not be blank."
        if (objects.map { it.id }.distinct().size != objects.size) errors += "Object ids must be unique."
        objects.forEach { obj ->
            if (!obj.position.isInside(expectedWidth, map.size)) {
                errors += "Object '${obj.id}' is outside the map."
            } else if (rectangularMap && map[obj.position.y][obj.position.x] == WALL_TILE) {
                errors += "Object '${obj.id}' cannot be placed inside a wall."
            }
        }

        if (sensors.any { it.id.isBlank() }) errors += "Sensor ids must not be blank."
        if (sensors.map { it.id }.distinct().size != sensors.size) errors += "Sensor ids must be unique."
        if (sensors.any { it.maximumRange < 1 }) errors += "Sensor maximum range must be positive."

        if (scenarios.any { it.id.isBlank() }) errors += "Scenario ids must not be blank."
        if (scenarios.map { it.id }.distinct().size != scenarios.size) errors += "Scenario ids must be unique."
        val sensorsById = sensors.associateBy { it.id }
        scenarios.forEach { scenario ->
            scenario.sensorReadings.forEach { (sensorId, reading) ->
                val sensor = sensorsById[sensorId]
                if (sensor == null) {
                    errors += "Scenario '${scenario.id}' refers to an unknown sensor."
                } else if (reading !in 0..sensor.maximumRange) {
                    errors += "Scenario '${scenario.id}' has a reading outside sensor '${sensor.id}' range."
                }
            }
            if (scenario.obstaclePositions.any { !it.isInside(expectedWidth, map.size) }) {
                errors += "Scenario '${scenario.id}' has an obstacle outside the map."
            }
            if (rectangularMap && scenario.obstaclePositions.any { map[it.y][it.x] == WALL_TILE }) {
                errors += "Scenario '${scenario.id}' places an obstacle inside a wall."
            }
        }

        val scenarioIds = scenarios.map { it.id }.toSet()
        if (objects.any { it.scenarioIds.isNotEmpty() && !scenarioIds.containsAll(it.scenarioIds) }) {
            errors += "An object refers to an unknown scenario."
        }

        if (objectives.any { it.id.isBlank() || it.completionEvent.isBlank() }) {
            errors += "Objective ids and completion events must not be blank."
        }
        if (objectives.map { it.id }.distinct().size != objectives.size) errors += "Objective ids must be unique."
        if (objectives.map { it.order }.distinct().size != objectives.size) errors += "Objective order values must be unique."
        if (objectives.any { it.order < 1 }) errors += "Objective order values must be positive."

        val objectiveIds = objectives.map { it.id }.toSet()
        if (sensors.any { it.completesObjectiveId != null && it.completesObjectiveId !in objectiveIds }) {
            errors += "A sensor refers to an unknown objective."
        }
        objects.forEach { obj ->
            if (!objectiveIds.containsAll(obj.requiredObjectiveIds)) {
                errors += "Object '${obj.id}' requires an unknown objective."
            }
            if (obj.completesObjectiveId != null && obj.completesObjectiveId !in objectiveIds) {
                errors += "Object '${obj.id}' completes an unknown objective."
            }
        }

        if (resources.any { it.id.isBlank() }) errors += "Resource ids must not be blank."
        if (resources.map { it.id }.distinct().size != resources.size) errors += "Resource ids must be unique."
        resources.forEach { resource ->
            if (resource.minimum > resource.initialValue || resource.initialValue > resource.maximum ||
                resource.moveCost < 0
            ) errors += "Resource '${resource.id}' has invalid limits."
        }
        val resourceIds = resources.map { it.id }.toSet()
        if (objects.any { !resourceIds.containsAll(it.resourceChanges.keys) }) {
            errors += "An object changes an unknown resource."
        }

        if (defaultPythonSource.isBlank()) errors += "Default Python source must not be blank."
        if (defaultPythonSource.length > MAX_DEFAULT_SOURCE_LENGTH) {
            errors += "Default Python source exceeds the runner source limit."
        }
        if (sensors.isNotEmpty() && RobotApi.SCAN_DISTANCE !in robotApi) {
            errors += "Levels with sensors must expose the scan_distance robot API."
        }
        return errors
    }

    /**
     * Serialize only runtime behavior. UI strings and teaching metadata stay in Kotlin.
     */
    fun toRunnerJson(): String = JSONObject().apply {
        put("id", id)
        put("map", JSONArray(map))
        put("robot_start", JSONObject().apply {
            put("x", robotStart.x)
            put("y", robotStart.y)
        })
        put("robot_api", JSONArray(robotApi.map { it.name.lowercase() }))
        put("resources", JSONObject().apply {
            resources.forEach { resource ->
                put(resource.id, JSONObject().apply {
                    put("initial", resource.initialValue)
                    put("minimum", resource.minimum)
                    put("maximum", resource.maximum)
                    put("move_cost", resource.moveCost)
                })
            }
        })
        put("sensors", JSONArray().apply {
            sensors.forEach { sensor ->
                put(JSONObject().apply {
                    put("id", sensor.id)
                    put("type", sensor.type.name)
                    put("maximum_range", sensor.maximumRange)
                    put("requires_scan_before_movement", sensor.requiresScanBeforeMovement)
                    sensor.completesObjectiveId?.let { put("completes_objective", it) }
                })
            }
        })
        put("scenarios", JSONArray().apply {
            scenarios.forEach { scenario ->
                put(JSONObject().apply {
                    put("id", scenario.id)
                    put("sensor_readings", JSONObject().apply {
                        scenario.sensorReadings.forEach { (sensorId, reading) -> put(sensorId, reading) }
                    })
                    put("obstacle_positions", JSONArray().apply {
                        scenario.obstaclePositions.forEach { position ->
                            put(JSONObject().apply {
                                put("x", position.x)
                                put("y", position.y)
                            })
                        }
                    })
                })
            }
        })
        put("objects", JSONArray().apply {
            objects.forEach { obj ->
                put(JSONObject().apply {
                    put("id", obj.id)
                    put("type", obj.type.name)
                    put("position", JSONObject().apply {
                        put("x", obj.position.x)
                        put("y", obj.position.y)
                    })
                    put("blocks_movement", obj.blocksMovement)
                    put("scenario_ids", JSONArray(obj.scenarioIds.toList()))
                    put("requires_sensor_scan", obj.requiresSensorScan)
                    put("required_objectives", JSONArray(obj.requiredObjectiveIds.toList()))
                    obj.completesObjectiveId?.let { put("completes_objective", it) }
                    obj.timelineEvent?.let { put("timeline_event", it) }
                    obj.eventMessage?.let { put("event_message", it) }
                    obj.prerequisiteError?.let { put("prerequisite_error", it) }
                    put("resource_changes", JSONObject().apply {
                        obj.resourceChanges.forEach { (resourceId, change) -> put(resourceId, change) }
                    })
                })
            }
        })
        put("objectives", JSONArray().apply {
            objectives.sortedBy { it.order }.forEach { objective ->
                put(JSONObject().apply {
                    put("id", objective.id)
                    put("completion_event", objective.completionEvent)
                })
            }
        })
    }.toString()

    companion object {
        const val WALL_TILE = '#'
        const val FLOOR_TILE = '.'
        const val MAX_DEFAULT_SOURCE_LENGTH = 12_000
        private val VALID_MAP_TILES = setOf(WALL_TILE, FLOOR_TILE)
    }
}

private fun GridPosition.isInside(width: Int, height: Int): Boolean =
    x in 0 until width && y in 0 until height
