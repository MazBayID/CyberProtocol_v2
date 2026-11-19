package com.zeroday.cyberprotocol.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.view.SoundEffectConstants
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.sp
import com.zeroday.cyberprotocol.R
import com.zeroday.cyberprotocol.core.level.CampaignProgress
import com.zeroday.cyberprotocol.core.level.LevelDefinition
import com.zeroday.cyberprotocol.core.level.TrainingLevelCatalog
import com.zeroday.cyberprotocol.core.model.GameLanguage
import com.zeroday.cyberprotocol.data.CampaignProgressPreferences
import com.zeroday.cyberprotocol.data.GameLanguagePreferences
import com.zeroday.cyberprotocol.data.GameSettingsPreferences

internal val DepartureMonoFamily = FontFamily(Font(R.font.departure_mono))

internal fun playGameUiFeedback(context: Context, view: View, haptic: HapticFeedback) {
    if (GameSettingsPreferences.soundEnabled(context)) {
        view.playSoundEffect(SoundEffectConstants.CLICK)
    }
    if (GameSettingsPreferences.vibrationEnabled(context)) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}

private enum class EntryScreen { MENU, SETTINGS, CAMPAIGN, INTRODUCTION, GAMEPLAY, TUTORIAL_2 }

@Composable
fun CyberProtocolStartExperience() {
    val context = LocalContext.current
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    var screen by remember { mutableStateOf(EntryScreen.MENU) }
    var language by remember { mutableStateOf(GameLanguagePreferences.load(context)) }
    var soundEnabled by remember { mutableStateOf(GameSettingsPreferences.soundEnabled(context)) }
    var vibrationEnabled by remember { mutableStateOf(GameSettingsPreferences.vibrationEnabled(context)) }
    var campaignProgress by remember { mutableStateOf(CampaignProgressPreferences.load(context)) }
    val isId = language == GameLanguage.INDONESIAN

    BackHandler(enabled = screen != EntryScreen.MENU) {
        screen = when (screen) {
            EntryScreen.GAMEPLAY, EntryScreen.TUTORIAL_2, EntryScreen.INTRODUCTION -> EntryScreen.CAMPAIGN
            EntryScreen.SETTINGS, EntryScreen.CAMPAIGN -> EntryScreen.MENU
            EntryScreen.MENU -> EntryScreen.MENU
        }
    }

    CompositionLocalProvider(LocalTextStyle provides TextStyle(fontFamily = DepartureMonoFamily)) {
        when (screen) {
            EntryScreen.MENU -> StartMenuScreen(
                isId = isId,
                onPlay = {
                    playGameUiFeedback(context, view, haptic)
                    campaignProgress = CampaignProgressPreferences.load(context)
                    screen = EntryScreen.CAMPAIGN
                },
                onSettings = {
                    playGameUiFeedback(context, view, haptic)
                    screen = EntryScreen.SETTINGS
                }
            )
            EntryScreen.SETTINGS -> SettingsScreen(
                isId = isId,
                language = language,
                soundEnabled = soundEnabled,
                vibrationEnabled = vibrationEnabled,
                onLanguageChange = {
                    language = it
                    GameLanguagePreferences.save(context, it)
                    playGameUiFeedback(context, view, haptic)
                },
                onSoundChange = {
                    soundEnabled = it
                    GameSettingsPreferences.saveSound(context, it)
                    playGameUiFeedback(context, view, haptic)
                },
                onVibrationChange = {
                    vibrationEnabled = it
                    GameSettingsPreferences.saveVibration(context, it)
                    if (it) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onBack = {
                    playGameUiFeedback(context, view, haptic)
                    screen = EntryScreen.MENU
                }
            )
            EntryScreen.CAMPAIGN -> CampaignSelectionScreen(
                isId = isId,
                progress = campaignProgress,
                onBack = {
                    playGameUiFeedback(context, view, haptic)
                    screen = EntryScreen.MENU
                },
                onSelectLevel = { levelId ->
                    playGameUiFeedback(context, view, haptic)
                    screen = if (levelId == "tutorial_01") EntryScreen.INTRODUCTION else EntryScreen.TUTORIAL_2
                }
            )
            EntryScreen.INTRODUCTION -> IntroductionScreen(
                isId = isId,
                onBack = {
                    playGameUiFeedback(context, view, haptic)
                    screen = EntryScreen.CAMPAIGN
                },
                onBegin = {
                    playGameUiFeedback(context, view, haptic)
                    screen = EntryScreen.GAMEPLAY
                }
            )
            EntryScreen.GAMEPLAY, EntryScreen.TUTORIAL_2 -> {
                if (screen == EntryScreen.GAMEPLAY) {
                    CyberProtocolTutorialExperience(
                        onTutorial2 = { screen = EntryScreen.TUTORIAL_2 },
                        onLevelCompleted = {
                            CampaignProgressPreferences.markCompleted(context, "tutorial_01")
                            campaignProgress = CampaignProgressPreferences.load(context)
                        }
                    )
                } else {
                    CyberProtocolTutorial2Experience(
                        onLevelCompleted = {
                            CampaignProgressPreferences.markCompleted(context, "tutorial_02")
                            campaignProgress = CampaignProgressPreferences.load(context)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CampaignSelectionScreen(
    isId: Boolean,
    progress: CampaignProgress,
    onBack: () -> Unit,
    onSelectLevel: (String) -> Unit
) {
    val levels = TrainingLevelCatalog.all.sortedBy { it.sequence }
    val completedCount = levels.count { progress.isCompleted(it.id) }

    EntryBackground(R.drawable.bg_main_menu) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                if (isId) "CAMPAIGN // PILIH MISI" else "CAMPAIGN // SELECT MISSION",
                color = Color(0xFF8BFFA0),
                fontSize = 21.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                if (isId) "PROGRES: $completedCount / ${levels.size} MISI SELESAI"
                else "PROGRESS: $completedCount / ${levels.size} MISSIONS COMPLETE",
                color = Color(0xFFB9C9C0),
                fontSize = 10.sp
            )
            EntryTerminalPanel(
                Modifier.widthIn(max = 520.dp).fillMaxWidth().weight(1f)
            ) {
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    levels.forEach { level ->
                        CampaignLevelCard(
                            level = level,
                            isId = isId,
                            completed = progress.isCompleted(level.id),
                            unlocked = progress.isUnlocked(level.id, levels),
                            onClick = { onSelectLevel(level.id) }
                        )
                    }
                }
            }
            EntryMenuButton(
                if (isId) "<< KEMBALI" else "<< BACK",
                "",
                onBack
            )
        }
    }
}

@Composable
private fun CampaignLevelCard(
    level: LevelDefinition,
    isId: Boolean,
    completed: Boolean,
    unlocked: Boolean,
    onClick: () -> Unit
) {
    val status = when {
        completed -> if (isId) "SELESAI" else "COMPLETED"
        unlocked -> if (isId) "TERBUKA" else "UNLOCKED"
        else -> if (isId) "TERKUNCI" else "LOCKED"
    }
    val accent = when {
        completed -> Color(0xFF8BFFA0)
        unlocked -> Color(0xFF00E5FF)
        else -> Color(0xFF71808A)
    }
    Box(
        Modifier.fillMaxWidth()
            .height(112.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(Color(0xAA101B22))
            .clickable(enabled = unlocked, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "0${level.sequence}",
                    color = accent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    if (isId) level.titleId else level.titleEn,
                    color = Color(0xFFE8F5EA),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(status, color = accent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                when (level.id) {
                    "tutorial_01" -> if (isId) "Kendalikan MX-01 dan capai pintu keluar." else "Control MX-01 and reach the exit."
                    "tutorial_02" -> if (isId) "Baca sensor dan pilih tindakan robot." else "Read sensors and choose the robot's action."
                    else -> if (isId) "Misi campaign" else "Campaign mission"
                },
                color = Color(0xFFB9C9C0),
                fontSize = 9.sp
            )
            if (!unlocked) {
                Text(
                    if (isId) "Selesaikan misi sebelumnya untuk membuka." else "Complete the previous mission to unlock.",
                    color = Color(0xFF71808A),
                    fontSize = 8.sp
                )
            } else if (completed) {
                Text(
                    if (isId) "Pilih untuk mengulang misi." else "Select to replay this mission.",
                    color = Color(0xFF8BFFA0),
                    fontSize = 8.sp
                )
            }
        }
    }
}

@Composable
private fun StartMenuScreen(isId: Boolean, onPlay: () -> Unit, onSettings: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0xFF050B10))) {
        Image(
            painter = painterResource(R.drawable.bg_main_menu),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color(0xAA03080D),
                    0.48f to Color(0x55101A20),
                    1f to Color(0xDD03080D)
                )
            )
        )
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.cp_logo),
                    contentDescription = "Cyber Protocol emblem",
                    modifier = Modifier.size(88.dp)
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    "CYBER PROTOCOL",
                    color = Color(0xFFE8F5EA),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    "SYSTEMS ONLINE  //  TRAINING BUILD",
                    color = Color(0xFF71F08A),
                    fontSize = 10.sp,
                    letterSpacing = 1.3.sp
                )
            }
            Column(
                Modifier.widthIn(max = 360.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EntryMenuButton(
                    label = "PLAY >>",
                    description = if (isId) "MULAI PERMAINAN" else "START GAMEPLAY",
                    onClick = onPlay
                )
                EntryMenuButton(
                    label = "SETTINGS >>",
                    description = if (isId) "BAHASA / SUARA / GETARAN" else "LANGUAGE / SOUND / VIBRATION",
                    onClick = onSettings
                )
            }
            Text(
                "KNOWLEDGE IS POWER.  INTELLIGENCE IS RESPONSIBILITY.",
                color = Color(0xFFB9C9C0),
                fontSize = 9.sp,
                letterSpacing = 0.6.sp
            )
        }
    }
}

