package com.zeroday.cyberprotocol.execution

import com.chaquo.python.Python
import com.zeroday.cyberprotocol.core.level.LevelDefinition
import com.zeroday.cyberprotocol.core.level.TrainingLevelCatalog
import org.json.JSONArray
import org.json.JSONObject

/**
 * Shared runtime state for every campaign level.
 *
 * Tutorial-specific snapshots below are compatibility adapters for the
 * existing Compose screens and saved progress.
 */
data class LevelSessionState(
    val robotX: Int,
    val robotY: Int,
    val resources: Map<String, Int> = emptyMap(),
    val completedObjectiveIds: Set<String> = emptySet(),
    val sensorScanned: Boolean = false,
    val sensorReadings: Map<String, Int> = emptyMap(),
    val scenarioId: String? = null,
    val completed: Boolean = false
) {
    fun toJson(): String = JSONObject().apply {
        put("robot_x", robotX)
        put("robot_y", robotY)
        put("resources", JSONObject().apply {
            resources.forEach { (id, value) -> put(id, value) }
        })
        put("completed_objectives", JSONArray(completedObjectiveIds.sorted()))
        put("sensor_scanned", sensorScanned)
        put("sensor_readings", JSONObject().apply {
            sensorReadings.forEach { (id, value) -> put(id, value) }
        })
        if (scenarioId == null) put("scenario_id", JSONObject.NULL) else put("scenario_id", scenarioId)
        put("completed", completed)
    }.toString()

    companion object {
        fun fromJson(json: JSONObject, level: LevelDefinition): LevelSessionState {
            val resourceValues = json.optJSONObject("resources") ?: JSONObject()
            val resources = buildMap {
                val keys = resourceValues.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    put(key, resourceValues.optInt(key))
                }
            }
            val sensorValues = json.optJSONObject("sensor_readings") ?: JSONObject()
            val sensorReadings = buildMap {
                val keys = sensorValues.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    put(key, sensorValues.optInt(key))
                }
            }
            val objectives = json.optJSONArray("completed_objectives") ?: JSONArray()
            val completedObjectiveIds = buildSet {
                for (index in 0 until objectives.length()) {
                    objectives.optString(index).takeIf { it.isNotBlank() }?.let(::add)
                }
            }
            return LevelSessionState(
                robotX = json.optInt("robot_x", level.robotStart.x).coerceIn(0, (level.width - 1).coerceAtLeast(0)),
                robotY = json.optInt("robot_y", level.robotStart.y).coerceIn(0, (level.height - 1).coerceAtLeast(0)),
                resources = resources,
                completedObjectiveIds = completedObjectiveIds,
                sensorScanned = json.optBoolean("sensor_scanned"),
                sensorReadings = sensorReadings,
                scenarioId = if (json.isNull("scenario_id")) null else json.optString("scenario_id").takeIf { it.isNotBlank() },
                completed = json.optBoolean("completed")
            )
        }
    }
}

data class LevelRunResult(
    val output: String,
    val error: String,
    val state: LevelSessionState,
    val timeline: List<String>,
    val events: List<String>,
    val complete: Boolean
)

class LevelExecutionService {
    fun run(source: String, level: LevelDefinition, state: LevelSessionState): LevelRunResult = try {
        val raw = Python.getInstance().getModule("runner")
            .callAttr("run_level_json", source, level.toRunnerJson(), state.toJson()).toString()
        val json = JSONObject(raw)
        val stateJson = json.optJSONObject("state")
        val timeline = json.optJSONArray("timeline").toStringList()
        val events = json.optJSONArray("events").toStringList()
        LevelRunResult(
            output = json.optString("output"),
            error = json.optString("error"),
            state = if (stateJson == null || stateJson.length() == 0) {
                state
            } else {
                LevelSessionState.fromJson(stateJson, level)
            },
            timeline = timeline,
            events = events,
            complete = json.optBoolean("complete")
        )
    } catch (error: Exception) {
        LevelRunResult(
            output = "",
            error = error.message ?: "Python level runtime error",
            state = state,
            timeline = emptyList(),
            events = emptyList(),
            complete = false
        )
    }
}

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) add(optString(index))
    }
}

