package com.abrarshakhi.mishti.common.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.abrarshakhi.mishti.common.llm.InferenceSettings
import com.abrarshakhi.mishti.common.ui.theme.AppColorScheme
import com.abrarshakhi.mishti.common.ui.theme.AppFont
import com.abrarshakhi.mishti.common.ui.theme.ThemeMode
import com.abrarshakhi.mishti.common.ui.theme.ThemeSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException


private val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mishti_preferences",
)

class DataStoreAppPreferences(context: Context) : AppPreferences {

    private val dataStore = context.preferencesDataStore

    private val preferences: Flow<Preferences> =
        dataStore.data.catch { cause -> if (cause is IOException) emit(emptyPreferences()) else throw cause }

    override val hasCompletedOnboarding: Flow<Boolean> =
        preferences.map { it[Keys.ONBOARDING_COMPLETED] == true }

    override val themeSettings: Flow<ThemeSettings> = preferences.map { prefs ->
        ThemeSettings(
            mode = prefs[Keys.THEME_MODE].toEnum(ThemeMode.System),
            colorScheme = prefs[Keys.COLOR_SCHEME].toEnum(AppColorScheme.Dynamic),
            font = prefs[Keys.FONT].toEnum(AppFont.System),
        )
    }

    override val selectedModelId: Flow<String?> = preferences.map { it[Keys.SELECTED_MODEL_ID] }

    override suspend fun setSelectedModelId(modelId: String?) {
        dataStore.edit { prefs ->
            if (modelId == null) prefs.remove(Keys.SELECTED_MODEL_ID)
            else prefs[Keys.SELECTED_MODEL_ID] = modelId
        }
    }

    override val inferenceSettings: Flow<InferenceSettings> = preferences.map { prefs ->
        val defaults = InferenceSettings()
        InferenceSettings(
            systemPrompt = prefs[Keys.SYSTEM_PROMPT] ?: defaults.systemPrompt,
            temperature = prefs[Keys.TEMPERATURE] ?: defaults.temperature,
            topK = prefs[Keys.TOP_K] ?: defaults.topK,
            topP = prefs[Keys.TOP_P] ?: defaults.topP,
            maxTokens = prefs[Keys.MAX_TOKENS] ?: defaults.maxTokens,
            contextTokens = prefs[Keys.CONTEXT_TOKENS] ?: defaults.contextTokens,
            threads = prefs[Keys.THREADS] ?: defaults.threads,
        )
    }

    override suspend fun setInferenceSettings(settings: InferenceSettings) {
        dataStore.edit { prefs ->
            prefs[Keys.SYSTEM_PROMPT] = settings.systemPrompt
            prefs[Keys.TEMPERATURE] = settings.temperature
            prefs[Keys.TOP_K] = settings.topK
            prefs[Keys.TOP_P] = settings.topP
            prefs[Keys.MAX_TOKENS] = settings.maxTokens
            prefs[Keys.CONTEXT_TOKENS] = settings.contextTokens
            prefs[Keys.THREADS] = settings.threads
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    override suspend fun setColorScheme(scheme: AppColorScheme) {
        dataStore.edit { it[Keys.COLOR_SCHEME] = scheme.name }
    }

    override suspend fun setFont(font: AppFont) {
        dataStore.edit { it[Keys.FONT] = font.name }
    }

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val COLOR_SCHEME = stringPreferencesKey("color_scheme")
        val FONT = stringPreferencesKey("font")
        val SELECTED_MODEL_ID = stringPreferencesKey("selected_model_id")
        val SYSTEM_PROMPT = stringPreferencesKey("system_prompt")
        val TEMPERATURE = floatPreferencesKey("temperature")
        val TOP_K = intPreferencesKey("top_k")
        val TOP_P = floatPreferencesKey("top_p")
        val MAX_TOKENS = intPreferencesKey("max_tokens")
        val CONTEXT_TOKENS = intPreferencesKey("context_tokens")
        val THREADS = intPreferencesKey("threads")
    }
}

/**
 * Enums are stored by name, and an unrecognised one degrades to [fallback].
 *
 * Same rule as the Room mappers: a value written by a newer build — a scheme that has since
 * been renamed or removed — must not crash an older one on read.
 */
private inline fun <reified E : Enum<E>> String?.toEnum(fallback: E): E =
    this?.let { name -> runCatching { enumValueOf<E>(name) }.getOrNull() } ?: fallback
