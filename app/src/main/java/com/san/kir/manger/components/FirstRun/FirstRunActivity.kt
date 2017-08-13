package com.san.kir.manger.components.FirstRun

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceManager
import com.san.kir.manger.R
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.toast

class FirstRunActivity : AppCompatActivity(), LifecycleRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    val prefenceManager: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

    override fun getLifecycle(): LifecycleRegistry = lifecycleRegistry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirstRunView(this).setContentView(this)
    }

    private var back_pressed: Long = 0
    override fun onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis())
            finishAffinity()
        else
            toast(R.string.first_run_exit_text)
        back_pressed = System.currentTimeMillis()
    }
}
