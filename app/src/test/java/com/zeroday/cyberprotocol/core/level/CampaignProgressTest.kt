package com.zeroday.cyberprotocol.core.level

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CampaignProgressTest {
    @Test
    fun firstLevelStartsUnlockedAndSecondWaitsForFirstCompletion() {
        val progress = CampaignProgress()

        assertTrue(progress.isUnlocked("tutorial_01", TrainingLevelCatalog.all))
        assertFalse(progress.isUnlocked("tutorial_02", TrainingLevelCatalog.all))
    }

    @Test
    fun completingFirstLevelUnlocksSecondLevel() {
        val progress = CampaignProgress()
            .markCompleted("tutorial_01", TrainingLevelCatalog.all)

        assertTrue(progress.isCompleted("tutorial_01"))
        assertTrue(progress.isUnlocked("tutorial_02", TrainingLevelCatalog.all))
        assertEquals(setOf("tutorial_01", "tutorial_02"), progress.unlockedLevelIds(TrainingLevelCatalog.all))
    }

    @Test
    fun unknownLevelCannotBeCompletedOrUnlocked() {
        val progress = CampaignProgress()
            .markCompleted("tutorial_99", TrainingLevelCatalog.all)

        assertFalse(progress.isCompleted("tutorial_99"))
        assertFalse(progress.isUnlocked("tutorial_99", TrainingLevelCatalog.all))
        assertTrue(progress.completedLevelIds.isEmpty())
    }

    @Test
    fun completionIsIdempotent() {
        val once = CampaignProgress().markCompleted("tutorial_01", TrainingLevelCatalog.all)
        val twice = once.markCompleted("tutorial_01", TrainingLevelCatalog.all)

        assertEquals(once, twice)
    }
}
