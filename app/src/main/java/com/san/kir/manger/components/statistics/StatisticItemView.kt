package com.san.kir.manger.components.statistics

import android.arch.lifecycle.Observer
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.san.kir.manger.R
import com.san.kir.manger.extending.anko_extend.onClick
import com.san.kir.manger.extending.anko_extend.roundedImageView
import com.san.kir.manger.extending.anko_extend.visibleOrInvisible
import com.san.kir.manger.extending.launchUI
import com.san.kir.manger.room.models.MangaStatistic
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.TimeFormat
import com.san.kir.manger.utils.loadImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.above
import org.jetbrains.anko.alignParentEnd
import org.jetbrains.anko.below
import org.jetbrains.anko.centerVertically
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalPadding
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.leftOf
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.rightOf
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent

class StatisticItemView(private val act: StatisticActivity) :
    RecyclerViewAdapterFactory.AnkoView<MangaStatistic>() {

    private lateinit var root: RelativeLayout
    private lateinit var logo: ImageView
    private lateinit var name: TextView
    private lateinit var timeText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var percent: TextView

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        relativeLayout {
            lparams(width = matchParent, height = dip(84)) { margin = dip(4) }

            padding = dip(2)

            logo = roundedImageView {
                id = ID.generate()
            }.lparams(width = dip(80), height = dip(80))

            timeText = textView {
                id = ID.generate()
                textSize = 15f
                padding = dip(2)
                leftPadding = dip(5)
            }.lparams {
                centerVertically()
                rightOf(logo)
            }

            name = textView {
                id = ID.generate()
                textSize = 20f
                padding = dip(2)
                gravity = Gravity.CENTER_HORIZONTAL
                maxLines = 1
            }.lparams(width = matchParent) {
                above(timeText)
                rightOf(logo)
            }

            percent = textView {
                id = ID.generate()
                gravity = Gravity.CENTER_HORIZONTAL
                textSize = 16f
            }.lparams(height = wrapContent, width = wrapContent) {
                below(timeText)
                alignParentEnd()
            }

            progressBar = horizontalProgressBar {
                progressDrawable = ContextCompat.getDrawable(
                    this@with.ctx,
                    R.drawable.storage_progressbar
                )
                horizontalPadding = dip(3)
            }.lparams(height = dip(10), width = matchParent) {
                below(timeText)
                rightOf(logo)
                leftOf(percent)
                topMargin = dip(5)
            }
            root = this
        }
    }

    override fun bind(item: MangaStatistic, isSelected: Boolean, position: Int) {
        act.launch(act.coroutineContext) {
            val manga = act.mViewModel.getMangaItemOrNull(item)

            withContext(Dispatchers.Main) {
                if (manga != null && manga.logo.isNotEmpty()) {
                    loadImage(manga.logo) {
                        errorColor(Color.TRANSPARENT)
                        into(logo)
                    }
                } else logo.visibleOrInvisible(false)

                name.text = item.manga

                timeText.text = act.getString(
                    R.string.statistic_subtitle,
                    TimeFormat(item.allTime).toString(act)
                )

                root.onClick {
                    act.startActivity<StatisticItemActivity>("manga" to item.manga)
                }
            }

            act.mViewModel.getStatisticAllTime()
                .observe(act, Observer {
                    act.launchUI {
                        val i = it?.toInt() ?: 0
                        progressBar.max = i
                        progressBar.progress = item.allTime.toInt()

                        if (i != 0) {
                            percent.text = act.getString(
                                R.string.storage_manga_item_size_percent,
                                Math.round(item.allTime.toDouble() / i * 100)
                            )
                        }
                    }
                })
        }
    }

}
