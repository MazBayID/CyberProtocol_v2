package com.zeroday.cyberprotocol.ui.iso

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.zeroday.cyberprotocol.R
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

enum class IsoPropKind { ENERGY_CELL, CRATE }

/** A prop standing on the centre of a floor tile. */
data class IsoProp(val kind: IsoPropKind, val col: Int, val row: Int)

/** [action] is one of idle, move, interact, scan. [directionRow] is the robot sheet row (see [IsoMetrics.directionRow]). */
data class IsoRobot(val col: Int, val row: Int, val action: String, val directionRow: Int)

private const val MOVE_MILLIS = 390
private const val DOOR_MILLIS = 450
private val MarkerColor = Color(0xFF36E7F2)
private val HazardFill = Color(0x66FF3B3B)
private val HazardLine = Color(0xFFFF5A5A)

private class RobotSheet(val resource: Int, val frames: Int, val fps: Int, val loop: Boolean)

private fun robotSheetFor(action: String): RobotSheet = when (action) {
    "move" -> RobotSheet(R.drawable.anim_robot_walk, 8, 12, true)
    "interact" -> RobotSheet(R.drawable.anim_robot_interact, 8, 10, false)
    "scan" -> RobotSheet(R.drawable.anim_robot_scan, 8, 10, true)
    else -> RobotSheet(R.drawable.anim_robot_idle, 8, 6, true)
}

@Composable
private fun rememberSheetFrame(key: Any, frames: Int, fps: Int, loop: Boolean): State<Int> =
    produceState(initialValue = 0, key1 = key) {
        value = 0
        while (true) {
            delay(1000L / fps)
            if (loop) {
                value = (value + 1) % frames
            } else if (value < frames - 1) {
                value += 1
            }
        }
    }

/** Everything drawn upright, sorted back to front by the y of its ground contact. */
private sealed class IsoItem(val depth: Float) {
    class Piece(val piece: IsoPiece) : IsoItem(piece.y)
    class PropItem(val prop: IsoProp, x: Float, y: Float) : IsoItem(y) {
        val gx = x
        val gy = y
    }
    class RobotItem(x: Float, y: Float) : IsoItem(y + 0.01f) {
        val gx = x
        val gy = y
    }
}

/**
 * Draws a whole isometric room on one Canvas with ONE uniform scale.
 * Sprite sizes and anchors come from [IsoMetrics]; layout from [IsoRoomLayout]. No per-asset dp tuning.
 *
 * @param goalDoors tiles that carry a door (exit / checkpoint). Doors on `D` map tiles are added automatically.
 * @param goalDoorOpen open state of [goalDoors]. [doorsOpen] is the open state of `D` tiles.
 */
