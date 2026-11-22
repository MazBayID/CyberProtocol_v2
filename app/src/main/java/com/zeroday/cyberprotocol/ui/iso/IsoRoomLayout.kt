package com.zeroday.cyberprotocol.ui.iso

enum class IsoPieceKind { WALL_STRAIGHT, WALL_WINDOW, WALL_CORNER, DOOR }

/** A floor tile. [x],[y] is the top-left corner of the 256x152 floor image in layout pixels. */
data class IsoTile(val col: Int, val row: Int, val symbol: Char, val x: Float, val y: Float)

/** A standing piece (wall panel, corner, door). [x],[y] is where the sprite's ground pivot goes. */
data class IsoPiece(
    val kind: IsoPieceKind,
    val x: Float,
    val y: Float,
    val flip: Boolean,
    val col: Int,
    val row: Int,
    val edge: IsoEdge
)

data class IsoBounds(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
}

/** [scale] converts layout pixels to screen pixels, then add the offsets. */
data class IsoFit(val scale: Float, val offsetX: Float, val offsetY: Float)

/**
 * Pure geometry of an isometric room. No Android or Compose types, so it is unit-testable.
 *
 * Map symbols: `.` floor, `H` hazard floor, `D` door tile (floor with a door), `#` solid wall block.
 * Anything else, and anything outside the map, counts as "no floor".
 */
