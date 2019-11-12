package com.san.kir.manger.components.storage

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.bottomPadding
import com.san.kir.ankofork.defaultSharedPreferences
import com.san.kir.ankofork.dialogs.alert
import com.san.kir.ankofork.dialogs.customView
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.horizontalProgressBar
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.padding
import com.san.kir.ankofork.sdk28.backgroundColor
import com.san.kir.ankofork.sdk28.backgroundResource
import com.san.kir.ankofork.sdk28.imageView
import com.san.kir.ankofork.sdk28.linearLayout
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.verticalLayout
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.extending.dialogs.DeleteAllDialog
import com.san.kir.manger.extending.dialogs.DeleteReadChaptersDialog
import com.san.kir.manger.repositories.StorageRepository
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.Storage
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.utils.extensions.format
import com.san.kir.manger.utils.extensions.formatDouble
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class StorageDialogView(private val act: BaseActivity) {
    private val storage = StorageRepository(act)
    private var name: TextView? = null
    private var progressBar: ProgressBar? = null
    private var allSize: TextView? = null
    private var mangaSize: TextView? = null
    private var readSize: TextView? = null
    private var readSizeAction: LinearLayout? = null
    private var allSizeAction: LinearLayout? = null
    private var isDark: Boolean = false

    init {
        act.alert {
            customView {
                verticalLayout {
                    padding = dip(16)
                    bottomPadding = 0

                    val key = act.getString(R.string.settings_app_dark_theme_key)
                    val default =
                        act.getString(R.string.settings_app_dark_theme_default) == "true"
                    isDark = act.defaultSharedPreferences.getBoolean(key, default)

                    name = textView {
                        gravity = Gravity.CENTER_HORIZONTAL
                        textSize = 18f
                        setTypeface(typeface, Typeface.BOLD)
                    }.lparams(width = matchParent, height = wrapContent) {
                        bottomMargin = dip(10)
                    }

                    progressBar = horizontalProgressBar {
                        progressDrawable = ContextCompat.getDrawable(
                            act, R.drawable.storage_progressbar
                        )
                    }.lparams(height = dip(50), width = matchParent)

                    // Строка с отображением всего занятого места
                    linearLayout {
                        lparams(width = matchParent, height = dip(30)) {
                            topMargin = dip(10)
                        }

                        imageView { backgroundColor = Color.LTGRAY }
                            .lparams(width = dip(50), height = dip(28)) {
                                marginEnd = dip(16)
                            }

                        allSize = textView(R.string.storage_item_all_s).lparams {
                            gravity = Gravity.CENTER_VERTICAL
                        }
                    }

                    // Строка с отображением занятого места выбранной манги
                    linearLayout {
                        lparams(width = matchParent, height = dip(30)) {
                            topMargin = dip(10)
                        }

                        imageView { backgroundColor = Color.parseColor("#FFFF4081") }
                            .lparams(width = dip(50), height = dip(28)) {
                                marginEnd = dip(16)
                            }

                        mangaSize = textView().lparams { gravity = Gravity.CENTER_VERTICAL }
                    }

                    // Строка с отображение зянятого места прочитанных глав выбранной манги
                    linearLayout {
                        lparams(width = matchParent, height = dip(30)) {
                            topMargin = dip(10)
                        }

                        imageView { backgroundColor = Color.parseColor("#222e7a") }
                            .lparams(width = dip(50), height = dip(28)) {
                                marginEnd = dip(16)
                            }

                        readSize = textView().lparams { gravity = Gravity.CENTER_VERTICAL }
                    }

                    // Очистка прочитанных глав
                    linearLayout {
                        lparams(width = matchParent, height = dip(34)) {
                            topMargin = dip(10)
                        }

                        imageView {
                            backgroundResource =
                                if (isDark) R.drawable.ic_action_delete_white
                                else R.drawable.ic_action_delete_black
                            scaleType = ImageView.ScaleType.FIT_CENTER
                        }.lparams(width = dip(30), height = dip(30)) {
                            marginEnd = dip(26)
                            marginStart = dip(10)
                        }

                        textView(R.string.library_popupmenu_delete_read_chapters).lparams {
                            gravity = Gravity.CENTER_VERTICAL
                        }

                        readSizeAction = this
                    }

                    // Очистка прочитанных глав
                    linearLayout {
                        lparams(width = matchParent, height = dip(34)) {
                            topMargin = dip(10)
                        }

                        imageView {
                            backgroundResource =
                                if (isDark) R.drawable.ic_action_delete_white
                                else R.drawable.ic_action_delete_black
                            scaleType = ImageView.ScaleType.FIT_CENTER
                        }.lparams(width = dip(30), height = dip(30)) {
                            marginEnd = dip(26)
                            marginStart = dip(10)
                        }

                        textView(R.string.library_popupmenu_delete_all).lparams {
                            gravity = Gravity.CENTER_VERTICAL
                        }

                        allSizeAction = this
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
                progressBar?.max = it?.roundToInt() ?: 0
                progressBar?.progress = dir?.sizeFull?.roundToInt() ?: 0
                progressBar?.secondaryProgress = dir?.sizeRead?.roundToInt() ?: 0
            })
        storage.loadItemWhere(manga.path).observe(act, Observer { item ->
            if (dir == null && item != null) {
                updateStorageItem(item)
            }

            name?.text = manga.name

            mangaSize?.text =
                act.getString(R.string.storage_item_manga_size, item?.sizeFull.format())

            readSize?.text = act.getString(R.string.storage_item_read_size, item?.sizeRead.format())

            readSizeAction?.onClick {
                DeleteReadChaptersDialog(act, manga, item?.sizeRead)
            }

            allSizeAction?.onClick {
                DeleteAllDialog(act, manga, item?.sizeFull)
            }
            dir = item
        })
    }

    private fun updateStorageItem(dir: Storage?) = act.lifecycleScope.launch(Dispatchers.Default) {
        dir?.let { storage.update(storage.getSizeAndIsNew(it)) }
    }
}
