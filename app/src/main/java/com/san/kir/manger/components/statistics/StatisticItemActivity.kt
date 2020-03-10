package com.san.kir.manger.components.statistics

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.dialogs.toast
import com.san.kir.ankofork.doFromSdk
import com.san.kir.ankofork.setContentView
import com.san.kir.manger.R
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.view_models.StatisticViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StatisticItemActivity : BaseActivity() {
    val mViewModel by viewModels<StatisticViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        doFromSdk(Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.transparent_dark)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.transparent_dark2)
        }

        val string = intent.getStringExtra("manga")

        string?.let {
            lifecycleScope.launchWhenResumed {
                val manga = withContext(Dispatchers.Default) {
                    mViewModel.getStatisticItem(string)
                }

                val statisticItemFullView = StatisticItemFullView(manga)

                statisticItemFullView.setContentView(this@StatisticItemActivity)

                setSupportActionBar(statisticItemFullView.appbar)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.title = manga.manga
            }
        } ?: kotlin.run {
            applicationContext.toast("Что-то пошло не так, попробуйте позже")
            finishAffinity()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
