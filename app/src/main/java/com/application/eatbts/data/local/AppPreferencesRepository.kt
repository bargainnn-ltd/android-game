package com.application.eatbts.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "truth_or_dare_prefs")

private val JsonLenient = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/** Default intensity: 0 mild, 1 spicy, 2 extreme — matches Settings UI. */
enum class DefaultIntensity(val storageValue: Int) {
    MILD(0),
    SPICY(1),
    EXTREME(2),
}

/** App theme from Settings. */
enum class AppThemePreference {
    MIDNIGHT,
    LIGHT,
}

class AppPreferencesRepository(private val context: Context) {

    private object Keys {
        val disclaimerAccepted = booleanPreferencesKey("disclaimer_accepted")
        val ageVerified = booleanPreferencesKey("age_verified")
        val extremeUnlocked = booleanPreferencesKey("extreme_unlocked")
        val climaxUnlockedLegacy = booleanPreferencesKey("climax_unlocked")
        val favoritesJson = stringPreferencesKey("favorites_json")

        val defaultIntensity = intPreferencesKey("default_intensity")
        val categoryRomance = booleanPreferencesKey("category_romance")
        val categoryPartyDrinking = booleanPreferencesKey("category_party_drinking")
        val categoryNsfw = booleanPreferencesKey("category_nsfw")
        val turnTimerSeconds = intPreferencesKey("turn_timer_seconds")
        val soundEffectsEnabled = booleanPreferencesKey("sound_effects_enabled")
        val hapticFeedbackEnabled = booleanPreferencesKey("haptic_feedback_enabled")
        val appTheme = stringPreferencesKey("app_theme")
        val languageTag = stringPreferencesKey("language_tag")
        val sessionPlayerNamesJson = stringPreferencesKey("session_player_names_json")
    }

    val disclaimerAccepted: Flow<Boolean> = context.dataStore.data.map { it[Keys.disclaimerAccepted] == true }

    /** True if new age gate passed or legacy disclaimer accepted. */
    val ageVerified: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.ageVerified] == true || prefs[Keys.disclaimerAccepted] == true
    }

    /** True if extreme unlocked, or legacy `climax_unlocked` from before the rename. */
    val extremeUnlocked: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.extremeUnlocked] == true || prefs[Keys.climaxUnlockedLegacy] == true
    }

    val favoritesJson: Flow<String?> = context.dataStore.data.map { it[Keys.favoritesJson] }

    val defaultIntensity: Flow<Int> = context.dataStore.data.map { it[Keys.defaultIntensity] ?: DefaultIntensity.SPICY.storageValue }

    val categoryRomance: Flow<Boolean> = context.dataStore.data.map { it[Keys.categoryRomance] != false }

    val categoryPartyDrinking: Flow<Boolean> = context.dataStore.data.map { it[Keys.categoryPartyDrinking] != false }

    val categoryNsfw: Flow<Boolean> = context.dataStore.data.map { it[Keys.categoryNsfw] == true }

    val turnTimerSeconds: Flow<Int> = context.dataStore.data.map { it[Keys.turnTimerSeconds] ?: 30 }

    val soundEffectsEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.soundEffectsEnabled] != false }

    val hapticFeedbackEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.hapticFeedbackEnabled] != false }

    val appThemePreference: Flow<AppThemePreference> = context.dataStore.data.map { raw ->
        when (raw[Keys.appTheme]) {
            "light", "twilight" -> AppThemePreference.LIGHT
            else -> AppThemePreference.MIDNIGHT
        }
    }

    val languageTag: Flow<String> = context.dataStore.data.map { it[Keys.languageTag] ?: "en" }

    /** Last saved session player names (Session Setup); empty if unset or invalid JSON. */
    val sessionPlayerNames: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val json = prefs[Keys.sessionPlayerNamesJson] ?: return@map emptyList()
        runCatching {
            JsonLenient.decodeFromString(ListSerializer(String.serializer()), json)
        }.getOrElse { emptyList() }
    }

    suspend fun setSessionPlayerNames(names: List<String>) {
        val json = JsonLenient.encodeToString(ListSerializer(String.serializer()), names)
        context.dataStore.edit { it[Keys.sessionPlayerNamesJson] = json }
    }

    suspend fun setDisclaimerAccepted(value: Boolean) {
        context.dataStore.edit { it[Keys.disclaimerAccepted] = value }
    }

    suspend fun setAgeVerified(value: Boolean) {
        context.dataStore.edit {
            it[Keys.ageVerified] = value
            if (value) it[Keys.disclaimerAccepted] = true
        }
    }

    suspend fun setExtremeUnlocked(value: Boolean) {
        context.dataStore.edit {
            it[Keys.extremeUnlocked] = value
            it.remove(Keys.climaxUnlockedLegacy)
        }
    }

    suspend fun setFavoritesJson(json: String) {
        context.dataStore.edit { it[Keys.favoritesJson] = json }
    }

    suspend fun setDefaultIntensity(value: Int) {
        context.dataStore.edit { it[Keys.defaultIntensity] = value.coerceIn(0, 2) }
    }

    suspend fun setCategoryRomance(value: Boolean) {
        context.dataStore.edit { it[Keys.categoryRomance] = value }
    }

    suspend fun setCategoryPartyDrinking(value: Boolean) {
        context.dataStore.edit { it[Keys.categoryPartyDrinking] = value }
    }

    suspend fun setCategoryNsfw(value: Boolean) {
        context.dataStore.edit { it[Keys.categoryNsfw] = value }
    }

    suspend fun setTurnTimerSeconds(value: Int) {
        context.dataStore.edit { it[Keys.turnTimerSeconds] = value.coerceIn(10, 120) }
    }

    suspend fun setSoundEffectsEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.soundEffectsEnabled] = value }
    }

    suspend fun setHapticFeedbackEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.hapticFeedbackEnabled] = value }
    }

    suspend fun setAppThemePreference(theme: AppThemePreference) {
        context.dataStore.edit {
            it[Keys.appTheme] = when (theme) {
                AppThemePreference.LIGHT -> "light"
                AppThemePreference.MIDNIGHT -> "midnight"
            }
        }
    }

    suspend fun setLanguageTag(tag: String) {
        context.dataStore.edit { it[Keys.languageTag] = tag }
    }

    suspend fun clearFavorites() {
        context.dataStore.edit { it.remove(Keys.favoritesJson) }
    }
}
