package com.zeroday.cyberprotocol.data

import android.content.Context
import com.zeroday.cyberprotocol.core.level.CampaignProgress
import com.zeroday.cyberprotocol.core.level.TrainingLevelCatalog

/**
 * Campaign progress is stored separately from language and settings.
 * Only completed stable level ids are persisted; unlocks are derived from the
 * current level catalog so catalog changes do not require a new save format.
 */
object CampaignProgressPreferences {
    private const val STORE_NAME = "cp_campaign_progress_v1"
    private const val KEY_COMPLETED_LEVEL_IDS = "completed_level_ids"

    fun load(context: Context): CampaignProgress {
        val savedIds = context.applicationContext
            .getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_COMPLETED_LEVEL_IDS, emptySet())
            .orEmpty()
            .toSet()
        return CampaignProgress(completedLevelIds = savedIds)
    }

    fun save(context: Context, progress: CampaignProgress) {
        context.applicationContext
            .getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_COMPLETED_LEVEL_IDS, progress.completedLevelIds.toSet())
            .apply()
    }

    fun markCompleted(context: Context, levelId: String) {
        val current = load(context)
        save(context, current.markCompleted(levelId, TrainingLevelCatalog.all))
    }
}
