package com.san.kir.manger.extending.dialogs

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewManager
import android.widget.ProgressBar
import android.widget.TextView
import com.san.kir.manger.App
import com.san.kir.manger.R
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.extending.ankoExtend.onClick
import com.san.kir.manger.room.dao.categoryNames
import com.san.kir.manger.room.dao.insertAsync
import com.san.kir.manger.room.models.MangaColumn
import com.san.kir.manger.room.models.MangaStatistic
import com.san.kir.manger.room.models.SiteCatalogElement
import com.san.kir.manger.room.models.toManga
import com.san.kir.manger.utils.DIR
import com.san.kir.manger.utils.MangaUpdaterService
import com.san.kir.manger.utils.createDirs
import com.san.kir.manger.utils.getFullPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.customView
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.padding
import org.jetbrains.anko.selector
import org.jetbrains.anko.startService
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
        GlobalScope.launch(Dispatchers.Main) {
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
                    textView(R.string.add_manga_dialog_update_manga) {
                        padding = dip(5)
                    }
                    added = hideTextView(R.string.add_manga_dialog_added_manga)
                    searching = hideTextView(R.string.add_manga_dialog_search_chapters)
                    allReady = hideTextView(R.string.add_manga_dialog_all_complete)
                    error = hideTextView(R.string.add_manga_dialog_error)
                    progressBar = horizontalProgressBar {
                        isIndeterminate = true
                    }
                    okBtn = hideTextView(R.string.add_manga_close_btn).lparams(width = wrapContent, height = wrapContent) {
                        gravity = Gravity.END
                    }
                }
            }
        }.show()
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val pat = Pattern.compile("[a-z/0-9]+-").matcher(element.shotLink)
                if (pat.find())
                    element.shotLink = element.shotLink
                        .removePrefix(pat.group()).removeSuffix(".html")
                val path = "${DIR.MANGA}/${element.catalogName}/${element.shotLink}"
                createDirs(getFullPath(path))

                val updatingElement = ManageSites.getFullElement(element).await()

                val manga = updatingElement.toManga(category = category, path = path)
                Main.db.mangaDao.insertAsync(manga)

                Main.db.statisticDao.insertAsync(MangaStatistic(manga = manga.unic))

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

    private fun ViewManager.hideTextView(id: Int) =
        textView(id) {
            visibility = View.GONE
            padding = dip(5)
        }
}
