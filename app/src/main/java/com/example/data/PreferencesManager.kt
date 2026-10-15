package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class AngleMode {
    DEG, RAD, GRAD
}

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("smart_calc_prefs", Context.MODE_PRIVATE)

    // StateFlows for reactive Compose observations
    private val _themeMode = MutableStateFlow(getSavedThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _amoledBlack = MutableStateFlow(prefs.getBoolean(KEY_AMOLED_MODE, false))
    val amoledBlack: StateFlow<Boolean> = _amoledBlack.asStateFlow()
    val amoledMode: StateFlow<Boolean> = _amoledBlack.asStateFlow()

    private val _hapticFeedback = MutableStateFlow(prefs.getBoolean(KEY_HAPTIC, true))
    val hapticFeedback: StateFlow<Boolean> = _hapticFeedback.asStateFlow()

    private val _soundFeedback = MutableStateFlow(prefs.getBoolean(KEY_SOUND, false))
    val soundFeedback: StateFlow<Boolean> = _soundFeedback.asStateFlow()

    private val _decimalPrecision = MutableStateFlow(prefs.getInt(KEY_PRECISION, 4))
    val decimalPrecision: StateFlow<Int> = _decimalPrecision.asStateFlow()

    private val _thousandsSeparator = MutableStateFlow(prefs.getBoolean(KEY_THOUSANDS, true))
    val thousandsSeparator: StateFlow<Boolean> = _thousandsSeparator.asStateFlow()

    private val _defaultAngleMode = MutableStateFlow(getSavedAngleMode())
    val defaultAngleMode: StateFlow<AngleMode> = _defaultAngleMode.asStateFlow()

    private val _autoSaveCalculations = MutableStateFlow(prefs.getBoolean(KEY_AUTO_SAVE, true))
    val autoSaveCalculations: StateFlow<Boolean> = _autoSaveCalculations.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString(KEY_USER_NAME, "User") ?: "User")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userRole = MutableStateFlow(prefs.getString(KEY_USER_ROLE, "Student") ?: "Student")
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _userAvatarUri = MutableStateFlow(prefs.getString(KEY_AVATAR_URI, null))
    val userAvatarUri: StateFlow<String?> = _userAvatarUri.asStateFlow()

    private val _quickNotes = MutableStateFlow(prefs.getString(KEY_QUICK_NOTES, "") ?: "")
    val quickNotes: StateFlow<String> = _quickNotes.asStateFlow()

    private val _favoriteTools = MutableStateFlow(getFavoritesSet())
    val favoriteTools: StateFlow<Set<String>> = _favoriteTools.asStateFlow()
    val favorites: StateFlow<Set<String>> = _favoriteTools.asStateFlow()

    private val _recentTools = MutableStateFlow(getRecentToolsList())
    val recentTools: StateFlow<List<String>> = _recentTools.asStateFlow()

    private val _proSupporter = MutableStateFlow(prefs.getBoolean(KEY_PRO_SUPPORTER, false))
    val proSupporter: StateFlow<Boolean> = _proSupporter.asStateFlow()

    private val _rewardCredits = MutableStateFlow(prefs.getInt(KEY_REWARD_CREDITS, 100))
    val rewardCredits: StateFlow<Int> = _rewardCredits.asStateFlow()

    private val _appLanguage = MutableStateFlow(prefs.getString(KEY_LANGUAGE, "en") ?: "en")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    fun setAppLanguage(lang: String) {
        _appLanguage.value = lang
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
    }

    private fun getSavedThemeMode(): ThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(name ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    private fun getSavedAngleMode(): AngleMode {
        val name = prefs.getString(KEY_ANGLE_MODE, AngleMode.DEG.name)
        return try {
            AngleMode.valueOf(name ?: AngleMode.DEG.name)
        } catch (e: Exception) {
            AngleMode.DEG
        }
    }

    private fun getFavoritesSet(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, setOf("emi_calculator", "gst_calculator", "sip_calculator", "unit_converter"))
            ?: setOf("emi_calculator", "gst_calculator", "sip_calculator", "unit_converter")
    }

    private fun getRecentToolsList(): List<String> {
        val raw = prefs.getString(KEY_RECENT_TOOLS, "emi_calculator,gst_calculator,age_calculator") ?: "emi_calculator,gst_calculator,age_calculator"
        return raw.split(",").filter { it.isNotBlank() }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun setAmoledBlack(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AMOLED_MODE, enabled).apply()
        _amoledBlack.value = enabled
    }

    fun setHapticFeedback(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()
        _hapticFeedback.value = enabled
    }

    fun setSoundFeedback(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
        _soundFeedback.value = enabled
    }

    fun setDecimalPrecision(precision: Int) {
        prefs.edit().putInt(KEY_PRECISION, precision.coerceIn(0, 10)).apply()
        _decimalPrecision.value = precision.coerceIn(0, 10)
    }

    fun setThousandsSeparator(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_THOUSANDS, enabled).apply()
        _thousandsSeparator.value = enabled
    }

    fun setDefaultAngleMode(mode: AngleMode) {
        prefs.edit().putString(KEY_ANGLE_MODE, mode.name).apply()
        _defaultAngleMode.value = mode
    }

    fun setAutoSaveCalculations(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SAVE, enabled).apply()
        _autoSaveCalculations.value = enabled
    }

    fun setUserName(name: String) {
        val trimmed = name.trim().ifEmpty { "User" }
        prefs.edit().putString(KEY_USER_NAME, trimmed).apply()
        _userName.value = trimmed
    }

    fun setUserRole(role: String) {
        val trimmed = role.trim().ifEmpty { "User" }
        prefs.edit().putString(KEY_USER_ROLE, trimmed).apply()
        _userRole.value = trimmed
    }

    fun setUserAvatarUri(uriString: String?) {
        prefs.edit().putString(KEY_AVATAR_URI, uriString).apply()
        _userAvatarUri.value = uriString
    }

    fun setQuickNotes(notes: String) {
        prefs.edit().putString(KEY_QUICK_NOTES, notes).apply()
        _quickNotes.value = notes
    }

    fun toggleFavorite(toolId: String) {
        val current = _favoriteTools.value.toMutableSet()
        if (current.contains(toolId)) {
            current.remove(toolId)
        } else {
            current.add(toolId)
        }
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
        _favoriteTools.value = current
    }

    fun addRecentTool(toolId: String) {
        val current = _recentTools.value.toMutableList()
        current.remove(toolId)
        current.add(0, toolId)
        val trimmed = current.take(6)
        prefs.edit().putString(KEY_RECENT_TOOLS, trimmed.joinToString(",")).apply()
        _recentTools.value = trimmed
    }

    fun setProSupporter(isPro: Boolean) {
        prefs.edit().putBoolean(KEY_PRO_SUPPORTER, isPro).apply()
        _proSupporter.value = isPro
    }

    fun addRewardCredits(amount: Int) {
        val newCredits = _rewardCredits.value + amount
        prefs.edit().putInt(KEY_REWARD_CREDITS, newCredits).apply()
        _rewardCredits.value = newCredits
    }

    companion object {
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_AMOLED_MODE = "pref_amoled_mode"
        private const val KEY_HAPTIC = "pref_haptic"
        private const val KEY_SOUND = "pref_sound"
        private const val KEY_PRECISION = "pref_precision"
        private const val KEY_THOUSANDS = "pref_thousands"
        private const val KEY_ANGLE_MODE = "pref_angle_mode"
        private const val KEY_AUTO_SAVE = "pref_auto_save"
        private const val KEY_USER_NAME = "pref_user_name"
        private const val KEY_USER_ROLE = "pref_user_role"
        private const val KEY_AVATAR_URI = "pref_avatar_uri"
        private const val KEY_QUICK_NOTES = "pref_quick_notes"
        private const val KEY_FAVORITES = "pref_favorites"
        private const val KEY_RECENT_TOOLS = "pref_recent_tools"
        private const val KEY_PRO_SUPPORTER = "pref_pro_supporter"
        private const val KEY_REWARD_CREDITS = "pref_reward_credits"
        private const val KEY_LANGUAGE = "pref_language"
    }
}
