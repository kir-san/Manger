package com.san.kir.manger.components.statistics

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.defaultSharedPreferences
import com.san.kir.ankofork.setContentView
import com.san.kir.manger.R
import com.san.kir.manger.utils.extensions.ThemedActionBarActivity
import com.san.kir.manger.view_models.StatisticViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StatisticItemActivity : ThemedActionBarActivity() {
    val mViewModel by viewModels<StatisticViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        val key = getString(R.string.settings_app_dark_theme_key)
        val default = getString(R.string.settings_app_dark_theme_default) == "true"
        val isDark = defaultSharedPreferences.getBoolean(key, default)
        setTheme(if (isDark) R.style.AppThemeDark else R.style.AppTheme)

        super.onCreate(savedInstanceState)

            lifecycleScope.launchWhenResumed {
            val manga = withContext(Dispatchers.Default) {
                mViewModel.getStatisticItem(intent.getStringExtra("manga"))
            }
            StatisticItemFullView(manga).setContentView(this@StatisticItemActivity)

            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = manga.manga
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
