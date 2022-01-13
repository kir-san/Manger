package com.san.kir.testshikimori

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.san.kir.core.support.DIR
import com.san.kir.core.utils.createDirs
import com.san.kir.core.utils.getFullPath
import com.san.kir.features.shikimori.ShikimoriAuth
import com.san.kir.features.shikimori.ui.setContent
import com.san.kir.testshikimori.databinding.ActivityMainBinding
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
