package com.san.kir.manger.components.drawer

import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.view.View
import android.widget.LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.extending.BaseActivity
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.toast

abstract class DrawerActivity : BaseActivity() {
    private val mView by lazy { DrawerView(this) }

    abstract val LinearLayout.view: View

    override fun onCreate(savedInstanceState: Bundle?) {

        val key = getString(R.string.settings_app_dark_theme_key)
        val default = getString(R.string.settings_app_dark_theme_default) == "true"
        val isDark = defaultSharedPreferences.getBoolean(key, default)
        setTheme(if (isDark) R.style.AppThemeDark else R.style.AppTheme)

        super.onCreate(savedInstanceState)

        mView.createView(this) {
            view
        }
    }

    private var backPressed: Long = 0
    override fun onBackPressed() {
        if (mView.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            if (backPressed + 2000 > System.currentTimeMillis()) {
                finishAffinity()
            } else {
                toast(R.string.first_run_exit_text)
            }
            backPressed = System.currentTimeMillis()
        } else {
            mView.drawerLayout.openDrawer(GravityCompat.START)
        }
    }
}