@Composable
fun IsoRoom(
    map: List<String>,
    robot: IsoRobot,
    modifier: Modifier = Modifier,
    props: List<IsoProp> = emptyList(),
    goalDoors: List<Pair<Int, Int>> = emptyList(),
    goalDoorOpen: Boolean = false,
    doorsOpen: Boolean = false,
    projection: IsoProjection = IsoProjection.COL_UP_RIGHT
) {
    val layout = remember(map, goalDoors, projection) { IsoRoomLayout(map, projection, goalDoors) }

    val floor = ImageBitmap.imageResource(R.drawable.tile_floor_metal)
    val wallStraight = ImageBitmap.imageResource(R.drawable.tile_wall_straight)
    val wallWindow = ImageBitmap.imageResource(R.drawable.structure_window)
    val wallCorner = ImageBitmap.imageResource(R.drawable.tile_wall_corner)
    val doorClosed = ImageBitmap.imageResource(R.drawable.structure_door)
    val doorSheet = ImageBitmap.imageResource(R.drawable.anim_door_open)
    val energyCell = ImageBitmap.imageResource(R.drawable.resource_energy_cell)
    val crate = ImageBitmap.imageResource(R.drawable.prop_supply_crate)
    val shadow = ImageBitmap.imageResource(R.drawable.robot_shadow)

    val sheet = robotSheetFor(robot.action)
    val robotBitmap = ImageBitmap.imageResource(sheet.resource)
    val frame by rememberSheetFrame(robot.action, sheet.frames, sheet.fps, sheet.loop)

    val robotCol by animateFloatAsState(robot.col.toFloat(), tween(MOVE_MILLIS, easing = LinearEasing), label = "isoRobotCol")
    val robotRow by animateFloatAsState(robot.row.toFloat(), tween(MOVE_MILLIS, easing = LinearEasing), label = "isoRobotRow")
    val goalDoorProgress by animateFloatAsState(if (goalDoorOpen) 1f else 0f, tween(DOOR_MILLIS), label = "isoGoalDoor")
    val tileDoorProgress by animateFloatAsState(if (doorsOpen) 1f else 0f, tween(DOOR_MILLIS), label = "isoTileDoor")

    Canvas(modifier = modifier) {
        val fit = layout.fit(size.width, size.height, padding = 6.dp.toPx(), maxScale = 0.30f * density)

        fun sx(x: Float) = x * fit.scale + fit.offsetX
        fun sy(y: Float) = y * fit.scale + fit.offsetY

        // 1. floor, hazard overlays, shadows and the tile marker (always below everything upright)
        for (tile in layout.tiles) {
            drawSprite(floor, IsoMetrics.FLOOR, tile.x + IsoMetrics.HALF_W, tile.y + IsoMetrics.HALF_H, false, fit)
            if (tile.symbol == 'H') {
                val cx = sx(tile.x + IsoMetrics.HALF_W)
                val cy = sy(tile.y + IsoMetrics.HALF_H)
                val hw = IsoMetrics.HALF_W * fit.scale
                val hh = IsoMetrics.HALF_H * fit.scale
                val diamond = Path().apply {
                    moveTo(cx, cy - hh); lineTo(cx + hw, cy); lineTo(cx, cy + hh); lineTo(cx - hw, cy); close()
                }
                drawPath(diamond, HazardFill)
                drawLine(HazardLine, Offset(cx - hw * 0.5f, cy), Offset(cx + hw * 0.5f, cy), 2.dp.toPx(), StrokeCap.Round)
            }
        }
        val (robotX, robotY) = layout.groundPoint(robotCol, robotRow)
        drawTileMarker(sx(robotX), sy(robotY), fit.scale, MarkerColor)
        drawSprite(shadow, IsoMetrics.SHADOW, robotX, robotY, false, fit, alpha = 0.9f)
        for (prop in props) {
            if (prop.kind != IsoPropKind.ENERGY_CELL) continue
            val (gx, gy) = layout.groundPoint(prop.col.toFloat(), prop.row.toFloat())
            val glowCenter = Offset(sx(gx), sy(gy))
            val glowRadius = 34f * fit.scale
            withTransform({ scale(1f, 0.5f, glowCenter) }) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xAA36E7F2), Color(0x5536E7F2), Color.Transparent),
                        center = glowCenter, radius = glowRadius
                    ),
                    radius = glowRadius, center = glowCenter
                )
            }
        }

        // 2. upright things, back to front
        val items = ArrayList<IsoItem>(layout.pieces.size + props.size + 1)
        layout.pieces.forEach { items += IsoItem.Piece(it) }
        props.forEach {
            val (gx, gy) = layout.groundPoint(it.col.toFloat(), it.row.toFloat())
            items += IsoItem.PropItem(it, gx, gy)
        }
        items += IsoItem.RobotItem(robotX, robotY)
        items.sortBy { it.depth }

        for (item in items) {
            when (item) {
                is IsoItem.Piece -> {
                    val piece = item.piece
                    when (piece.kind) {
                        IsoPieceKind.WALL_STRAIGHT -> drawSprite(wallStraight, IsoMetrics.WALL_STRAIGHT, piece.x, piece.y, piece.flip, fit)
                        IsoPieceKind.WALL_WINDOW -> drawSprite(wallWindow, IsoMetrics.WALL_WINDOW, piece.x, piece.y, piece.flip, fit)
                        IsoPieceKind.WALL_CORNER -> drawSprite(wallCorner, IsoMetrics.WALL_CORNER, piece.x, piece.y, piece.flip, fit)
                        IsoPieceKind.DOOR -> {
                            val goal = goalDoors.contains(Pair(piece.col, piece.row))
                            val progress = if (goal) goalDoorProgress else tileDoorProgress
                            if (progress <= 0.01f) {
                                drawSprite(doorClosed, IsoMetrics.DOOR, piece.x, piece.y, piece.flip, fit)
                            } else {
                                val index = (progress * 3f).roundToInt().coerceIn(0, 3)
                                drawSprite(
                                    doorSheet, IsoMetrics.DOOR, piece.x, piece.y, piece.flip, fit,
                                    srcOffset = IntOffset(index * 256, 0), srcSize = IntSize(256, 256)
                                )
                            }
                        }
                    }
                }
                is IsoItem.PropItem -> when (item.prop.kind) {
                    IsoPropKind.ENERGY_CELL -> drawSprite(energyCell, IsoMetrics.ENERGY_CELL, item.gx, item.gy, false, fit)
                    IsoPropKind.CRATE -> drawSprite(crate, IsoMetrics.CRATE, item.gx, item.gy, false, fit)
                }
                is IsoItem.RobotItem -> drawSprite(
                    robotBitmap, IsoMetrics.ROBOT, item.gx, item.gy, false, fit,
                    srcOffset = IntOffset(frame * 256, robot.directionRow.coerceIn(0, 3) * 256),
                    srcSize = IntSize(256, 256)
                )
            }
        }
    }
}

