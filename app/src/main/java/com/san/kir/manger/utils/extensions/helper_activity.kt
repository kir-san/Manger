package com.san.kir.manger.utils.extensions

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.san.kir.ankofork.defaultSharedPreferences

abstract class BaseActivity : AppCompatActivity(), SharedPreferencesHolder {
    override val preferences: SharedPreferences by lazy { defaultSharedPreferences }
    override val editor: SharedPreferences.Editor by lazy { preferences.edit() }
    override val ctx: Context by lazy { this }

    override fun onDestroy() {
        super.onDestroy()
        finishEditor()
    }
}
