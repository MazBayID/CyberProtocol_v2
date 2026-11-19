package com.zeroday.cyberprotocol.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.chaquo.python.Python
import com.zeroday.cyberprotocol.core.model.GameLanguage
import com.zeroday.cyberprotocol.data.GameLanguagePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CyberProtocolApp() {
    val context = LocalContext.current
    val startupLanguage = remember { GameLanguagePreferences.load(context) }
    var startupReady by remember { mutableStateOf(false) }
    var startupMinimumElapsed by remember { mutableStateOf(false) }
    var startupError by remember { mutableStateOf<String?>(null) }
    var startupAttempt by remember { mutableIntStateOf(0) }

    LaunchedEffect(startupAttempt) {
        startupMinimumElapsed = false
        launch {
            delay(5_000L)
            startupMinimumElapsed = true
        }
        runCatching {
            withContext(Dispatchers.IO) {
                Python.getInstance()
            }
        }.onSuccess {
            startupReady = true
        }.onFailure {
            startupError = it.message ?: "Inisialisasi game gagal."
        }
    }

    if (!startupReady || !startupMinimumElapsed) {
        EntryLoadingScreen(
            selectedLanguage = startupLanguage,
            ready = startupReady && startupMinimumElapsed,
            error = if (startupMinimumElapsed) startupError else null,
            onRetry = {
                startupReady = false
                startupError = null
                startupAttempt += 1
            }
        )
        return
    }

    CyberProtocolStartExperience()
}
