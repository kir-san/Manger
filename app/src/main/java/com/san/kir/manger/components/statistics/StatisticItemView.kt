package com.san.kir.manger.components.statistics

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.horizontalProgressBar
import com.san.kir.ankofork.margin
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.sdk28.linearLayout
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.startActivity
import com.san.kir.ankofork.verticalLayout
import com.san.kir.ankofork.verticalMargin
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.MangaStatistic
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.TimeFormat
import com.san.kir.manger.utils.extensions.roundedImageView
import com.san.kir.manger.utils.extensions.visibleOrInvisible
import com.san.kir.manger.utils.loadImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatisticItemView(private val act: StatisticActivity) :
    RecyclerViewAdapterFactory.AnkoView<MangaStatistic>() {

    private lateinit var root: LinearLayout
    private lateinit var logo: ImageView
    private lateinit var name: TextView
    private lateinit var timeText: TextView
    private lateinit var progressBar: ProgressBar

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        linearLayout {
            lparams(width = matchParent)

            gravity = Gravity.CENTER_VERTICAL

            logo = roundedImageView {
            }.lparams(width = dip(55), height = dip(55)) {
                margin = dip(16)
            }

            verticalLayout {
                name = textView {
                    textSize = 17f
                    maxLines = 1
                }

                timeText = textView {
                    textSize = 14f
                }

                progressBar = horizontalProgressBar {
                    progressDrawable = ContextCompat.getDrawable(
                        this@with.ctx, R.drawable.storage_progressbar
                    )
                }.lparams(height = dip(10), width = matchParent) {
                    verticalMargin = dip(6)
                }

            }.lparams(width = matchParent) {
                weight = 1f
                marginEnd = dip(16)
            }

            root = this
        }
    }

    override fun bind(item: MangaStatistic, isSelected: Boolean, position: Int) {
        act.lifecycleScope.launch(Dispatchers.Main) {
            val manga = withContext(Dispatchers.Default) { act.mViewModel.getMangaItemOrNull(item) }

            if (manga != null && manga.logo.isNotEmpty()) {
                loadImage(manga.logo) {
                    errorColor(Color.TRANSPARENT)
                    into(logo)
                }
            } else logo.visibleOrInvisible(false)

            name.text = item.manga

            timeText.text = act.getString(
                R.string.statistic_item_time,
                TimeFormat(item.allTime).toString(act)
            )

            root.onClick {
                act.startActivity<StatisticItemActivity>("manga" to item.manga)
            }

            act.mViewModel.getStatisticAllTime()
                .observe(act, Observer {
                    progressBar.max = it?.toInt() ?: 0
                    progressBar.progress = item.allTime.toInt()
                })
        }
    }

}