class IsoRoomLayout(
    val map: List<String>,
    val projection: IsoProjection,
    goalDoorTiles: List<Pair<Int, Int>> = emptyList()
) {
    val cols: Int = map.maxOfOrNull { it.length } ?: 0
    val rows: Int = map.size

    val tiles: List<IsoTile>
    val pieces: List<IsoPiece>
    val bounds: IsoBounds

    private val doorSlots = HashMap<Pair<Int, Int>, IsoEdge>()

    fun symbolAt(col: Int, row: Int): Char = map.getOrNull(row)?.getOrNull(col) ?: ' '

    fun isFloor(col: Int, row: Int): Boolean = symbolAt(col, row).let { it == '.' || it == 'H' || it == 'D' }

    private fun screenDeltaX(dCol: Int, dRow: Int) = dCol * projection.colDx + dRow * projection.rowDx

    private fun screenDeltaY(dCol: Int, dRow: Int) = dCol * projection.colDy + dRow * projection.rowDy

    fun tileX(col: Float, row: Float): Float = (col * projection.colDx + row * projection.rowDx) * IsoMetrics.HALF_W

    fun tileY(col: Float, row: Float): Float = (col * projection.colDy + row * projection.rowDy) * IsoMetrics.HALF_H

    /** Ground point (centre of the top diamond) of a possibly fractional cell, for props and the moving robot. */
    fun groundPoint(col: Float, row: Float): Pair<Float, Float> =
        Pair(tileX(col, row) + IsoMetrics.HALF_W, tileY(col, row) + IsoMetrics.HALF_H)

    /** Grid offset (dCol, dRow) of the neighbour that lies across [edge] of a tile. */
    fun neighbourAcross(edge: IsoEdge): Pair<Int, Int> {
        val wantLeft = edge == IsoEdge.NW || edge == IsoEdge.SW
        val wantUp = edge == IsoEdge.NW || edge == IsoEdge.NE
        for ((dc, dr) in listOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1))) {
            val sx = screenDeltaX(dc, dr)
            val sy = screenDeltaY(dc, dr)
            if ((sx < 0) == wantLeft && (sy < 0) == wantUp) return Pair(dc, dr)
        }
        error("Projection has no neighbour across $edge")
    }

    private fun isWallAcross(col: Int, row: Int, edge: IsoEdge): Boolean {
        val (dc, dr) = neighbourAcross(edge)
        return !isFloor(col + dc, row + dr)
    }

    /** Which back edge a door on this tile stands on. Door tiles follow the corridor; goal doors prefer a wall plane. */
    private fun chooseDoorEdge(col: Int, row: Int): IsoEdge {
        val back = listOf(IsoEdge.NE, IsoEdge.NW)
        if (symbolAt(col, row) == 'D') {
            val alongCol = isFloor(col - 1, row) && isFloor(col + 1, row)
            val alongRow = isFloor(col, row - 1) && isFloor(col, row + 1)
            if (alongCol != alongRow) {
                return back.first { edge -> (neighbourAcross(edge).first != 0) == alongCol }
            }
        }
        return back.firstOrNull { isWallAcross(col, row, it) } ?: IsoEdge.NE
    }

    init {
        val tileList = ArrayList<IsoTile>()
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val symbol = symbolAt(col, row)
                if (symbol == '.' || symbol == 'H' || symbol == 'D') {
                    tileList += IsoTile(col, row, symbol, tileX(col.toFloat(), row.toFloat()), tileY(col.toFloat(), row.toFloat()))
                }
            }
        }
        // painter's order for the floor: back (small y) to front
        tiles = tileList.sortedWith(compareBy<IsoTile> { it.y }.thenBy { it.x })

        for (tile in tiles) {
            if (tile.symbol == 'D' || goalDoorTiles.contains(Pair(tile.col, tile.row))) {
                doorSlots[Pair(tile.col, tile.row)] = chooseDoorEdge(tile.col, tile.row)
            }
        }

        val result = ArrayList<IsoPiece>()
        val straight = ArrayList<IsoPiece>()
        for (tile in tiles) {
            val doorEdge = doorSlots[Pair(tile.col, tile.row)]
            val wallNw = isWallAcross(tile.col, tile.row, IsoEdge.NW) && doorEdge != IsoEdge.NW
            val wallNe = isWallAcross(tile.col, tile.row, IsoEdge.NE) && doorEdge != IsoEdge.NE
            if (wallNw && wallNe) {
                result += IsoPiece(
                    IsoPieceKind.WALL_CORNER, tile.x + IsoMetrics.HALF_W, tile.y, false, tile.col, tile.row, IsoEdge.NW
                )
            } else {
                for ((edge, present) in listOf(Pair(IsoEdge.NW, wallNw), Pair(IsoEdge.NE, wallNe))) {
                    if (present) straight += wallPiece(tile, edge, IsoPieceKind.WALL_STRAIGHT)
                }
            }
            if (doorEdge != null) result += wallPiece(tile, doorEdge, IsoPieceKind.DOOR)
        }
        // one window in the middle of each long wall run
        for (edge in listOf(IsoEdge.NW, IsoEdge.NE)) {
            val run = straight.filter { it.edge == edge }.sortedBy { it.x }
            val middle = if (run.size >= 3) run[run.size / 2] else null
            for (piece in run) {
                result += if (piece === middle) piece.copy(kind = IsoPieceKind.WALL_WINDOW) else piece
            }
        }
        // solid blocks: wall faces on the two edges that face the viewer
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if (symbolAt(col, row) != '#') continue
                for (edge in listOf(IsoEdge.SW, IsoEdge.SE)) {
                    val (dc, dr) = neighbourAcross(edge)
                    if (isFloor(col + dc, row + dr)) {
                        val x = tileX(col.toFloat(), row.toFloat())
                        val y = tileY(col.toFloat(), row.toFloat())
                        result += IsoPiece(
                            IsoPieceKind.WALL_STRAIGHT, x + IsoMetrics.edgeMidX(edge), y + IsoMetrics.edgeMidY(edge),
                            IsoMetrics.edgeNeedsFlip(edge), col, row, edge
                        )
                    }
                }
            }
        }
        pieces = result.sortedWith(compareBy<IsoPiece> { it.y }.thenBy { it.x })
        bounds = computeBounds()
    }

    private fun wallPiece(tile: IsoTile, edge: IsoEdge, kind: IsoPieceKind) = IsoPiece(
        kind, tile.x + IsoMetrics.edgeMidX(edge), tile.y + IsoMetrics.edgeMidY(edge),
        IsoMetrics.edgeNeedsFlip(edge), tile.col, tile.row, edge
    )

    fun doorPiece(col: Int, row: Int): IsoPiece? = pieces.firstOrNull { it.kind == IsoPieceKind.DOOR && it.col == col && it.row == row }

    fun specFor(kind: IsoPieceKind): IsoSpriteSpec = when (kind) {
        IsoPieceKind.WALL_STRAIGHT -> IsoMetrics.WALL_STRAIGHT
        IsoPieceKind.WALL_WINDOW -> IsoMetrics.WALL_WINDOW
        IsoPieceKind.WALL_CORNER -> IsoMetrics.WALL_CORNER
        IsoPieceKind.DOOR -> IsoMetrics.DOOR
    }

    private fun computeBounds(): IsoBounds {
        var left = Float.MAX_VALUE
        var top = Float.MAX_VALUE
        var right = -Float.MAX_VALUE
        var bottom = -Float.MAX_VALUE
        fun include(l: Float, t: Float, r: Float, b: Float) {
            left = minOf(left, l); top = minOf(top, t); right = maxOf(right, r); bottom = maxOf(bottom, b)
        }
        for (tile in tiles) include(tile.x, tile.y, tile.x + IsoMetrics.FLOOR.width, tile.y + IsoMetrics.FLOOR.height)
        for (piece in pieces) {
            val spec = specFor(piece.kind)
            val s = spec.scale
            val l = if (piece.flip) piece.x - (spec.width - spec.pivotX) * s else piece.x - spec.pivotX * s
            val r = if (piece.flip) piece.x + spec.pivotX * s else piece.x + (spec.width - spec.pivotX) * s
            include(l, piece.y - spec.pivotY * s, r, piece.y + (spec.height - spec.pivotY) * s)
        }
        if (left == Float.MAX_VALUE) return IsoBounds(0f, 0f, 0f, 0f)
        return IsoBounds(left, top, right, bottom)
    }

    /** Uniform scale that fits the whole room (walls included) into the given box and centres it. */
    fun fit(availableWidth: Float, availableHeight: Float, padding: Float = 0f, maxScale: Float = Float.MAX_VALUE): IsoFit {
        val w = bounds.width
        val h = bounds.height
        if (w <= 0f || h <= 0f) return IsoFit(1f, 0f, 0f)
        val scale = minOf((availableWidth - 2 * padding) / w, (availableHeight - 2 * padding) / h, maxScale)
        return IsoFit(
            scale,
            (availableWidth - w * scale) / 2f - bounds.left * scale,
            (availableHeight - h * scale) / 2f - bounds.top * scale
        )
    }
}
