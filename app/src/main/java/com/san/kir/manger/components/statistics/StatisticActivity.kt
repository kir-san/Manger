package com.san.kir.manger.components.statistics

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.View
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.utils.TimeFormat
import com.san.kir.manger.view_models.StatisticViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko._LinearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView

class StatisticActivity : DrawerActivity() {
    private val titleObserver by lazy {
        Observer<Long> { l ->
            l?.let {
                launch(Dispatchers.Main) {
                    supportActionBar?.setTitle(R.string.main_menu_statistic)
                    supportActionBar?.subtitle = withContext(Dispatchers.Default) {
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
                }
            }
        }
    }
    val mViewModel by lazy {
        ViewModelProviders.of(this).get(StatisticViewModel::class.java)
    }

    override val _LinearLayout.customView: View
        get() = recyclerView {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            StatisticRecyclerPresenter(this@StatisticActivity).into(this)
            lparams(matchParent, matchParent)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.getStatisticAllTime().observe(this, titleObserver)
    }
}
