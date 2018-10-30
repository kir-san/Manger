package com.san.kir.manger.components.statistics

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.View
import android.widget.LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.dao.loadAllTime
import com.san.kir.manger.utils.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView

class StatisticActivity : DrawerActivity() {
    private val titleObserver by lazy {
        Observer<Long> { l ->
            l?.let {
                val time = GlobalScope.async {
                    val time = TimeFormat(it)
                    Html.fromHtml(
                        "<font color='#FFFFFF'>${
                        getString(
                            R.string.statistic_subtitle,
                            time.toString(this@StatisticActivity)
                        )
                        }</font>"
                    )
                }

                GlobalScope.launch(Dispatchers.Main) {
                    supportActionBar?.setTitle(R.string.main_menu_statistic)
                    supportActionBar?.subtitle = time.await()
                }
            }
        }
    }

    override val LinearLayout.customView: View
        get() = recyclerView {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            StatisticRecyclerPresenter(this@StatisticActivity).into(this)
            lparams(matchParent, matchParent)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Main.db.statisticDao.loadAllTime().observe(this, titleObserver)
    }
}
