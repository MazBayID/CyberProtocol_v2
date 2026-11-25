package com.zeroday.cyberprotocol.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.zeroday.cyberprotocol.ui.hud.HudFrame
import com.zeroday.cyberprotocol.ui.hud.HudFrameVariant
import com.zeroday.cyberprotocol.ui.iso.IsoMetrics
import com.zeroday.cyberprotocol.ui.iso.IsoProjection
import com.zeroday.cyberprotocol.ui.iso.IsoProp
import com.zeroday.cyberprotocol.ui.iso.IsoPropKind
import com.zeroday.cyberprotocol.ui.iso.IsoRobot
import com.zeroday.cyberprotocol.ui.iso.IsoRoom

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
    var robotDirectionRow by remember { mutableIntStateOf(IsoMetrics.directionRow(IsoProjection.COL_UP_RIGHT, 1, 0, 2)) }
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
                            robotDirectionRow = IsoMetrics.directionRow(
                                IsoProjection.COL_UP_RIGHT,
                                nextX - snapshot.robotX,
                                nextY - snapshot.robotY,
                                robotDirectionRow
                            )
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
                variant = HudFrameVariant.CLASSIC,
                modifier = Modifier.fillMaxWidth().weight(0.29f),
            ) { TutorialGuidance(snapshot, isId, compact) }
            TutorialTerminalPanel(
                variant = HudFrameVariant.TERMINAL,
                background = R.drawable.bg_digital_network,
                modifier = Modifier.fillMaxWidth().weight(0.40f),
            ) { TutorialRoomScene(snapshot, robotAction, robotDirectionRow, isId) }
            TutorialTerminalPanel(
                variant = HudFrameVariant.EDITOR,
                modifier = Modifier.fillMaxWidth().weight(0.31f),
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
    var robotDirectionRow by remember { mutableIntStateOf(IsoMetrics.directionRow(IsoProjection.COL_UP_RIGHT, 1, 0, 2)) }
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
                            robotDirectionRow = IsoMetrics.directionRow(
                                IsoProjection.COL_UP_RIGHT,
                                nextX - snapshot.robotX,
                                nextY - snapshot.robotY,
                                robotDirectionRow
                            )
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
                variant = HudFrameVariant.CLASSIC,
                modifier = Modifier.fillMaxWidth().weight(0.29f),
            ) { Tutorial2Guidance(snapshot, isId, compact) }
            TutorialTerminalPanel(
                variant = HudFrameVariant.TERMINAL,
                background = R.drawable.bg_digital_network,
                modifier = Modifier.fillMaxWidth().weight(0.40f),
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
                variant = HudFrameVariant.EDITOR,
                modifier = Modifier.fillMaxWidth().weight(0.31f),
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
        val blocked = snapshot.sensorDistance <= 30
        IsoRoom(
            map = TrainingLevelCatalog.tutorial02.map,
            robot = IsoRobot(snapshot.robotX, snapshot.robotY, robotAction, robotDirectionRow),
            props = if (blocked) listOf(IsoProp(IsoPropKind.CRATE, 1, 2)) else emptyList(),
            goalDoors = listOf(if (blocked) Pair(0, 3) else Pair(1, 2)),
            goalDoorOpen = snapshot.checkpointOpen,
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
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
    variant: HudFrameVariant,
    modifier: Modifier = Modifier,
    background: Int? = null,
    content: @Composable () -> Unit
) {
    HudFrame(variant, modifier) {
        // Network artwork fills the display opening without being stretched out of proportion.
        if (background != null) {
            Image(
                painter = painterResource(background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        // Game scene and robot are the foreground content inside the terminal display.
        content()
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
        IsoRoom(
            map = TrainingLevelCatalog.tutorial01.map,
            robot = IsoRobot(snapshot.robotX, snapshot.robotY, robotAction, robotDirectionRow),
            props = if (snapshot.energyCollected) emptyList() else listOf(IsoProp(IsoPropKind.ENERGY_CELL, 2, 2)),
            goalDoors = listOf(Pair(4, 2)),
            goalDoorOpen = snapshot.exitOpen,
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
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