/**
 * Draws [image] (or the [srcOffset]/[srcSize] frame of it) so that its ground pivot lands on layout point ([x],[y]).
 * Flipping mirrors around the pivot. The scale is the room scale times the sprite's own correction.
 */
private fun DrawScope.drawSprite(
    image: ImageBitmap,
    spec: IsoSpriteSpec,
    x: Float,
    y: Float,
    flip: Boolean,
    fit: IsoFit,
    srcOffset: IntOffset = IntOffset.Zero,
    srcSize: IntSize = IntSize(image.width, image.height),
    alpha: Float = 1f
) {
    val k = fit.scale * spec.scale
    withTransform({
        translate(x * fit.scale + fit.offsetX, y * fit.scale + fit.offsetY)
        scale(if (flip) -k else k, k, Offset.Zero)
        translate(-spec.pivotX, -spec.pivotY)
    }) {
        drawImage(
            image = image,
            srcOffset = srcOffset,
            srcSize = srcSize,
            dstOffset = IntOffset.Zero,
            dstSize = srcSize,
            alpha = alpha,
            filterQuality = FilterQuality.High
        )
    }
}

/** Four corner brackets on the diamond of the tile the robot stands on. Drawn, not an image, so it is always an exact 2:1. */
private fun DrawScope.drawTileMarker(cx: Float, cy: Float, scale: Float, color: Color) {
    val hw = IsoMetrics.HALF_W * scale * 0.94f
    val hh = IsoMetrics.HALF_H * scale * 0.94f
    val corners = listOf(Offset(cx, cy - hh), Offset(cx + hw, cy), Offset(cx, cy + hh), Offset(cx - hw, cy))
    val stroke = 1.8.dp.toPx()
    for (i in corners.indices) {
        val vertex = corners[i]
        drawLine(color, vertex, lerp(vertex, corners[(i + 1) % 4], 0.24f), stroke, StrokeCap.Round)
        drawLine(color, vertex, lerp(vertex, corners[(i + 3) % 4], 0.24f), stroke, StrokeCap.Round)
    }
}
