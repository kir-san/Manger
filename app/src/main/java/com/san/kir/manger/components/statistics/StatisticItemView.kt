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
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.extending.ankoExtend.roundedImageView
import com.san.kir.manger.extending.ankoExtend.visibleOrInvisible
import com.san.kir.manger.room.dao.loadAllTime
import com.san.kir.manger.room.models.MangaStatistic
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.TimeFormat
import com.san.kir.manger.utils.loadImage
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.below
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalPadding
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.rightOf
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent

class StatisticItemView(private val act: StatisticActivity) :
    RecyclerViewAdapterFactory.AnkoView<MangaStatistic>() {

    private object Id {
        val logo = ID.generate()
        val name = ID.generate()
    }

    private lateinit var root: RelativeLayout
    private lateinit var logo: ImageView
    private lateinit var name: TextView
    private lateinit var timeText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var percent: TextView

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        relativeLayout {
            lparams {
                margin = dip(4)
            }

            padding = dip(2)

            logo = roundedImageView {
                id = Id.logo
            }.lparams(width = dip(80), height = dip(80))

            name = textView {
                id = Id.name
                textSize = 20f
                padding = dip(2)
                gravity = Gravity.CENTER_HORIZONTAL
                maxLines = 1
            }.lparams(width = matchParent) { rightOf(Id.logo) }

            timeText = textView {
                textSize = 15f
                padding = dip(2)
                leftPadding = dip(5)
            }.lparams {
                below(Id.name)
                rightOf(Id.logo)
            }

            progressBar = horizontalProgressBar {
                progressDrawable = ContextCompat.getDrawable(
                    this@with.ctx,
                    R.drawable.storage_progressbar
                )
                horizontalPadding = dip(3)
            }.lparams(height = dip(21), width = matchParent) {
                alignParentBottom()
                rightOf(Id.logo)
            }

            percent = textView {
                gravity = Gravity.CENTER_HORIZONTAL
                textSize = 15f
            }.lparams(height = wrapContent, width = matchParent) {
                alignParentBottom()
                rightOf(Id.logo)
            }


            root = this
        }
    }

    override fun bind(item: MangaStatistic, isSelected: Boolean, position: Int) {
        launch(UI) {
            val context = root.context
            val manga = Main.db.mangaDao.loadMangaOrNull(item.manga)

            if (manga != null && manga.logo.isNotEmpty()) {
                loadImage(manga.logo) {
                    errorColor(Color.TRANSPARENT)
                    into(logo)
                }
            } else logo.visibleOrInvisible(false)

            name.text = item.manga

            timeText.text = context.getString(
                R.string.statistic_subtitle,
                TimeFormat(item.allTime).toString(context)
            )

            root.onClick {
                context.startActivity<StatisticItemActivity>("manga" to item.manga)
            }

            Main.db.statisticDao
                .loadAllTime()
                .observe(act, Observer {
                    launch(UI) {
                        val i = it?.toInt() ?: 0
                        progressBar.max = i
                        progressBar.progress = item.allTime.toInt()

                        if (i != 0) {
                            percent.text = context.getString(
                                R.string.storage_manga_item_size_percent,
                                Math.round((item.allTime / i * 100).toDouble())
                            )
                        }
                    }
                })
        }
    }

}
