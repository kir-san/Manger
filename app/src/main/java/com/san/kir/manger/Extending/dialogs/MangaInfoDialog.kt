package com.san.kir.manger.Extending.dialogs

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewManager
import android.widget.ProgressBar
import android.widget.TextView
import com.san.kir.manger.App.Companion.context
import com.san.kir.manger.Extending.AnkoExtend.labelView
import com.san.kir.manger.Extending.AnkoExtend.positiveButton
import com.san.kir.manger.R
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.room.models.SiteCatalogElement
import com.san.kir.manger.utils.listStrToString
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.alert
import org.jetbrains.anko.browse
import org.jetbrains.anko.customView
import org.jetbrains.anko.dip
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent

class MangaInfoDialog(context: Context,
                      item: SiteCatalogElement,
                      val onFinish: () -> Unit) {
    private lateinit var updateProgress: ProgressBar
    private lateinit var name: TextView
    private lateinit var authors: TextView
    private lateinit var type: TextView
    private lateinit var statusEdition: TextView
    private lateinit var volume: TextView
    private lateinit var statusTranslate: TextView
    private lateinit var genres: TextView
    private lateinit var link: TextView
    private lateinit var about: TextView

    init {
        updateInfo(item)
        context.alert {
            customView {
                frameLayout {
                    lparams(width = matchParent, height = matchParent)

                    updateProgress = horizontalProgressBar {
                        isIndeterminate = true
                        visibility = View.GONE
                    }.lparams(width = matchParent, height = wrapContent) {
                        gravity = Gravity.TOP
                    }

                    scrollView {
                        verticalLayout {
                            lparams(width = matchParent, height = matchParent) {
                                margin = dip(10)
                            }
                            padding = dip(10)

                            labelView("Название")
                            name = text()

                            labelView("Авторы")
                            authors = text()

                            labelView("Тип")
                            type = text()

                            labelView("Статус выпуска")
                            statusEdition = text()

                            labelView("Объем")
                            volume = text()

                            labelView("Статус перевода")
                            statusTranslate = text()

                            labelView("Жанры")
                            genres = text()

                            labelView("Ссылка на источник")
                            link = textView() {
                                textSize = 15f
                                setTypeface(typeface, Typeface.BOLD)
                                textColor = Color.BLUE
                                onClick { ctx.browse(item.link) }
                            }

                            labelView("Описание")
                            about = text()
                        }
                    }
                    bind(item)
                }
            }

            if (!item.isAdded)
                positiveButton("Добавить", UI) {
                    AddMangaDialog(context, item) {
onFinish()
                    }
                }
            negativeButton("Закрыть") {}
        }.show()
    }

    private fun bind(element: SiteCatalogElement) {
        name.text = element.name
        authors.text = listStrToString(element.authors)
        type.text = element.type
        statusEdition.text = element.statusEdition
        volume.text = context.getString(
                R.string.catalog_for_one_site_prefix_volume,
                element.volume)
        statusTranslate.text = element.statusTranslate
        genres.text = listStrToString(element.genres)
        link.text = element.link
        about.text = element.about
    }

    private fun updateInfo(element: SiteCatalogElement) = async(UI) {
        try {
            updateProgress.visibility = View.VISIBLE

            bind(ManageSites.getFullElement(element).await())
        } finally {
            updateProgress.visibility = View.GONE
        }
    }

    private fun ViewManager.text() = textView {
        textSize = 15f
        setTypeface(typeface, Typeface.BOLD)
    }
}