@Composable
private fun EntryMenuButton(label: String, description: String, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(66.dp).clip(RoundedCornerShape(7.dp)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        PrimaryButtonSprite(state = 0, modifier = Modifier.fillMaxSize())
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(label, color = Color(0xFF8BFFA0), fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text(description, color = Color(0xFFE5EEE7), fontSize = 9.sp, letterSpacing = 0.7.sp)
        }
    }
}


@Composable
internal fun PrimaryButtonSprite(state: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.ui_button_primary_states)
    }
    DisposableEffect(bitmap) {
        onDispose { bitmap?.recycle() }
    }
    Canvas(modifier) {
        bitmap?.let { sheet ->
            val frameWidth = sheet.width / 4
            drawImage(
                image = sheet.asImageBitmap(),
                srcOffset = IntOffset(state.coerceIn(0, 3) * frameWidth, 0),
                srcSize = IntSize(frameWidth, sheet.height),
                dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                filterQuality = FilterQuality.Medium
            )
        }
    }
}

@Composable
private fun SettingsScreen(
    isId: Boolean,
    language: GameLanguage,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    onLanguageChange: (GameLanguage) -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    EntryBackground(R.drawable.bg_main_menu) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("SETTINGS", color = Color(0xFF8BFFA0), fontSize = 23.sp, fontWeight = FontWeight.Black)
            EntryTerminalPanel(Modifier.widthIn(max = 430.dp).fillMaxWidth().weight(1f)) {
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(if (isId) "BAHASA / LANGUAGE" else "LANGUAGE", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        LanguageChoice("ENGLISH", language == GameLanguage.ENGLISH) { onLanguageChange(GameLanguage.ENGLISH) }
                        LanguageChoice("BAHASA", language == GameLanguage.INDONESIAN) { onLanguageChange(GameLanguage.INDONESIAN) }
                    }
                    EntrySettingToggle(
                        title = if (isId) "SUARA" else "SOUND",
                        subtitle = if (isId) "Efek suara antarmuka" else "Interface sound effects",
                        checked = soundEnabled,
                        onCheckedChange = onSoundChange
                    )
                    EntrySettingToggle(
                        title = if (isId) "GETARAN" else "VIBRATION",
                        subtitle = if (isId) "Umpan balik sentuhan" else "Haptic feedback",
                        checked = vibrationEnabled,
                        onCheckedChange = onVibrationChange
                    )
                }
            }
            EntryMenuButton(if (isId) "<< KEMBALI" else "<< BACK", "", onBack)
        }
    }
}

