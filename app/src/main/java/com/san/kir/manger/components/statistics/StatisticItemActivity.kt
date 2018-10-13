package com.san.kir.manger.components.statistics

import android.os.Bundle
import android.view.MenuItem
import com.san.kir.manger.R
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.extending.ThemedActionBarActivity
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.setContentView

class StatisticItemActivity : ThemedActionBarActivity() {
    private val manga by lazy {
        val unic = intent.getStringExtra("manga")
        Main.db.statisticDao.loadItem(unic)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val key = getString(R.string.settings_app_dark_theme_key)
        val default = getString(R.string.settings_app_dark_theme_default) == "true"
        val isDark = defaultSharedPreferences.getBoolean(key, default)
        setTheme(if (isDark) R.style.AppThemeDark else R.style.AppTheme)

        super.onCreate(savedInstanceState)

        StatisticItemFullView(manga).setContentView(this)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = manga.manga
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
