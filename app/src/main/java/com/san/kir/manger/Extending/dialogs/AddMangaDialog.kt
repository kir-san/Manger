package com.san.kir.manger.Extending.dialogs

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.san.kir.manger.App
import com.san.kir.manger.R
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.room.DAO.categoryNames
import com.san.kir.manger.room.DAO.insert
import com.san.kir.manger.room.models.SiteCatalogElement
import com.san.kir.manger.room.models.toManga
import com.san.kir.manger.utils.DIR
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
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent
import java.util.regex.Pattern

class AddMangaDialog(context: Context,
                     val element: SiteCatalogElement,
                     private val onFinish: () -> Unit) {
    init {
        async(UI) {
            val categories = Main.db.categoryDao.categoryNames()

            context.selector(title = App.context.getString(R.string.catalog_for_one_site_selector_item),
                             items = categories) { _, index ->
                nextStep(context, categories[index])
            }
        }
    }

    private fun nextStep(context: Context, category: String) {
        var added: TextView? = null
        var searching: TextView? = null
        var save: TextView? = null
        var allReady: TextView? = null
        var progressBar: ProgressBar? = null
        var okBtn: TextView? = null
        var error: TextView? = null
        val dialog = context.alert {
            customView {
                verticalLayout {
                    padding = dip(10)
                    textView {
                        text = "Категория выбрана: $category"
                        padding = dip(5)
                    }
                    textView {
                        text = "Создаю папки для манги"
                        padding = dip(5)
                    }
                    added = textView {
                        text = "Новая манга добавлена в библиотеку"
                        visibility = View.GONE
                        padding = dip(5)
                    }
                    searching = textView {
                        text = "Поиск глав для манги"
                        padding = dip(5)
                        visibility = View.GONE
                    }
                    save = textView {
                        text = "Сохранение найденных глав"
                        padding = dip(5)
                        visibility = View.GONE
                    }
                    allReady = textView {
                        text = "Все работы завершены успешно"
                        padding = dip(5)
                        visibility = View.GONE
                    }
                    error = textView {
                        padding = dip(5)
                        text = "Во время работы произошла ошибка, проверьте подключение к интеренету"
                        visibility = View.GONE
                        textColor = Color.RED
                    }
                    progressBar = horizontalProgressBar {
                        isIndeterminate = true
                    }
                    okBtn = textView {
                        text = "Закрыть"
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

                Main.db.mangaDao.insert(manga)
                added?.visibility = View.VISIBLE
                searching?.visibility = View.VISIBLE

                val catalog = ManageSites.chapters(
                        manga)

                save?.visibility = View.VISIBLE

                catalog?.forEach {
                    Main.db.chapterDao.insert(it)
                }

                allReady?.visibility = View.VISIBLE
                onFinish()
            } catch (ex: Exception) {
                error?.visibility = View.VISIBLE
            } finally {
                progressBar?.visibility = View.GONE
                okBtn?.visibility = View.VISIBLE
                okBtn?.onClick {
                    dialog.dismiss()
                }
            }
        }
    }
}
