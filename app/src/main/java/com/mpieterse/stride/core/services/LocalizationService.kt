package com.mpieterse.stride.core.services

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.mpieterse.stride.core.utils.LocaleConfig

class LocalizationService(
    private val application: Application,
) {
    companion object {
        private const val TAG = "LocalizationService"
    }


// --- Functions


    fun setCurrentUiCulture(cultureTagIso639: String) {
        val locales = LocaleListCompat.forLanguageTags(cultureTagIso639)
        AppCompatDelegate.setApplicationLocales(locales)
    }


    fun getCurrentUiCulture(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return locales.toLanguageTags().ifBlank {
            "en"
        }
    }


    fun getSupportedLanguages(): List<String> {
        return LocaleConfig.readLocales(application)
    }


//    suspend fun setCurrentUiCulture(cultureTagIso639: String) {
//        configurationService.put(
//            ConfigurationSchema.appUiCulture,
//            cultureTagIso639
//        )
//    }
//
//
//    suspend fun getCurrentUiCulture(): String {
//        return configurationService.get(ConfigurationSchema.appUiCulture)
//    }
//
//
//    suspend fun wrapContext(
//        context: Context
//    ): Context {
//        val current = getCurrentUiCulture()
//        val culture = Locale.forLanguageTag(current)
//        Locale.setDefault(culture)
//
//        val appConfig = context.resources.configuration
//        appConfig.setLayoutDirection(culture)
//        appConfig.setLocale(culture)
//
//        return context
//            .createConfigurationContext(appConfig)
//    }
}