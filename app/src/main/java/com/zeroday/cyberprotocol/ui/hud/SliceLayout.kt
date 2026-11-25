package com.zeroday.cyberprotocol.ui.hud

/** One rectangle copied from the source image to the screen. Every value is a whole pixel, so neighbouring parts never leave gaps. */
data class SlicePart(
    val srcX: Int,
    val srcY: Int,
    val srcW: Int,
    val srcH: Int,
    val dstX: Int,
    val dstY: Int,
    val dstW: Int,
    val dstH: Int
)

/**
 * Pure geometry for stretchable UI art. Corners keep their proportions, only the straight parts stretch,
 * so a frame or button never gets distorted whatever the size of the panel.
 */
object SliceLayout {
    /**
     * Frame with a transparent centre: 4 corners + 4 edges. [cap] is the corner size in source pixels,
     * [scale] is destination pixels per source pixel. The scale shrinks when the panel is too small for two caps.
     */
    fun nineSlice(srcW: Int, srcH: Int, cap: Int, dstW: Int, dstH: Int, scale: Float): List<SlicePart> {
        if (dstW <= 0 || dstH <= 0 || cap <= 0) return emptyList()
        val fitted = minOf(scale, dstW / (2f * cap), dstH / (2f * cap))
        val capDst = Math.round(cap * fitted)
        val x = intArrayOf(0, capDst, dstW - capDst, dstW)
        val y = intArrayOf(0, capDst, dstH - capDst, dstH)
        val sx = intArrayOf(0, cap, srcW - cap, srcW)
        val sy = intArrayOf(0, cap, srcH - cap, srcH)
        val parts = ArrayList<SlicePart>(8)
        for (row in 0..2) {
            for (col in 0..2) {
                if (row == 1 && col == 1) continue // transparent opening
                val part = SlicePart(
                    sx[col], sy[row], sx[col + 1] - sx[col], sy[row + 1] - sy[row],
                    x[col], y[row], x[col + 1] - x[col], y[row + 1] - y[row]
                )
                if (part.dstW > 0 && part.dstH > 0 && part.srcW > 0 && part.srcH > 0) parts += part
            }
        }
        return parts
    }

    /**
     * Button: left cap, stretched middle, right cap. The art is scaled by height, so the caps keep their proportions.
     * [srcTop] selects a state row when several states are stacked in one sheet.
     */
    fun threeSlice(srcW: Int, srcH: Int, cap: Int, dstW: Int, dstH: Int, srcTop: Int = 0): List<SlicePart> {
        if (dstW <= 0 || dstH <= 0 || cap <= 0) return emptyList()
        val capDst = minOf(Math.round(cap * dstH.toFloat() / srcH), dstW / 2)
        val parts = ArrayList<SlicePart>(3)
        parts += SlicePart(0, srcTop, cap, srcH, 0, 0, capDst, dstH)
        if (dstW - 2 * capDst > 0) {
            parts += SlicePart(cap, srcTop, srcW - 2 * cap, srcH, capDst, 0, dstW - 2 * capDst, dstH)
        }
        parts += SlicePart(srcW - cap, srcTop, cap, srcH, dstW - capDst, 0, capDst, dstH)
        return parts
    }
}