data class TutorialSnapshot(
    val robotX: Int = 0,
    val robotY: Int = 2,
    val energyCollected: Boolean = false,
    val exitOpen: Boolean = false,
    val completed: Boolean = false,
    val battery: Int = 32
) {
    fun toJson(): String = JSONObject().apply {
        put("robot_x", robotX)
        put("robot_y", robotY)
        put("energy_collected", energyCollected)
        put("exit_open", exitOpen)
        put("completed", completed)
        put("battery", battery)
    }.toString()

    companion object {
        fun fromJson(json: JSONObject) = TutorialSnapshot(
            robotX = json.optInt("robot_x", 0).coerceIn(0, 4),
            robotY = json.optInt("robot_y", 2).coerceIn(0, 4),
            energyCollected = json.optBoolean("energy_collected"),
            exitOpen = json.optBoolean("exit_open"),
            completed = json.optBoolean("completed"),
            battery = json.optInt("battery", 32).coerceIn(0, 100)
        )
    }
}

data class TutorialRunResult(
    val output: String,
    val error: String,
    val state: TutorialSnapshot,
    val timeline: List<String>,
    val events: List<String>,
    val complete: Boolean
)

class TutorialExecutionService {
    private val levelExecutionService = LevelExecutionService()

    fun run(source: String, state: TutorialSnapshot): TutorialRunResult {
        val genericState = LevelSessionState(
            robotX = state.robotX,
            robotY = state.robotY,
            resources = mapOf("battery" to state.battery),
            completedObjectiveIds = buildSet {
                if (state.energyCollected) add("collect_energy")
                if (state.exitOpen) add("open_exit")
            },
            completed = state.completed
        )
        val result = levelExecutionService.run(source, TrainingLevelCatalog.tutorial01, genericState)
        val updated = result.state
        return TutorialRunResult(
            output = result.output,
            error = result.error,
            state = TutorialSnapshot(
                robotX = updated.robotX,
                robotY = updated.robotY,
                energyCollected = "collect_energy" in updated.completedObjectiveIds,
                exitOpen = "open_exit" in updated.completedObjectiveIds,
                completed = result.complete,
                battery = updated.resources["battery"] ?: state.battery
            ),
            timeline = result.timeline,
            events = result.events,
            complete = result.complete
        )
    }
}

data class Tutorial2Snapshot(
    val robotX: Int = 0,
    val robotY: Int = 2,
    val sensorScanned: Boolean = false,
    val sensorDistance: Int = 18,
    val checkpointOpen: Boolean = false,
    val completed: Boolean = false
) {
    fun toJson(): String = JSONObject().apply {
        put("robot_x", robotX)
        put("robot_y", robotY)
        put("sensor_scanned", sensorScanned)
        put("sensor_distance", sensorDistance)
        put("checkpoint_open", checkpointOpen)
        put("completed", completed)
    }.toString()

    companion object {
        fun fromJson(json: JSONObject) = Tutorial2Snapshot(
            robotX = json.optInt("robot_x", 0).coerceIn(0, 4),
            robotY = json.optInt("robot_y", 2).coerceIn(0, 4),
            sensorScanned = json.optBoolean("sensor_scanned"),
            sensorDistance = json.optInt("sensor_distance", 18).coerceIn(0, 1000),
            checkpointOpen = json.optBoolean("checkpoint_open"),
            completed = json.optBoolean("completed")
        )
    }
}

data class Tutorial2RunResult(
    val output: String,
    val error: String,
    val state: Tutorial2Snapshot,
    val timeline: List<String>,
    val events: List<String>,
    val complete: Boolean
)

class Tutorial2ExecutionService {
    private val levelExecutionService = LevelExecutionService()

    fun run(source: String, state: Tutorial2Snapshot): Tutorial2RunResult {
        val distance = state.sensorDistance.coerceIn(0, 1000)
        val genericState = LevelSessionState(
            robotX = state.robotX,
            robotY = state.robotY,
            completedObjectiveIds = buildSet {
                if (state.sensorScanned) add("read_sensor")
                if (state.checkpointOpen) add("open_checkpoint")
            },
            sensorScanned = state.sensorScanned,
            sensorReadings = mapOf("range_sensor" to distance),
            scenarioId = if (distance > 30) "clear" else "blocked",
            completed = state.completed
        )
        val result = levelExecutionService.run(source, TrainingLevelCatalog.tutorial02, genericState)
        val updated = result.state
        return Tutorial2RunResult(
            output = result.output,
            error = result.error,
            state = Tutorial2Snapshot(
                robotX = updated.robotX,
                robotY = updated.robotY,
                sensorScanned = updated.sensorScanned,
                sensorDistance = updated.sensorReadings["range_sensor"] ?: distance,
                checkpointOpen = "open_checkpoint" in updated.completedObjectiveIds,
                completed = result.complete
            ),
            timeline = result.timeline,
            events = result.events,
            complete = result.complete
        )
    }
}
