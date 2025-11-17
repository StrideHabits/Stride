package com.mpieterse.stride.ui.layout.shared.models

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate
import com.mpieterse.stride.core.LocalApplication
import kotlinx.coroutines.runBlocking

abstract class LocalizableActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context?) {
//        val service = (newBase?.applicationContext as LocalApplication).languageService
//        runBlocking {
//            val context = service.wrapContext(newBase)
//        }
        super.attachBaseContext(newBase)
    }
}