package com.zeroday.cyberprotocol.data

import android.content.Context

/** Preferences for optional UI feedback, stored independently from campaign progress. */
object GameSettingsPreferences {
    private const val STORE_NAME = "cyber_protocol_settings_v2"
    private const val KEY_SOUND = "sound_enabled"
    private const val KEY_VIBRATION = "vibration_enabled"

    fun soundEnabled(context: Context): Boolean =
        context.applicationContext.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SOUND, true)

    fun vibrationEnabled(context: Context): Boolean =
        context.applicationContext.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_VIBRATION, true)

    fun saveSound(context: Context, enabled: Boolean) {
        context.applicationContext.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_SOUND, enabled).apply()
    }

    fun saveVibration(context: Context, enabled: Boolean) {
        context.applicationContext.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_VIBRATION, enabled).apply()
    }
}