@Composable
private fun LanguageChoice(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.width(140.dp).height(42.dp).clip(RoundedCornerShape(5.dp))
            .background(if (selected) Color(0xFF164B2A) else Color(0x88202C30))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) Color(0xFF9BFFAA) else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EntrySettingToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color(0xFFB9C9C0), fontSize = 10.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF06120A),
                checkedTrackColor = Color(0xFF73E98A),
                uncheckedThumbColor = Color(0xFFB6C1B8),
                uncheckedTrackColor = Color(0xFF34423A)
            )
        )
    }
}

private fun wrapIntroLines(text: String, maxChars: Int = 37): String {
    val lines = mutableListOf<String>()
    var line = ""
    text.split(Regex("\\s+")).forEach { word ->
        if (line.isEmpty()) {
            line = word
        } else if (line.length + 1 + word.length <= maxChars) {
            line += " $word"
        } else {
            lines += line
            line = word
        }
    }
    if (line.isNotEmpty()) lines += line
    return lines.joinToString("\n")
}

@Composable
private fun IntroductionScreen(isId: Boolean, onBack: () -> Unit, onBegin: () -> Unit) {
    val introPurple = Color(0xFFC792EA)
    val statusLine = wrapIntroLines("UNIT: MX-01  //  STATUS: ONLINE")
    val description = wrapIntroLines(
        if (isId)
            "Kamu terhubung ke unit robot MX-01 di ruang latihan. Robot hanya akan bergerak ketika program Python yang kamu tulis dijalankan."
        else
            "You are connected to robot unit MX-01 inside a training room. The robot moves only when the Python program you write is executed."
    )
    val objective = wrapIntroLines(
        if (isId)
            "Tugas pertama: ambil energy cell untuk memulihkan baterai, lalu buka pintu keluar. Tulis perintah di terminal bawah dan tekan RUN."
        else
            "First objective: collect the energy cell to restore the battery, then open the exit. Write commands in the lower terminal and press RUN."
    )
    val controls = wrapIntroLines(
        if (isId) "KONTROL: KODE PYTHON  /  BUKAN TOMBOL ARAH"
        else "CONTROL: PYTHON CODE  /  NO DIRECTIONAL BUTTONS"
    )
    val messages = listOf(statusLine, description, objective, controls)
    val totalCharacters = messages.sumOf { it.length }
    var printedCharacters by remember(isId) { mutableStateOf(0) }

    LaunchedEffect(isId) {
        printedCharacters = 0
        for (count in 1..totalCharacters) {
            delay(24)
            printedCharacters = count
        }
    }

    EntryBackground(R.drawable.bg_gameplay_facility) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text(
                if (isId) "BOOT SEQUENCE // LATIHAN 001" else "BOOT SEQUENCE // TRAINING 001",
                color = Color(0xFF00E5FF),
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            )
            EntryTerminalPanel(Modifier.widthIn(max = 520.dp).fillMaxWidth().weight(1f)) {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    var characterOffset = 0
                    messages.forEachIndexed { index, message ->
                        val start = characterOffset
                        characterOffset += message.length
                        val visibleCount = (printedCharacters - start).coerceIn(0, message.length)
                        if (visibleCount > 0) {
                            Text(
                                message.take(visibleCount),
                                modifier = Modifier.fillMaxWidth(),
                                color = introPurple,
                                fontSize = when (index) {
                                    0 -> 13.sp
                                    3 -> 11.sp
                                    else -> 14.sp
                                },
                                lineHeight = if (index == 0 || index == 3) 18.sp else 21.sp,
                                fontWeight = if (index == 0 || index == 3) FontWeight.Bold else FontWeight.Normal,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            if (index < messages.lastIndex) Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }
            Row(
                Modifier.widthIn(max = 520.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(Modifier.weight(1f)) { EntryMenuButton(if (isId) "<< KEMBALI" else "<< BACK", "", onBack) }
                Box(Modifier.weight(1f)) { EntryMenuButton(if (isId) "MULAI >>" else "BEGIN >>", "", onBegin) }
            }
        }
    }
}

@Composable
private fun EntryBackground(resource: Int, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0xFF050B10))) {
        Image(
            painter = painterResource(resource),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(Modifier.fillMaxSize().background(Color(0xA8071015)))
        content()
    }
}

@Composable
private fun EntryTerminalPanel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    BoxWithConstraints(modifier.clip(RoundedCornerShape(10.dp))) {
        Image(
            painter = painterResource(R.drawable.ui_terminal_frame_classic),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Box(
            Modifier.fillMaxSize().padding(
                start = maxWidth * 0.10f,
                end = maxWidth * 0.10f,
                top = maxHeight * 0.16f,
                bottom = maxHeight * 0.16f
            )
        ) { content() }
    }
}
