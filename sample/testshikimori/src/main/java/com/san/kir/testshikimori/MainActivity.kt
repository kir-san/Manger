package com.san.kir.testshikimori

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.san.kir.features.shikimori.ui.setContent
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent()
    }
}
