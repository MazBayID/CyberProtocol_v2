package com.zeroday.cyberprotocol.core.level

/**
 * Completed campaign level ids are the save source of truth.
 * Unlocks are derived from catalog order so new levels need no hard-coded
 * tutorial-specific progression branches.
 */
data class CampaignProgress(
    val completedLevelIds: Set<String> = emptySet()
) {
    fun isCompleted(levelId: String): Boolean = levelId in completedLevelIds

    fun isUnlocked(levelId: String, levels: List<LevelDefinition>): Boolean {
        val target = levels.firstOrNull { it.id == levelId } ?: return false
        return levels
            .filter { it.sequence < target.sequence }
            .all { it.id in completedLevelIds }
    }

    fun unlockedLevelIds(levels: List<LevelDefinition>): Set<String> =
        levels.filter { isUnlocked(it.id, levels) }.map { it.id }.toSet()

    fun markCompleted(levelId: String, levels: List<LevelDefinition>): CampaignProgress {
        if (levels.none { it.id == levelId }) return this
        return copy(completedLevelIds = completedLevelIds + levelId)
    }
}
