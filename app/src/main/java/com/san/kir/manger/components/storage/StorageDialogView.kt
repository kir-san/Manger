package com.san.kir.manger.components.storage

import android.arch.lifecycle.Observer
import android.graphics.Color
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.san.kir.manger.R
import com.san.kir.manger.extending.BaseActivity
import com.san.kir.manger.extending.ankoExtend.onClick
import com.san.kir.manger.extending.dialogs.DeleteReadChaptersDialog
import com.san.kir.manger.repositories.StorageRepository
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.Storage
import com.san.kir.manger.utils.formatDouble
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.customView
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalMargin
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.textResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent
import kotlin.math.roundToInt

class StorageDialogView(private val act: BaseActivity) {
    private val storage = StorageRepository(act)
    private var name: TextView? = null
    private var progressBar: ProgressBar? = null
    private var allSize: TextView? = null
    private var mangaSize: TextView? = null
    private var readSize: TextView? = null
    private var readSizeAction: LinearLayout? = null
    private var isDark: Boolean = false

    init {
        act.alert {
            customView {
                verticalLayout {
                    padding = dip(10)
                    bottomPadding = 0

                    val key = act.getString(R.string.settings_app_dark_theme_key)
                    val default =
                        act.getString(R.string.settings_app_dark_theme_default) == "true"
                    isDark = act.defaultSharedPreferences.getBoolean(key, default)

                    name = textView {
                        gravity = Gravity.CENTER_HORIZONTAL
                        textSize = 18f
                        setTypeface(typeface, Typeface.BOLD)
                    }.lparams(width = matchParent, height = wrapContent)

                    progressBar = horizontalProgressBar {
                        padding = dip(4)
                        progressDrawable = ContextCompat.getDrawable(
                            act,
                            R.drawable.storage_progressbar
                        )
                    }.lparams(height = dip(50), width = matchParent)

                    // Строка с отображением всего занятого места
                    linearLayout {
                        lparams(width = matchParent, height = dip(30))
                        padding = dip(4)
                        imageView { backgroundColor = Color.LTGRAY }
                            .lparams(width = dip(50), height = dip(28))

                        allSize = textView(R.string.storage_item_all_s) {
                            leftPadding = dip(4)
                        }.lparams { gravity = Gravity.CENTER_VERTICAL }
                    }

                    // Строка с отображением занятого места выбранной манги
                    linearLayout {
                        lparams(width = matchParent, height = dip(30))
                        padding = dip(4)
                        imageView { backgroundColor = Color.parseColor("#FFFF4081") }
                            .lparams(width = dip(50), height = dip(28))

                        mangaSize = textView {
                            leftPadding = dip(4)
                        }.lparams { gravity = Gravity.CENTER_VERTICAL }
                    }

                    // Строка с отображение зянятого места прочитанных глав выбранной манги
                    linearLayout {
                        lparams(width = matchParent, height = dip(30))
                        padding = dip(4)
                        imageView { backgroundColor = Color.parseColor("#222e7a") }
                            .lparams(width = dip(50), height = dip(28))

                        readSize = textView {
                            leftPadding = dip(4)
                        }.lparams { gravity = Gravity.CENTER_VERTICAL }
                    }

                    // Очистка прочитанных глав
                    linearLayout {
                        lparams(width = matchParent, height = dip(34))
                        padding = dip(4)
                        imageView {
                            backgroundResource =
                                    if (isDark) R.drawable.ic_action_delete_white
                                    else R.drawable.ic_action_delete_black
                            scaleType = ImageView.ScaleType.FIT_CENTER
                        }.lparams(width = dip(30), height = dip(30)) {
                            horizontalMargin = dip(10)
                        }

                        textView {
                            leftPadding = dip(4)
                            textResource = R.string.library_popupmenu_delete_read_chapters
                        }.lparams { gravity = Gravity.CENTER_VERTICAL }

                        readSizeAction = this
                    }
                }
            }


            positiveButton(R.string.storage_dialog_close) { }
        }.show()
    }

    fun bind(manga: Manga) {
        var dir: Storage? = null
        storage.loadAllSize()
            .observe(act, Observer {
                allSize?.text = act.getString(
                    R.string.storage_item_all_size,
                    formatDouble(it)
                )
//                allSize?.textColor = if (isDark) Color.WHITE else Color.BLACK
                progressBar?.max = it?.roundToInt() ?: 0
                progressBar?.progress = dir?.sizeFull?.roundToInt() ?: 0
                progressBar?.secondaryProgress = dir?.sizeRead?.roundToInt() ?: 0
            })
        storage.loadItemWhere(manga.path).observe(act, Observer { item ->
            if (dir == null && item != null) {
                updateStorageItem(item)
            }

            name?.text = manga.name
            mangaSize?.text = act.getString(
                R.string.storage_item_manga_size,
                formatDouble(item?.sizeFull)
            )
            readSize?.text = act.getString(
                R.string.storage_item_read_size,
                formatDouble(item?.sizeRead)
            )
            readSizeAction?.onClick {
                DeleteReadChaptersDialog(act, manga) {
                    updateStorageItem(item)
                }
            }
            dir = item
        })
    }

    private fun updateStorageItem(dir: Storage?) = act.launch(Dispatchers.Default) {
        dir?.let { storage.update(storage.getSizeAndIsNew(it)) }
    }
}
