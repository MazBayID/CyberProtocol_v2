package com.zeroday.cyberprotocol.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeroday.cyberprotocol.R
import com.zeroday.cyberprotocol.core.level.TrainingLevelCatalog
import com.zeroday.cyberprotocol.core.model.GameLanguage
import com.zeroday.cyberprotocol.data.GameLanguagePreferences
import com.zeroday.cyberprotocol.execution.TutorialExecutionService
import com.zeroday.cyberprotocol.execution.TutorialSnapshot
import com.zeroday.cyberprotocol.execution.Tutorial2ExecutionService
import com.zeroday.cyberprotocol.execution.Tutorial2Snapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val TutorialBackground = Color(0xFF070C12)
private val TutorialAccent = Color(0xFFB56CFF)
private val TutorialText = Color(0xFFCDA6FF)
private val TutorialMuted = Color(0xFFA98ACB)
private val TutorialCodeGreen = Color(0xFF8BFFA0)
private val TutorialEditorBase = Color(0xFFE6EDF3)
private val TutorialSyntax = PythonSyntaxHighlightTransformation()

@Composable
fun CyberProtocolTutorialExperience(onTutorial2: () -> Unit, onLevelCompleted: () -> Unit = {}) {
    val context = LocalContext.current
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val isId = remember { GameLanguagePreferences.load(context) == GameLanguage.INDONESIAN }
    var snapshot by remember { mutableStateOf(TutorialSnapshot()) }
    var source by rememberSaveable { mutableStateOf("robot.move(\"right\")") }
    var output by rememberSaveable { mutableStateOf(if (isId) "MX-01 // SIAP MENERIMA INSTRUKSI" else "MX-01 // READY FOR COMMANDS") }
    var error by rememberSaveable { mutableStateOf("") }
    var running by remember { mutableStateOf(false) }
    var robotAction by remember { mutableStateOf("idle") }
    var robotDirectionRow by remember { mutableIntStateOf(1) }
    val scope = rememberCoroutineScope()
    val execution = remember { TutorialExecutionService() }

    fun runPlayerCode() {
        if (running || snapshot.completed || source.isBlank()) return
        playGameUiFeedback(context, view, haptic)
        running = true
        error = ""
        output = if (isId) "MENJALANKAN PROGRAM..." else "EXECUTING PLAYER PROGRAM..."
        scope.launch {
            val result = withContext(Dispatchers.IO) { execution.run(source, snapshot) }
            result.timeline.forEach { action ->
                when {
                    action.startsWith("move:") -> {
                        robotAction = "move"
                        val parts = action.split(":")
                        if (parts.size == 3) {
                            val nextX = parts[1].toIntOrNull() ?: snapshot.robotX
                            val nextY = parts[2].toIntOrNull() ?: snapshot.robotY
                            robotDirectionRow = when {
                                nextX > snapshot.robotX -> 1 // SE
                                nextX < snapshot.robotX -> 3 // NW
                                nextY > snapshot.robotY -> 0 // SW
                                nextY < snapshot.robotY -> 2 // NE
                                else -> robotDirectionRow
                            }
                            snapshot = snapshot.copy(robotX = nextX, robotY = nextY)
                        }
                        delay(390L)
                    }
                    action == "collect_energy" || action == "open_exit" -> {
                        robotAction = "interact"
                        delay(650L)
                    }
                }
            }
            snapshot = result.state
            if (result.complete) onLevelCompleted()
            robotAction = "idle"
            error = result.error
            output = when {
                result.error.isNotBlank() -> if (result.output.isBlank()) "RUNTIME ERROR" else result.output.trimEnd()
                result.output.isNotBlank() -> result.output.trimEnd()
                result.events.isNotEmpty() -> result.events.joinToString("\n")
                else -> if (isId) "PROGRAM SELESAI." else "PROGRAM FINISHED."
            }
            running = false
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(TutorialBackground)) {
        val compact = maxHeight < 650.dp
        val outerPadding = if (compact) 5.dp else 8.dp
        Column(
            Modifier.fillMaxSize().padding(outerPadding),
            verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 7.dp)
        ) {
            TutorialTerminalPanel(
                frame = R.drawable.ui_terminal_frame_classic,
                modifier = Modifier.fillMaxWidth().weight(0.29f),
                contentHorizontalInset = 0.095f,
                contentVerticalInset = 0.165f
            ) { TutorialGuidance(snapshot, isId, compact) }
            TutorialTerminalPanel(
                frame = R.drawable.ui_terminal_frame,
                background = R.drawable.bg_digital_network,
                modifier = Modifier.fillMaxWidth().weight(0.40f),
                contentHorizontalInset = 0.095f,
                contentVerticalInset = 0.12f,
                backgroundHorizontalInset = 0.105f,
                backgroundVerticalInset = 0.13f
            ) { TutorialRoomScene(snapshot, robotAction, robotDirectionRow, isId) }
            TutorialTerminalPanel(
                frame = R.drawable.ui_code_editor_frame,
                modifier = Modifier.fillMaxWidth().weight(0.31f),
                contentHorizontalInset = 0.095f,
                contentVerticalInset = 0.12f
            ) {
                Column(
                    Modifier.fillMaxSize().background(Color(0xFF101923).copy(alpha = 1f)),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Text(
                            if (isId) "03 / EDITOR PYTHON" else "03 / PYTHON EDITOR",
                            color = TutorialAccent,
                            fontSize = if (compact) 11.sp else 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.weight(1f))
                        androidx.compose.material3.Text("CPYTHON 3.13", color = TutorialMuted, fontSize = 10.sp)
                    }
                    BasicTextField(
                        value = source,
                        onValueChange = { source = it },
                        modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                        textStyle = TextStyle(
                            color = TutorialEditorBase,
                            fontFamily = DepartureMonoFamily,
                            fontSize = if (compact) 14.sp else 15.sp,
                            lineHeight = if (compact) 18.sp else 20.sp
                        ),
                        cursorBrush = SolidColor(TutorialAccent),
                        visualTransformation = TutorialSyntax,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        maxLines = Int.MAX_VALUE,
                        decorationBox = { innerTextField ->
                            Box(Modifier.fillMaxSize().padding(horizontal = 5.dp, vertical = 2.dp)) {
                                innerTextField()
                            }
                        }
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        androidx.compose.material3.Text(
                            text = when {
                                error.isNotBlank() -> error
                                snapshot.completed -> if (isId) "LATIHAN SELESAI" else "TRAINING COMPLETE"
                                else -> output.lineSequence().lastOrNull().orEmpty()
                            },
                            color = if (error.isNotBlank()) Color(0xFFFF9292) else Color(0xFFB7C4CC),
                            fontSize = 10.sp,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        if (snapshot.completed) {
                            TutorialRunButton(
                                label = if (isId) "LANJUT >>" else "NEXT >>",
                                enabled = !running,
                                running = false,
                                onClick = onTutorial2,
                                modifier = Modifier.offset(y = (-5).dp)
                            )
                        } else {
                            TutorialRunButton(
                                label = if (isId) "JALANKAN" else "RUN",
                                enabled = !running && source.isNotBlank(),
                                running = running,
                                onClick = { runPlayerCode() },
                                modifier = Modifier.offset(y = (-5).dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CyberProtocolTutorial2Experience(onLevelCompleted: () -> Unit = {}) {
    val context = LocalContext.current
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val isId = remember { GameLanguagePreferences.load(context) == GameLanguage.INDONESIAN }
    var snapshot by remember { mutableStateOf(Tutorial2Snapshot()) }
    var source by rememberSaveable {
        mutableStateOf(
            """distance = robot.scan_distance()
if distance > 30:
    robot.move("right")
else:
    robot.move("down")
robot.action()"""
        )
    }
    var output by rememberSaveable {
        mutableStateOf(if (isId) "MX-01 // SENSOR JARAK SIAP" else "MX-01 // RANGE SENSOR READY")
    }
    var error by rememberSaveable { mutableStateOf("") }
    var running by remember { mutableStateOf(false) }
    var robotAction by remember { mutableStateOf("idle") }
    var robotDirectionRow by remember { mutableIntStateOf(1) }
    val scope = rememberCoroutineScope()
    val execution = remember { Tutorial2ExecutionService() }

    fun runPlayerCode() {
        if (running || snapshot.completed || source.isBlank()) return
        playGameUiFeedback(context, view, haptic)
        running = true
        error = ""
        output = if (isId) "MENJALANKAN PROGRAM..." else "EXECUTING PLAYER PROGRAM..."
        scope.launch {
            val result = withContext(Dispatchers.IO) { execution.run(source, snapshot) }
            result.timeline.forEach { action ->
                when {
                    action.startsWith("scan:") -> {
                        robotAction = "scan"
                        val distance = action.substringAfter(":").toIntOrNull() ?: snapshot.sensorDistance
                        snapshot = snapshot.copy(sensorScanned = true, sensorDistance = distance)
                        delay(430L)
                    }
                    action.startsWith("move:") -> {
                        robotAction = "move"
                        val parts = action.split(":")
                        if (parts.size == 3) {
                            val nextX = parts[1].toIntOrNull() ?: snapshot.robotX
                            val nextY = parts[2].toIntOrNull() ?: snapshot.robotY
                            robotDirectionRow = when {
                                nextX > snapshot.robotX -> 1 // SE
                                nextX < snapshot.robotX -> 3 // NW
                                nextY > snapshot.robotY -> 0 // SW
                                nextY < snapshot.robotY -> 2 // NE
                                else -> robotDirectionRow
                            }
                            snapshot = snapshot.copy(robotX = nextX, robotY = nextY)
                        }
                        delay(390L)
                    }
                    action == "open_checkpoint" -> {
                        robotAction = "interact"
                        delay(650L)
                    }
                }
            }
            snapshot = result.state
            if (result.complete) onLevelCompleted()
            robotAction = "idle"
            error = result.error
            output = when {
                result.error.isNotBlank() -> if (result.output.isBlank()) "RUNTIME ERROR" else result.output.trimEnd()
                result.output.isNotBlank() -> result.output.trimEnd()
                result.events.isNotEmpty() -> result.events.joinToString("\n")
                else -> if (isId) "PROGRAM SELESAI." else "PROGRAM FINISHED."
            }
            running = false
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(TutorialBackground)) {
        val compact = maxHeight < 650.dp
        val outerPadding = if (compact) 5.dp else 8.dp
        Column(
            Modifier.fillMaxSize().padding(outerPadding),
            verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 7.dp)
        ) {
            TutorialTerminalPanel(
                frame = R.drawable.ui_terminal_frame_classic,
                modifier = Modifier.fillMaxWidth().weight(0.29f),
                contentHorizontalInset = 0.095f,
                contentVerticalInset = 0.165f
            ) { Tutorial2Guidance(snapshot, isId, compact) }
            TutorialTerminalPanel(
                frame = R.drawable.ui_terminal_frame,
                background = R.drawable.bg_digital_network,
                modifier = Modifier.fillMaxWidth().weight(0.40f),
                contentHorizontalInset = 0.095f,
                contentVerticalInset = 0.12f,
                backgroundHorizontalInset = 0.105f,
                backgroundVerticalInset = 0.13f
            ) { Tutorial2RoomScene(snapshot, robotAction, robotDirectionRow, isId) {
                    if (!running && !snapshot.completed) {
                        snapshot = snapshot.copy(
                            robotX = 0,
                            robotY = 2,
                            sensorDistance = if (snapshot.sensorDistance <= 30) 42 else 18,
                            sensorScanned = false,
                            checkpointOpen = false,
                            completed = false
                        )
                        error = ""
                        output = if (isId) "MODE SIMULASI DIUBAH" else "SIMULATION MODE CHANGED"
                    }
                } }
            TutorialTerminalPanel(
                frame = R.drawable.ui_code_editor_frame,
                modifier = Modifier.fillMaxWidth().weight(0.31f),
                contentHorizontalInset = 0.095f,
                contentVerticalInset = 0.12f
            ) {
                Column(
                    Modifier.fillMaxSize().background(Color(0xFF101923)),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Text(
                            if (isId) "03 / EDITOR PYTHON" else "03 / PYTHON EDITOR",
                            color = TutorialAccent,
                            fontSize = if (compact) 11.sp else 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.weight(1f))
                        androidx.compose.material3.Text("CPYTHON 3.13", color = TutorialMuted, fontSize = 10.sp)
                    }
                    BasicTextField(
                        value = source,
                        onValueChange = { source = it },
                        modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                        textStyle = TextStyle(
                            color = TutorialEditorBase,
                            fontFamily = DepartureMonoFamily,
                            fontSize = if (compact) 10.sp else 11.sp,
                            lineHeight = if (compact) 12.sp else 13.sp
                        ),
                        cursorBrush = SolidColor(TutorialAccent),
                        visualTransformation = TutorialSyntax,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        maxLines = Int.MAX_VALUE,
                        decorationBox = { innerTextField ->
                            Box(Modifier.fillMaxSize().padding(horizontal = 5.dp, vertical = 1.dp)) {
                                innerTextField()
                            }
                        }
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        androidx.compose.material3.Text(
                            text = when {
                                error.isNotBlank() -> error
                                snapshot.completed -> if (isId) "MISI SELESAI" else "MISSION COMPLETE"
                                else -> output.lineSequence().lastOrNull().orEmpty()
                            },
                            color = if (error.isNotBlank()) Color(0xFFFF9292) else Color(0xFFB7C4CC),
                            fontSize = 10.sp,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        TutorialRunButton(
                            label = if (isId) "JALANKAN" else "RUN",
                            enabled = !running && !snapshot.completed && source.isNotBlank(),
                            running = running,
                            onClick = { runPlayerCode() },
                            modifier = Modifier.offset(y = (-5).dp)
                        )
                    }
                }
            }
        }
    }
}

private data class GuidancePanelContent(
    val trainingLabel: String,
    val objectiveTitle: String,
    val description: String,
    val progressLabel: String,
    val detailLabel: String,
    val detailSpacingDp: Int,
    val statusLabel: String
)

@Composable
private fun TutorialGuidancePanel(content: GuidancePanelContent, isId: Boolean, compact: Boolean) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        Column(
            Modifier.align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Text(
                    if (isId) "01 / TERMINAL PETUNJUK" else "01 / GUIDANCE TERMINAL",
                    color = TutorialAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.6.sp
                )
                Spacer(Modifier.weight(1f))
                androidx.compose.material3.Text(content.trainingLabel, color = TutorialMuted, fontSize = 9.sp)
            }
            androidx.compose.material3.Text(
                content.objectiveTitle,
                color = TutorialText,
                fontSize = if (compact) 12.sp else 14.sp,
                fontWeight = FontWeight.Black
            )
            androidx.compose.material3.Text(
                content.description,
                color = TutorialMuted,
                fontSize = if (compact) 10.sp else 11.sp,
                lineHeight = if (compact) 13.sp else 15.sp,
                modifier = Modifier.offset(y = 5.dp)
            )
        }
        Row(
            modifier = Modifier.align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 6.dp, end = 6.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Text(
                content.progressLabel,
                color = TutorialAccent,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(content.detailSpacingDp.dp))
            androidx.compose.material3.Text(
                content.detailLabel,
                color = TutorialMuted,
                fontSize = 9.sp
            )
            Spacer(Modifier.weight(1f))
            androidx.compose.material3.Text(
                content.statusLabel,
                color = TutorialCodeGreen,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun Tutorial2Guidance(snapshot: Tutorial2Snapshot, isId: Boolean, compact: Boolean) {
    val objectiveTitle = when {
        snapshot.completed -> if (isId) "CHECKPOINT TERBUKA" else "CHECKPOINT UNLOCKED"
        !snapshot.sensorScanned -> if (isId) "TUJUAN 01 // BACA SENSOR" else "OBJECTIVE 01 // READ SENSOR"
        snapshot.robotX == 0 && snapshot.robotY == 2 -> if (isId) "TUJUAN 02 // PILIH JALUR AMAN" else "OBJECTIVE 02 // CHOOSE SAFE ROUTE"
        else -> if (isId) "TUJUAN 03 // BUKA CHECKPOINT" else "OBJECTIVE 03 // OPEN CHECKPOINT"
    }
    val description = when {
        snapshot.completed -> if (isId)
            "MX-01 berhasil membaca sensor, memilih jalur aman, dan membuka checkpoint. Programmu sudah mengambil keputusan berdasarkan data."
        else
            "MX-01 read the sensor, selected a safe route, and opened the checkpoint. Your program made a decision from live mission data."
        !snapshot.sensorScanned -> if (isId)
            "Baca sensor jarak dengan robot.scan_distance(). Simpan hasilnya ke variabel agar dapat diperiksa oleh program."
        else
            "Read the range sensor with robot.scan_distance(). Store its value in a variable so your program can inspect it."
        snapshot.robotX == 0 && snapshot.robotY == 2 -> if (isId)
            "Sensor membaca 18 cm. Ada penghalang di depan. Gunakan if/else: jika jarak lebih dari 30, pilih kanan; jika tidak, pilih jalur bawah."
        else
            "The sensor reads 18 cm. An obstacle is ahead. Use if/else: if distance is greater than 30, move right; otherwise, take the lower route."
        snapshot.robotX == 0 && snapshot.robotY == 3 -> if (isId)
            "Jalur bawah aman. Jalankan robot.action() untuk membuka checkpoint."
        else
            "The lower route is safe. Run robot.action() to unlock the checkpoint."
        snapshot.robotX == 1 && snapshot.robotY == 2 -> if (isId)
            "Jalur depan terbuka. Jalankan robot.action() untuk membuka checkpoint."
        else
            "The forward route is clear. Run robot.action() to unlock the checkpoint."
        else -> if (isId)
            "MX-01 belum berada di checkpoint aman. Periksa kembali arah gerak dan kondisi sensor."
        else
            "MX-01 is not at the safe checkpoint. Review the movement direction and sensor condition."
    }
    TutorialGuidancePanel(
        content = GuidancePanelContent(
            trainingLabel = "TRAINING 02",
            objectiveTitle = objectiveTitle,
            description = description,
            progressLabel = if (snapshot.completed) "OBJECTIVE 3/3" else if (snapshot.sensorScanned) "OBJECTIVE 2/3" else "OBJECTIVE 1/3",
            detailLabel = if (snapshot.sensorScanned) "RANGE ${snapshot.sensorDistance} CM" else "RANGE SENSOR STANDBY",
            detailSpacingDp = 10,
            statusLabel = if (snapshot.completed) "COMPLETE" else "MX-01 ONLINE"
        ),
        isId = isId,
        compact = compact
    )
}

private data class IsometricTilePoint(val x: Dp, val y: Dp)

private fun isometricTilePoint(column: Int, row: Int): IsometricTilePoint =
    IsometricTilePoint(
        x = (108 + (column - row) * 27).dp,
        y = ((column + row) * 13.5f + 38f).dp
    )

@Composable
private fun TutorialIsometricMap(
    map: List<String>,
    robotXPosition: Int,
    robotYPosition: Int,
    robotAction: String,
    robotDirectionRow: Int,
    rightWallOpeningRow: Int? = null,
    animationLabelPrefix: String,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {}
) {
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val mapScale = minOf(maxWidth.value / 270f, maxHeight.value / 187f).coerceIn(0.75f, 1.25f)
        Box(
            Modifier.size(width = 270.dp, height = 187.dp)
                .graphicsLayer(scaleX = mapScale, scaleY = mapScale)
        ) {
            val robotX by animateDpAsState(
                targetValue = ((robotXPosition - robotYPosition) * 27).dp,
                animationSpec = tween(durationMillis = 360),
                label = "${animationLabelPrefix}_robot_x"
            )
            val robotY by animateDpAsState(
                targetValue = ((robotXPosition + robotYPosition) * 13.5f + 38f).dp,
                animationSpec = tween(durationMillis = 360),
                label = "${animationLabelPrefix}_robot_y"
            )
            val cells = remember(map) {
                map.flatMapIndexed { row, tiles -> tiles.indices.map { col -> col to row } }
                    .sortedBy { it.first + it.second }
            }
            cells.forEach { (col, row) ->
                if (map.getOrNull(row)?.getOrNull(col) == '.') {
                    Image(
                        painter = painterResource(R.drawable.tile_floor_metal),
                        contentDescription = null,
                        modifier = Modifier.offset(
                            x = isometricTilePoint(col, row).x,
                            y = isometricTilePoint(col, row).y
                        ).size(width = 54.dp, height = 32.dp),
                        contentScale = ContentScale.FillBounds
                    )
                }
            }
            Image(
                painter = painterResource(R.drawable.ui_selection_marker),
                contentDescription = null,
                modifier = Modifier.offset(
                    x = 108.dp + robotX,
                    y = robotY
                ).size(width = 54.dp, height = 27.dp),
                contentScale = ContentScale.FillBounds
            )
            // Back and side room boundaries follow the same isometric tile anchors.
            val columnCount = map.firstOrNull()?.length ?: 0
            if (columnCount > 0 && map.isNotEmpty()) {
                Image(
                    painter = painterResource(R.drawable.tile_wall_corner),
                    contentDescription = null,
                    modifier = Modifier.offset(
                        x = isometricTilePoint(0, 0).x,
                        y = isometricTilePoint(0, 0).y - 34.dp
                   ).size(width = 54.dp, height = 40.dp),
                    contentScale = ContentScale.Fit
                )
                Image(
                    painter = painterResource(R.drawable.tile_wall_corner),
                    contentDescription = null,
                    modifier = Modifier.offset(
                        x = isometricTilePoint(columnCount - 1, 0).x,
                        y = isometricTilePoint(columnCount - 1, 0).y - 34.dp
                   ).size(width = 54.dp, height = 40.dp),
                    contentScale = ContentScale.Fit
                )
                for (column in 1 until columnCount - 1) {
                    val wallResource = if (column == (columnCount - 1) / 2) {
                        R.drawable.structure_window
                    } else {
                        R.drawable.tile_wall_straight
                    }
                    Image(
                        painter = painterResource(wallResource),
                        contentDescription = null,
                        modifier = Modifier.offset(
                            x = isometricTilePoint(column, 0).x,
                            y = isometricTilePoint(column, 0).y - 34.dp
                        ).size(width = 54.dp, height = 40.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                for (row in 1 until map.size) {
                    Image(
                        painter = painterResource(R.drawable.tile_wall_straight),
                        contentDescription = null,
                        modifier = Modifier.offset(
                            x = isometricTilePoint(0, row).x,
                            y = isometricTilePoint(0, row).y - 34.dp
                        ).size(width = 54.dp, height = 40.dp).graphicsLayer(scaleX = -1f),
                        contentScale = ContentScale.Fit
                    )
                    if (row != rightWallOpeningRow) {
                        Image(
                            painter = painterResource(R.drawable.tile_wall_straight),
                            contentDescription = null,
                            modifier = Modifier.offset(
                                x = isometricTilePoint(columnCount - 1, row).x,
                                y = isometricTilePoint(columnCount - 1, row).y - 34.dp
                            ).size(width = 54.dp, height = 40.dp).graphicsLayer(scaleX = -1f),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
            overlay()
            Box(
                Modifier.offset(
                    x = 108.dp + robotX - 8.dp,
                    y = robotY - 28.dp
                ).size(74.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .size(width = 30.dp, height = 10.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0x66000000),
                                    Color(0x33000000),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )
                TutorialRobotSprite(robotAction, robotDirectionRow, Modifier.size(72.dp))
            }
        }
    }
}

@Composable
private fun Tutorial2RoomScene(
    snapshot: Tutorial2Snapshot,
    robotAction: String,
    robotDirectionRow: Int,
    isId: Boolean,
    onToggleScenario: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 6.dp, end = 6.dp, top = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Text(
                if (isId) "02 / KORIDOR SENSOR" else "02 / SENSOR CORRIDOR",
                color = TutorialAccent, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 0.6.sp
            )
            Spacer(Modifier.weight(1f))
            androidx.compose.material3.Text(
                (if (snapshot.sensorDistance <= 30) "SIM // BLOCKED" else "SIM // CLEAR") + "  /  " +
                    if (snapshot.sensorScanned) "RANGE " + snapshot.sensorDistance + " CM" else "RANGE --",
                color = if (snapshot.sensorDistance <= 30) Color(0xFFFFA36B) else TutorialCodeGreen,
                fontSize = 9.sp,
                modifier = Modifier.clickable(onClick = onToggleScenario)
            )
        }
        TutorialIsometricMap(
            map = TrainingLevelCatalog.tutorial02.map,
            robotXPosition = snapshot.robotX,
            robotYPosition = snapshot.robotY,
            robotAction = robotAction,
            robotDirectionRow = robotDirectionRow,
            animationLabelPrefix = "tutorial2",
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            if (snapshot.sensorDistance <= 30) {
                Box(
                    Modifier.offset(
                        x = isometricTilePoint(1, 2).x + 8.dp,
                        y = isometricTilePoint(1, 2).y - 19.dp
                    ).size(width = 38.dp, height = 35.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xDD57252A)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Text(
                        "!",
                        color = Color(0xFFFF7777),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            TutorialDoorSprite(
                isOpen = snapshot.checkpointOpen,
                modifier = Modifier.offset(
                    x = if (snapshot.sensorDistance <= 30) {
                        isometricTilePoint(0, 3).x - 2.dp
                    } else {
                        isometricTilePoint(1, 2).x - 2.dp
                    },
                    y = isometricTilePoint(
                        if (snapshot.sensorDistance <= 30) 0 else 1,
                        if (snapshot.sensorDistance <= 30) 3 else 2
                    ).y - 42.dp
                ).size(58.dp)
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(start = 10.dp, end = 6.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(7.dp).background(TutorialAccent, RoundedCornerShape(4.dp)))
            androidx.compose.material3.Text(
                when {
                    snapshot.completed -> if (isId) " CHECKPOINT TERBUKA" else " CHECKPOINT OPEN"
                    snapshot.sensorScanned && snapshot.sensorDistance <= 30 -> if (isId) " PENGHALANG TERDETEKSI" else " OBSTACLE DETECTED"
                    snapshot.sensorScanned -> if (isId) " JALUR DEPAN TERBUKA" else " FORWARD ROUTE CLEAR"
                    else -> if (isId) " MX-01 MENUNGGU PEMINDAIAN" else " MX-01 AWAITING SCAN"
                },
                color = TutorialText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TutorialRunButton(
    label: String,
    enabled: Boolean,
    running: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier.size(width = 92.dp, height = 39.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(if (enabled) Color.Transparent else Color(0x77313B35))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        PrimaryButtonSprite(
            state = if (enabled) 0 else 3,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier.fillMaxSize().clip(RoundedCornerShape(5.dp))
                .background(if (enabled) Color.Transparent else Color(0x99050A08))
                .then(if (enabled) Modifier else Modifier)
        )
        androidx.compose.material3.Text(
            text = if (running) "..." else label,
            color = if (enabled) TutorialCodeGreen else TutorialMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )
        if (enabled) {
            Box(
                Modifier.fillMaxSize().clip(RoundedCornerShape(5.dp))
                    .background(Color.Transparent)
                    .then(Modifier)
            )
        }
    }
}

@Composable
private fun TutorialTerminalPanel(
    frame: Int,
    modifier: Modifier = Modifier,
    contentHorizontalInset: Float,
    contentVerticalInset: Float,
    background: Int? = null,
    backgroundHorizontalInset: Float = 0.075f,
    backgroundVerticalInset: Float = 0.165f,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier.clip(RoundedCornerShape(10.dp)).background(Color(0xFF101923))) {
        // The terminal frame is an opaque asset, so it must be drawn first.
        Image(
            painter = painterResource(frame), contentDescription = null,
            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds
        )
        // Place the network artwork over the frame, but only inside its display opening.
        if (background != null) {
            Image(
                painter = painterResource(background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().padding(
                    start = maxWidth * backgroundHorizontalInset,
                    end = maxWidth * backgroundHorizontalInset,
                    top = maxHeight * backgroundVerticalInset,
                    bottom = maxHeight * backgroundVerticalInset
                ),
                contentScale = ContentScale.FillBounds
            )
        }
        // Game scene and robot are the foreground content inside the terminal display.
        BoxWithConstraints(Modifier.fillMaxSize()) {
            Box(
                Modifier.fillMaxSize().padding(
                    start = maxWidth * contentHorizontalInset,
                    end = maxWidth * contentHorizontalInset,
                    top = maxHeight * contentVerticalInset,
                    bottom = maxHeight * contentVerticalInset
                )
            ) { content() }
        }
    }
}

@Composable
private fun TutorialGuidance(snapshot: TutorialSnapshot, isId: Boolean, compact: Boolean) {
    val objectiveTitle = when {
        snapshot.completed -> if (isId) "LANGKAH PERTAMA SELESAI" else "FIRST STEPS COMPLETE"
        !snapshot.energyCollected -> if (isId) "TUJUAN 01 // AMBIL ENERGI" else "OBJECTIVE 01 // COLLECT ENERGY"
        else -> if (isId) "TUJUAN 02 // BUKA PINTU" else "OBJECTIVE 02 // OPEN THE EXIT"
    }
    val description = when {
        snapshot.completed -> if (isId)
            "MX-01 berhasil keluar. Kamu sudah menjalankan perintah Python dan mengubah keadaan dunia permainan."
        else
            "MX-01 reached the exit. Your Python commands changed the state of the game world."
        !snapshot.energyCollected && snapshot.robotX < 2 -> if (isId)
            "Tulis robot.move(\"right\") lalu tekan RUN. Setiap perintah memindahkan robot satu petak ke kanan. Ulangi hingga berada di energy cell."
        else
            "Type robot.move(\"right\") and press RUN. Each command moves the robot one tile right. Repeat until it reaches the energy cell."
        !snapshot.energyCollected -> if (isId)
            "Robot berada di energy cell. Jalankan robot.action() untuk mengambilnya dan memulihkan baterai."
        else
            "The robot is on the energy cell. Run robot.action() to collect it and restore battery power."
        snapshot.robotX < 4 -> if (isId)
            "Energi sudah didapat. Jalankan robot.move(\"right\") dua kali untuk mencapai pintu keluar."
        else
            "Energy acquired. Run robot.move(\"right\") twice to reach the exit door."
        else -> if (isId)
            "Robot berada di depan pintu. Jalankan robot.action() untuk membuka pintu dan menyelesaikan latihan."
        else
            "The robot is at the exit. Run robot.action() to open the door and complete the training."
    }
    TutorialGuidancePanel(
        content = GuidancePanelContent(
            trainingLabel = "TRAINING 01",
            objectiveTitle = objectiveTitle,
            description = description,
            progressLabel = if (snapshot.energyCollected) "OBJECTIVE 2/2" else "OBJECTIVE 1/2",
            detailLabel = "MX-01  /  ${snapshot.battery}% BATTERY",
            detailSpacingDp = 14,
            statusLabel = if (snapshot.completed) "COMPLETE" else "PYTHON CONTROL"
        ),
        isId = isId,
        compact = compact
    )
}

@Composable
private fun TutorialRoomScene(snapshot: TutorialSnapshot, robotAction: String, robotDirectionRow: Int, isId: Boolean) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 6.dp, end = 6.dp, top = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Text(
                if (isId) "02 / RUANG ROBOT" else "02 / ROBOT ENVIRONMENT",
                color = TutorialAccent, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 0.6.sp
            )
            Spacer(Modifier.weight(1f))
            androidx.compose.material3.Text("ROOM 01  /  5 × 5", color = TutorialMuted, fontSize = 9.sp)
        }
        TutorialIsometricMap(
            map = TrainingLevelCatalog.tutorial01.map,
            robotXPosition = snapshot.robotX,
            robotYPosition = snapshot.robotY,
            robotAction = robotAction,
            robotDirectionRow = robotDirectionRow,
            rightWallOpeningRow = 2,
            animationLabelPrefix = "tutorial",
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            TutorialDoorSprite(
                isOpen = snapshot.exitOpen,
                modifier = Modifier.offset(
                    x = isometricTilePoint(4, 2).x - 2.dp,
                    y = isometricTilePoint(4, 2).y - 42.dp
                ).size(58.dp)
            )
            if (!snapshot.energyCollected) {
                // Keep the objective visually anchored to the same floor tile as the robot.
                Box(
                    Modifier.offset(
                        x = isometricTilePoint(2, 2).x + 12.dp,
                        y = isometricTilePoint(2, 2).y + 23.dp
                    ).size(width = 30.dp, height = 8.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xAA36E7F2),
                                    Color(0x5536E7F2),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )
                Image(
                    painter = painterResource(R.drawable.resource_energy_cell),
                    contentDescription = "Energy cell objective",
                    modifier = Modifier.offset(
                        x = isometricTilePoint(2, 2).x + 9.dp,
                        y = isometricTilePoint(2, 2).y - 13.dp
                    ).size(36.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(start = 10.dp, end = 6.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(7.dp).background(TutorialAccent, RoundedCornerShape(4.dp)))
            androidx.compose.material3.Text(
                if (snapshot.completed) " EXIT OPEN" else if (snapshot.energyCollected) " ENERGY ACQUIRED" else " MX-01 ONLINE",
                color = TutorialText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class SpriteSheetSpec(
    val rows: Int,
    val columns: Int,
    val frameCount: Int,
    val fps: Int,
    val loop: Boolean,
    val directionRow: Int = 0,
    val frameWidth: Int,
    val frameHeight: Int,
    val pivotX: Int? = null,
    val pivotY: Int? = null
)

private fun decodeSpriteFrames(
    context: android.content.Context,
    resource: Int,
    spec: SpriteSheetSpec
): List<Bitmap> {
    if (spec.rows <= 0 || spec.columns <= 0 || spec.frameCount <= 0) return emptyList()
    val sheet = BitmapFactory.decodeResource(context.resources, resource) ?: return emptyList()
    if (sheet.width % spec.columns != 0 || sheet.height % spec.rows != 0) {
        sheet.recycle()
        return emptyList()
    }

    val frameWidth = sheet.width / spec.columns
    val frameHeight = sheet.height / spec.rows
    if (frameWidth != spec.frameWidth || frameHeight != spec.frameHeight) {
        sheet.recycle()
        return emptyList()
    }

    val row = spec.directionRow.coerceIn(0, spec.rows - 1)
    val count = spec.frameCount.coerceAtMost(spec.columns)
    return try {
        (0 until count).map { index ->
            Bitmap.createBitmap(
                sheet,
                index * frameWidth,
                row * frameHeight,
                frameWidth,
                frameHeight
            )
        }
    } finally {
        sheet.recycle()
    }
}

@Composable
private fun SpriteSheetAnimation(
    resource: Int,
    spec: SpriteSheetSpec,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val frames = remember(resource, spec) { decodeSpriteFrames(context, resource, spec) }
    var frame by remember(resource, spec) { mutableIntStateOf(0) }

    LaunchedEffect(resource, spec, frames.size) {
        frame = 0
        if (frames.isNotEmpty()) {
            val frameDelay = (1000L / spec.fps.coerceAtLeast(1)).coerceAtLeast(1L)
            if (spec.loop) {
                while (true) {
                    delay(frameDelay)
                    frame = (frame + 1) % frames.size
                }
            } else {
                frames.indices.forEach { index ->
                    frame = index
                    if (index < frames.lastIndex) delay(frameDelay)
                }
            }
        }
    }

    DisposableEffect(frames) {
        onDispose { frames.forEach { bitmap -> if (!bitmap.isRecycled) bitmap.recycle() } }
    }

    if (frames.isNotEmpty()) {
        val pivotOffsetX = spec.pivotX?.let {
            (0.5f - it.toFloat() / spec.frameWidth) * 72f
        } ?: 0f
        val pivotOffsetY = spec.pivotY?.let {
            (0.5f - it.toFloat() / spec.frameHeight) * 72f
        } ?: 0f
        Image(
            bitmap = frames[frame.coerceIn(0, frames.lastIndex)].asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier.offset(x = pivotOffsetX.dp, y = pivotOffsetY.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun TutorialRobotSprite(action: String, directionRow: Int, modifier: Modifier = Modifier) {
    val (resource, fps, loop) = when (action) {
        "move" -> Triple(R.drawable.anim_robot_walk, 12, true)
        "interact" -> Triple(R.drawable.anim_robot_interact, 10, false)
        else -> Triple(R.drawable.anim_robot_idle, 6, true)
    }
    SpriteSheetAnimation(
        resource = resource,
        spec = SpriteSheetSpec(
            rows = 4,
            columns = 8,
            frameCount = 8,
            fps = fps,
            loop = loop,
            directionRow = directionRow.coerceIn(0, 3),
            frameWidth = 256,
            frameHeight = 256,
            pivotX = 128,
            pivotY = 206
        ),
        contentDescription = "MX-01 robot animation",
        modifier = modifier
    )
}

@Composable
private fun TutorialDoorSprite(isOpen: Boolean, modifier: Modifier = Modifier) {
    if (!isOpen) {
        Image(
            painter = painterResource(R.drawable.structure_door),
            contentDescription = "Closed exit",
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
        return
    }

    SpriteSheetAnimation(
        resource = R.drawable.anim_door_open,
        spec = SpriteSheetSpec(
            rows = 1,
            columns = 4,
            frameCount = 4,
            fps = 8,
            loop = false,
            frameWidth = 256,
            frameHeight = 256
        ),
        contentDescription = "Opening exit door",
        modifier = modifier
    )
}

private class PythonSyntaxHighlightTransformation : VisualTransformation {
    private val tokens = Regex(
        """#[^\n]*|"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|\b(?:def|class|for|in|if|elif|else|while|return|True|False|None|and|or|not|import|from|as|pass|break|continue|try|except|with|lambda|assert|raise|finally|yield|global|nonlocal|is|del|async|await)\b|\b\d+(?:\.\d+)?\b|\b[A-Za-z_][A-Za-z0-9_]*(?=\s*\()"""
    )
    private val keywords = setOf(
        "def", "class", "for", "in", "if", "elif", "else", "while", "return",
        "True", "False", "None", "and", "or", "not", "import", "from", "as",
        "pass", "break", "continue", "try", "except", "with", "lambda",
        "assert", "raise", "finally", "yield", "global", "nonlocal", "is",
        "del", "async", "await"
    )

    override fun filter(text: AnnotatedString): TransformedText {
        val styled = androidx.compose.ui.text.buildAnnotatedString {
            append(text.text)
            tokens.findAll(text.text).forEach { match ->
                val token = match.value
                val color = when {
                    token.startsWith("#") -> Color(0xFF7F8C98)
                    token.startsWith("\"") || token.startsWith("'") -> Color(0xFF9BE2A8)
                    token in keywords -> Color(0xFFC792EA)
                    token.firstOrNull()?.isDigit() == true -> Color(0xFFE5C07B)
                    else -> Color(0xFF61AFEF)
                }
                addStyle(SpanStyle(color = color), match.range.first, match.range.last + 1)
            }
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}
