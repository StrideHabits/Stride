package com.mpieterse.stride.core.extensions.android

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

// --- Extensions

fun Context.openBrowserTo(
    webViewUrl: String,
    useContext: Context = this
) {
    val intent = CustomTabsIntent.Builder().build()
    intent.launchUrl(useContext, webViewUrl.toUri())
}