package com.san.kir.manger.extending.dialogs

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.san.kir.manger.App
import com.san.kir.manger.R
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.dao.categoryNames
import com.san.kir.manger.room.dao.insertAsync
import com.san.kir.manger.room.models.MangaColumn
import com.san.kir.manger.room.models.SiteCatalogElement
import com.san.kir.manger.room.models.toManga
import com.san.kir.manger.utils.DIR
import com.san.kir.manger.utils.MangaUpdaterService
import com.san.kir.manger.utils.createDirs
import com.san.kir.manger.utils.getFullPath
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.alert
import org.jetbrains.anko.customView
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.padding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.selector
import org.jetbrains.anko.startService
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textResource
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent
import java.util.regex.Pattern

class AddMangaDialog(
    context: Context,
    private val element: SiteCatalogElement,
    private val onFinish: () -> Unit
) {
    init {
        async(UI) {
            val categories = Main.db.categoryDao.categoryNames()

            context.selector(
                title = App.context.getString(R.string.catalog_for_one_site_selector_item),
                items = categories
            ) { _, index ->
                nextStep(context, categories[index])
            }
        }
    }

    private fun nextStep(context: Context, category: String) {
        var added: TextView? = null
        var searching: TextView? = null
        var allReady: TextView? = null
        var progressBar: ProgressBar? = null
        var okBtn: TextView? = null
        var error: TextView? = null
        val dialog = context.alert {
            customView {
                verticalLayout {
                    padding = dip(10)
                    textView {
                        text = context.getString(
                            R.string.add_manga_dialog_changed_category,
                            category
                        )
                        padding = dip(5)
                    }
                    textView {
                        textResource = R.string.add_manga_dialog_created_folder
                        padding = dip(5)
                    }
                    added = textView {
                        textResource = R.string.add_manga_dialog_added_manga
                        visibility = View.GONE
                        padding = dip(5)
                    }
                    searching = textView {
                        textResource = R.string.add_manga_dialog_search_chapters
                        padding = dip(5)
                        visibility = View.GONE
                    }
                    allReady = textView {
                        textResource = R.string.add_manga_dialog_all_complete
                        padding = dip(5)
                        visibility = View.GONE
                    }
                    error = textView {
                        padding = dip(5)
                        textResource = R.string.add_manga_dialog_error
                        visibility = View.GONE
                        textColor = Color.RED
                    }
                    progressBar = horizontalProgressBar {
                        isIndeterminate = true
                    }
                    okBtn = textView {
                        textResource = R.string.add_manga_close_btn
                        textColor = Color.parseColor("#FFFF4081")
                        visibility = View.GONE
                        padding = dip(5)
                    }.lparams(width = wrapContent, height = wrapContent) {
                        gravity = Gravity.END
                    }
                }
            }
        }.show()
        async(UI) {
            try {
                val pat = Pattern.compile("[a-z/0-9]+-").matcher(element.shotLink)
                if (pat.find())
                    element.shotLink = element.shotLink
                        .removePrefix(pat.group()).removeSuffix(".html")
                val path = "${DIR.MANGA}/${element.catalogName}/${element.shotLink}"
                createDirs(getFullPath(path))

                val manga = element.toManga(category = category, path = path)

                Main.db.mangaDao.insertAsync(manga)
                added?.visibility = View.VISIBLE
                searching?.visibility = View.VISIBLE

                context.startService<MangaUpdaterService>(MangaColumn.tableName to manga)

                allReady?.visibility = View.VISIBLE
            } catch (ex: Exception) {
                error?.visibility = View.VISIBLE
            } finally {
                onFinish()
                progressBar?.visibility = View.GONE
                okBtn?.visibility = View.VISIBLE
                okBtn?.onClick {
                    dialog.dismiss()
                }
            }
        }
    }
}
