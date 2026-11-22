package com.zeroday.cyberprotocol.ui.iso

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IsoRoomLayoutTest {
    private val open5 = List(5) { "....." }

    @Test
    fun directionRowFollowsTheProjection() {
        // rows: 0 = SW, 1 = SE, 2 = NE, 3 = NW
        val p = IsoProjection.COL_UP_RIGHT
        assertEquals(2, IsoMetrics.directionRow(p, 1, 0, -1)) // right -> up-right
        assertEquals(1, IsoMetrics.directionRow(p, 0, 1, -1)) // down -> down-right
        assertEquals(0, IsoMetrics.directionRow(p, -1, 0, -1)) // left -> down-left
        assertEquals(3, IsoMetrics.directionRow(p, 0, -1, -1)) // up -> up-left
        val legacy = IsoProjection.COL_DOWN_RIGHT
        assertEquals(1, IsoMetrics.directionRow(legacy, 1, 0, -1))
        assertEquals(0, IsoMetrics.directionRow(legacy, 0, 1, -1))
        assertEquals(3, IsoMetrics.directionRow(legacy, -1, 0, -1))
        assertEquals(2, IsoMetrics.directionRow(legacy, 0, -1, -1))
        assertEquals(7, IsoMetrics.directionRow(p, 0, 0, 7))
    }

    @Test
    fun tilesStepByHalfATileAlongEachAxis() {
        val layout = IsoRoomLayout(open5, IsoProjection.COL_UP_RIGHT)
        assertEquals(128f, layout.tileX(1f, 0f) - layout.tileX(0f, 0f), 0.001f)
        assertEquals(-64f, layout.tileY(1f, 0f) - layout.tileY(0f, 0f), 0.001f)
        assertEquals(128f, layout.tileX(0f, 1f) - layout.tileX(0f, 0f), 0.001f)
        assertEquals(64f, layout.tileY(0f, 1f) - layout.tileY(0f, 0f), 0.001f)
        assertEquals(25, layout.tiles.size)
    }

    @Test
    fun floorIsSortedBackToFront() {
        val layout = IsoRoomLayout(open5, IsoProjection.COL_UP_RIGHT)
        val ys = layout.tiles.map { it.y }
        assertEquals(ys.sorted(), ys)
    }

    @Test
    fun wallsStandOnTheTwoBackEdgesOnly() {
        val layout = IsoRoomLayout(open5, IsoProjection.COL_UP_RIGHT)
        assertTrue(layout.pieces.all { it.edge == IsoEdge.NW || it.edge == IsoEdge.NE })
        // 4 + 4 straight panels, one concave corner where both back walls meet
        assertEquals(1, layout.pieces.count { it.kind == IsoPieceKind.WALL_CORNER })
        assertEquals(8, layout.pieces.count { it.kind == IsoPieceKind.WALL_STRAIGHT || it.kind == IsoPieceKind.WALL_WINDOW })
        assertEquals(2, layout.pieces.count { it.kind == IsoPieceKind.WALL_WINDOW })
        val corner = layout.pieces.first { it.kind == IsoPieceKind.WALL_CORNER }
        assertEquals(4, corner.col)
        assertEquals(0, corner.row)
    }

    @Test
    fun wallOrientationMatchesTheEdgeItStandsOn() {
        val layout = IsoRoomLayout(open5, IsoProjection.COL_UP_RIGHT)
        assertTrue(layout.pieces.filter { it.edge == IsoEdge.NE && it.kind != IsoPieceKind.WALL_CORNER }.all { it.flip })
        assertTrue(layout.pieces.filter { it.edge == IsoEdge.NW && it.kind != IsoPieceKind.WALL_CORNER }.none { it.flip })
    }

    @Test
    fun straightWallSitsOnTheMidpointOfItsEdge() {
        val layout = IsoRoomLayout(open5, IsoProjection.COL_UP_RIGHT)
        val piece = layout.pieces.first { it.edge == IsoEdge.NW && it.col == 0 && it.row == 0 }
        val tile = layout.tiles.first { it.col == 0 && it.row == 0 }
        assertEquals(tile.x + 64f, piece.x, 0.001f)
        assertEquals(tile.y + 32f, piece.y, 0.001f)
    }

    @Test
    fun legacyProjectionPutsTheCornerAtTheOtherEnd() {
        val layout = IsoRoomLayout(open5, IsoProjection.COL_DOWN_RIGHT)
        val corner = layout.pieces.first { it.kind == IsoPieceKind.WALL_CORNER }
        assertEquals(0, corner.col)
        assertEquals(0, corner.row)
        assertTrue(layout.pieces.all { it.edge == IsoEdge.NW || it.edge == IsoEdge.NE })
    }

    @Test
    fun goalDoorReplacesTheWallPanelOnItsTile() {
        val layout = IsoRoomLayout(open5, IsoProjection.COL_UP_RIGHT, listOf(Pair(4, 2)))
        val door = layout.doorPiece(4, 2)
        assertNotNull(door)
        assertEquals(IsoEdge.NE, door!!.edge)
        assertTrue(door.flip)
        assertNull(layout.pieces.firstOrNull { it.kind == IsoPieceKind.WALL_STRAIGHT && it.col == 4 && it.row == 2 })
        assertEquals(1, layout.pieces.count { it.kind == IsoPieceKind.DOOR })
    }

    @Test
    fun doorTileStandsAcrossTheCorridor() {
        val layout = IsoRoomLayout(listOf("..D.."), IsoProjection.COL_UP_RIGHT)
        val door = layout.doorPiece(2, 0)
        assertNotNull(door)
        assertEquals(IsoEdge.NE, door!!.edge) // the back edge on the corridor axis
    }

    @Test
    fun solidBlocksShowFacesToTheViewer() {
        val layout = IsoRoomLayout(listOf("...", ".#.", "..."), IsoProjection.COL_UP_RIGHT)
        val faces = layout.pieces.filter { it.col == 1 && it.row == 1 }
        assertEquals(setOf(IsoEdge.SW, IsoEdge.SE), faces.map { it.edge }.toSet())
        assertEquals(0, layout.tiles.count { it.col == 1 && it.row == 1 })
    }

    @Test
    fun groundPointIsTheCentreOfTheDiamond() {
        val layout = IsoRoomLayout(open5, IsoProjection.COL_UP_RIGHT)
        val tile = layout.tiles.first { it.col == 2 && it.row == 3 }
        val (gx, gy) = layout.groundPoint(2f, 3f)
        assertEquals(tile.x + 128f, gx, 0.001f)
        assertEquals(tile.y + 64f, gy, 0.001f)
    }

    @Test
    fun boundsCoverWallsAndFloor() {
        val layout = IsoRoomLayout(open5, IsoProjection.COL_UP_RIGHT)
        val wallTops = layout.pieces.minOf { it.y - layout.specFor(it.kind).pivotY * layout.specFor(it.kind).scale }
        assertEquals(wallTops, layout.bounds.top, 0.001f)
        assertEquals(layout.tiles.maxOf { it.y } + IsoMetrics.FLOOR.height, layout.bounds.bottom, 0.001f)
    }

    @Test
    fun fitKeepsOneUniformScaleAndCentersTheRoom() {
        val layout = IsoRoomLayout(open5, IsoProjection.COL_UP_RIGHT)
        val fit = layout.fit(400f, 300f, padding = 10f)
        val width = layout.bounds.width * fit.scale
        val height = layout.bounds.height * fit.scale
        assertTrue(width <= 380.01f && height <= 280.01f)
        assertTrue(width >= 379.9f || height >= 279.9f) // the limiting side is filled
        val left = layout.bounds.left * fit.scale + fit.offsetX
        val top = layout.bounds.top * fit.scale + fit.offsetY
        assertEquals((400f - width) / 2f, left, 0.01f)
        assertEquals((300f - height) / 2f, top, 0.01f)
    }

    @Test
    fun fitCanBeCapped() {
        val layout = IsoRoomLayout(open5, IsoProjection.COL_UP_RIGHT)
        assertEquals(0.1f, layout.fit(4000f, 3000f, maxScale = 0.1f).scale, 0.0001f)
    }

    @Test
    fun anyMapSizeFitsWithoutChangingProportions() {
        for (map in listOf(List(3) { "............" }, List(6) { "........" }, List(5) { ".." })) {
            val layout = IsoRoomLayout(map, IsoProjection.COL_UP_RIGHT)
            val fit = layout.fit(320f, 220f)
            assertTrue(layout.bounds.width * fit.scale <= 320.01f)
            assertTrue(layout.bounds.height * fit.scale <= 220.01f)
        }
    }

    @Test
    fun emptyMapIsHarmless() {
        val layout = IsoRoomLayout(emptyList(), IsoProjection.COL_UP_RIGHT)
        assertEquals(0f, layout.bounds.width, 0f)
        assertEquals(1f, layout.fit(100f, 100f).scale, 0f)
        assertFalse(layout.isFloor(0, 0))
    }

    @Test
    fun spriteSpecsKeepTheMeasuredRelations() {
        // door and wall share one visual width once the door is scaled up
        assertEquals(183f, 169f * IsoMetrics.DOOR.scale, 0.5f)
        assertEquals(256f, IsoMetrics.TILE_W, 0f)
        assertEquals(IsoMetrics.TILE_W / 2f, IsoMetrics.TILE_H, 0f)
    }
}
