package com.zeroday.cyberprotocol.ui.hud

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SliceLayoutTest {
    @Test
    fun frameHasFourCornersAndFourEdges() {
        val parts = SliceLayout.nineSlice(256, 256, 96, 1089, 649, 0.764f)
        assertEquals(8, parts.size)
    }

    @Test
    fun frameCoversEveryPixelOutsideTheOpening() {
        for ((w, h) in listOf(Pair(1089, 649), Pair(1089, 894), Pair(900, 500), Pair(333, 211))) {
            val parts = SliceLayout.nineSlice(256, 256, 96, w, h, 0.764f)
            val cap = parts.first().dstW
            val opening = (w - 2 * cap) * (h - 2 * cap)
            val covered = parts.sumOf { it.dstW * it.dstH }
            assertEquals("$w x $h", w * h, covered + opening)
        }
    }

    @Test
    fun cornersKeepTheirProportions() {
        for ((w, h) in listOf(Pair(1089, 649), Pair(1089, 894), Pair(500, 1200))) {
            val corners = SliceLayout.nineSlice(256, 256, 96, w, h, 0.764f).filter { it.srcW == 96 && it.srcH == 96 }
            assertEquals(4, corners.size)
            assertTrue(corners.all { it.dstW == it.dstH && it.dstW == 73 })
        }
    }

    @Test
    fun onlyTheEdgesStretch() {
        val parts = SliceLayout.nineSlice(256, 256, 96, 1089, 649, 0.764f)
        val top = parts.first { it.srcX == 96 && it.srcY == 0 }
        assertEquals(1089 - 2 * 73, top.dstW)
        assertEquals(73, top.dstH)
        val left = parts.first { it.srcX == 0 && it.srcY == 96 }
        assertEquals(73, left.dstW)
        assertEquals(649 - 2 * 73, left.dstH)
    }

    @Test
    fun smallPanelsShrinkTheCapsInsteadOfOverlapping() {
        val parts = SliceLayout.nineSlice(256, 256, 96, 100, 60, 1f)
        assertTrue(parts.all { it.dstW >= 0 && it.dstH >= 0 })
        assertTrue(parts.all { it.dstX + it.dstW <= 100 && it.dstY + it.dstH <= 60 })
        val corner = parts.first { it.srcX == 0 && it.srcY == 0 }
        assertEquals(corner.dstW, corner.dstH)
    }

    @Test
    fun emptyAreaGivesNoParts() {
        assertTrue(SliceLayout.nineSlice(256, 256, 96, 0, 100, 1f).isEmpty())
        assertTrue(SliceLayout.threeSlice(384, 128, 64, 100, 0).isEmpty())
    }

    @Test
    fun buttonCapsKeepTheAspectRatioOfTheArt() {
        // 384x128 art, cap 64. Drawn 39 px high the caps must be 64 * 39 / 128 wide.
        val parts = SliceLayout.threeSlice(384, 128, 64, 276, 39)
        assertEquals(3, parts.size)
        assertEquals(Math.round(64 * 39f / 128), parts[0].dstW)
        assertEquals(parts[0].dstW, parts[2].dstW)
        assertTrue(parts.all { it.dstH == 39 })
    }

    @Test
    fun buttonPartsTileTheWidthWithoutGaps() {
        for (w in listOf(120, 276, 1000)) {
            val parts = SliceLayout.threeSlice(384, 128, 64, w, 66)
            assertEquals(w, parts.sumOf { it.dstW })
            assertEquals(0, parts[0].dstX)
            assertEquals(parts[0].dstW, parts[1].dstX)
            assertEquals(w - parts[2].dstW, parts[2].dstX)
        }
    }

    @Test
    fun buttonStateSelectsTheRowOfTheSheet() {
        val parts = SliceLayout.threeSlice(384, 128, 64, 276, 66, srcTop = 3 * 128)
        assertTrue(parts.all { it.srcY == 384 && it.srcH == 128 })
    }

    @Test
    fun narrowButtonNeverOverlapsItsCaps() {
        val parts = SliceLayout.threeSlice(384, 128, 64, 60, 66)
        assertTrue(parts.all { it.dstW >= 0 })
        assertTrue(parts.sumOf { it.dstW } <= 60)
    }
}
