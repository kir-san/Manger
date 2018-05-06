package com.san.kir.manger.components.storage

import android.arch.lifecycle.Observer
import android.graphics.Color
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.san.kir.manger.App.Companion.context
import com.san.kir.manger.R
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.extending.BaseActivity
import com.san.kir.manger.extending.dialogs.DeleteReadChaptersDialog
import com.san.kir.manger.room.dao.getSizeAndIsNew
import com.san.kir.manger.room.dao.loadAllSize
import com.san.kir.manger.room.dao.loadLivedStorageItem
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.Storage
import com.san.kir.manger.utils.formatDouble
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent
import kotlin.math.roundToInt

class StorageDialogView : AnkoComponent<StorageDialogFragment> {
    private val storage = Main.db.storageDao

    private var name: TextView? = null
    private var progressBar: ProgressBar? = null
    private var allSize: TextView? = null
    private var mangaSize: TextView? = null
    private var readSize: TextView? = null
    private var readSizeAction: LinearLayout? = null

    fun createView(parent: StorageDialogFragment): View {
        val ctx = parent.context
        return if (ctx != null) {
            createView(AnkoContext.create(ctx, parent))
        } else {
            View(ctx)
        }
    }

    override fun createView(ui: AnkoContext<StorageDialogFragment>) = with(ui) {
        verticalLayout {
            padding = dip(10)
            bottomPadding = 0

            name = textView {
                gravity = Gravity.CENTER_HORIZONTAL
                textSize = 18f
                setTypeface(typeface, Typeface.BOLD)
            }.lparams(width = matchParent, height = wrapContent)

            progressBar = horizontalProgressBar {
                padding = dip(4)
                progressDrawable = ContextCompat.getDrawable(this@with.ctx,
                                                             R.drawable.storage_progressbar)
            }.lparams(height = dip(50), width = matchParent)

            // Строка с отображением всего занятого места
            linearLayout {
                lparams(width = matchParent, height = dip(30))
                padding = dip(4)
                imageView { backgroundColor = Color.LTGRAY }
                        .lparams(width = dip(50), height = dip(28))

                allSize = textView(R.string.storage_item_all_s) { leftPadding = dip(4) }
                        .lparams { gravity = Gravity.CENTER_VERTICAL }
            }

            // Строка с отображением занятого места выбранной манги
            linearLayout {
                lparams(width = matchParent, height = dip(30))
                padding = dip(4)
                imageView { backgroundColor = Color.parseColor("#FFFF4081") }
                        .lparams(width = dip(50), height = dip(28))

                mangaSize = textView { leftPadding = dip(4) }
                        .lparams { gravity = Gravity.CENTER_VERTICAL }
            }

            // Строка с отображение зянятого места прочитанных глав выбранной манги, и возможностью
            // удалить эти прочитанные главы
            linearLayout {
                lparams(width = matchParent, height = dip(30))
                padding = dip(4)
                imageView { backgroundColor = Color.parseColor("#222e7a") }
                        .lparams(width = dip(50), height = dip(28))

                readSize = textView { leftPadding = dip(4) }
                        .lparams { gravity = Gravity.CENTER_VERTICAL }

                imageView {
                    backgroundResource = R.drawable.ic_action_delete_black
                }
                readSizeAction = this
            }
        }
    }

    fun bind(manga: Manga, act: BaseActivity) {
        var dir: Storage? = null
        storage.loadAllSize()
                .observe(act, Observer {
                    allSize?.text = context.getString(R.string.storage_item_all_size,
                                                      formatDouble(it))
                    progressBar?.max = it?.roundToInt() ?: 0
                    progressBar?.progress = dir?.sizeFull?.roundToInt() ?: 0
                    progressBar?.secondaryProgress = dir?.sizeRead?.roundToInt() ?: 0
                })

        storage.loadLivedStorageItem(manga.path).observe(act, Observer { item ->
            if (dir == null && item != null) {
                updateStorageItem(item)
            }

            name?.text = manga.name
            mangaSize?.text = context.getString(R.string.storage_item_manga_size,
                                                formatDouble(item?.sizeFull))
            readSize?.text = context.getString(R.string.storage_item_read_size,
                                               formatDouble(item?.sizeRead))
            readSizeAction?.onClick {
                DeleteReadChaptersDialog(readSizeAction!!.context, manga) {
                    updateStorageItem(item)
                }
            }
            dir = item
        })
    }

    private fun updateStorageItem(dir: Storage?) = async {
        dir?.let { storage.update(it.getSizeAndIsNew()) }
    }
}
