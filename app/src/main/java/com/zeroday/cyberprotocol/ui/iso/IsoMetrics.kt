package com.zeroday.cyberprotocol.ui.iso

/**
 * Single source of truth for isometric proportions.
 *
 * Every number below is measured from the asset pixels (see docs/ISO_RENDERING.md).
 * All layout is done in "source pixels": the pixel space of the 256 px wide floor tile.
 * The renderer applies ONE uniform scale to everything, so nothing can drift out of proportion.
 */

/** Which way the grid axes run on screen. */
enum class IsoProjection(val colDx: Int, val colDy: Int, val rowDx: Int, val rowDy: Int) {
    /** +col runs up-right (towards the back-right wall), +row runs down-right. Exits at the far right touch the back wall. */
    COL_UP_RIGHT(1, -1, 1, 1),

    /** Legacy mapping: +col runs down-right, +row runs down-left. */
    COL_DOWN_RIGHT(1, 1, -1, 1)
}

/** The four edges of a tile, named by where they point on screen. NW and NE face away from the viewer. */
enum class IsoEdge { NW, NE, SW, SE }

/** Size of a sprite and the point of it that touches the ground, in sprite pixels. [scale] is a per-asset size correction. */
data class IsoSpriteSpec(
    val width: Int,
    val height: Int,
    val pivotX: Float,
    val pivotY: Float,
    val scale: Float = 1f
)

object IsoMetrics {
    const val TILE_W = 256f
    const val TILE_H = 128f
    const val HALF_W = TILE_W / 2f
    const val HALF_H = TILE_H / 2f

    /** Floor slab: 256x152 image, the top diamond (256x128) sits at the top, pivot = diamond centre. */
    val FLOOR = IsoSpriteSpec(256, 152, 128f, 64f)

    /** Wall panels stand on an edge midpoint. Pivots come from the silhouette of the sprite base. */
    val WALL_STRAIGHT = IsoSpriteSpec(256, 256, 127.5f, 217.6f)
    val WALL_WINDOW = IsoSpriteSpec(256, 256, 127.5f, 216.1f)

    /** Concave corner: the seam meets the floor at the tile's back vertex. */
    val WALL_CORNER = IsoSpriteSpec(256, 256, 127.5f, 195f)

    /** The door sprite is drawn 7.7% smaller than the wall panels (169 px vs 183 px wide), so it is scaled up to match them. */
    val DOOR = IsoSpriteSpec(256, 256, 127.5f, 210.7f, scale = 1.083f)

    /** Collectible. 1.35 is a deliberate readability boost, not a proportion error. */
    val ENERGY_CELL = IsoSpriteSpec(128, 128, 63.5f, 108.5f, scale = 1.35f)
    val CRATE = IsoSpriteSpec(256, 256, 127.5f, 189f)

    /** Robot frames are 256x256, feet touch the ground at (128, 206). Shadow image is 256x128 centred on its middle. */
    val ROBOT = IsoSpriteSpec(256, 256, 128f, 206f)
    val SHADOW = IsoSpriteSpec(256, 128, 128f, 64f)

    /** Midpoint of each tile edge relative to the tile's top-left corner, in source pixels. */
    fun edgeMidX(edge: IsoEdge): Float = if (edge == IsoEdge.NW || edge == IsoEdge.SW) 64f else 192f

    fun edgeMidY(edge: IsoEdge): Float = if (edge == IsoEdge.NW || edge == IsoEdge.NE) 32f else 96f

    /** Wall sprites run "/" on the NW and SE edges, so the NE and SW edges need the mirrored sprite. */
    fun edgeNeedsFlip(edge: IsoEdge): Boolean = edge == IsoEdge.NE || edge == IsoEdge.SW

    /**
     * Robot sheet row for a one-tile move. Rows: 0 = SW, 1 = SE, 2 = NE, 3 = NW.
     * Returns [fallback] when the move has no direction.
     */
    fun directionRow(projection: IsoProjection, dCol: Int, dRow: Int, fallback: Int): Int {
        val sx = dCol * projection.colDx + dRow * projection.rowDx
        val sy = dCol * projection.colDy + dRow * projection.rowDy
        return when {
            sx < 0 && sy > 0 -> 0
            sx > 0 && sy > 0 -> 1
            sx > 0 && sy < 0 -> 2
            sx < 0 && sy < 0 -> 3
            else -> fallback
        }
    }
}
