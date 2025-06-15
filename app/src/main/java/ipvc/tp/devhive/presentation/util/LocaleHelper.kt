package ipvc.tp.devhive.presentation.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {

    private const val SELECTED_LANGUAGE_PREF_KEY = "Locale.Helper.Selected.Language"
    private const val PREFERENCES_FILE_NAME = "app_settings_prefs"

    fun onAttach(context: Context): Context {
        val languageCode = getSelectedLanguageCode(context)
        return updateResources(context, languageCode)
    }

    fun setAppLocale(context: Context, languageCode: String) {
        persistLanguageCode(context, languageCode)

        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun getSelectedLanguageCode(context: Context): String {
        val preferences = getPreferences(context)
        return preferences.getString(SELECTED_LANGUAGE_PREF_KEY, getDefaultLocale().language)
            ?: getDefaultLocale().language
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    }

    private fun persistLanguageCode(context: Context, languageCode: String?) {
        val preferences = getPreferences(context)
        preferences.edit {
            if (languageCode.isNullOrEmpty()) {
                remove(SELECTED_LANGUAGE_PREF_KEY)
            } else {
                putString(SELECTED_LANGUAGE_PREF_KEY, languageCode)
            }
        }
    }

    private fun updateResources(context: Context, languageCode: String): Context {
        val localeToSet = Locale(languageCode)
        Locale.setDefault(localeToSet)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        configuration.setLocale(localeToSet)
        configuration.setLayoutDirection(localeToSet)

        return context.createConfigurationContext(configuration)
    }

    private fun getDefaultLocale(): Locale {
        val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales.get(0)
        } else {
            @Suppress("DEPRECATION")
            Resources.getSystem().configuration.locale
        }
        return if (supportedLanguages.containsKey(systemLocale.language)) {
            systemLocale
        } else {
            Locale("en")
        }
    }

    fun getCurrentLocaleFromContext(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0)
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
    }

    val supportedLanguages = mapOf(
        "pt" to "\uD83C\uDDF5\uD83C\uDDF9 Português",
        "en" to "\uD83C\uDDFA\uD83C\uDDF8 English",
        "es" to "\uD83C\uDDEA\uD83C\uDDF8 Español",
        "de" to "\uD83C\uDDE9\uD83C\uDDEA Deutsch"
    )

    fun getLanguageNameByCode(code: String): String? {
        return supportedLanguages[code]
    }

    fun getLanguageCodeByName(name: String): String? {
        return supportedLanguages.entries.find { it.value == name }?.key
    }
}