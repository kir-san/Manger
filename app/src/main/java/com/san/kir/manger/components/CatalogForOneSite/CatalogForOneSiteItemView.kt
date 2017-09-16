package com.san.kir.manger.components.CatalogForOneSite

import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView.ScaleType.FIT_XY
import com.san.kir.manger.App
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.Extending.AnkoExtend.bind
import com.san.kir.manger.Extending.AnkoExtend.labelView
import com.san.kir.manger.Extending.AnkoExtend.textView
import com.san.kir.manger.R
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.dbflow.models.Chapter
import com.san.kir.manger.dbflow.models.SiteCatalogElement
import com.san.kir.manger.dbflow.models.toManga
import com.san.kir.manger.dbflow.wrapers.CategoryWrapper
import com.san.kir.manger.dbflow.wrapers.toStringList
import com.san.kir.manger.utils.DIR
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.createDirs
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.listStrToString
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import kotlinx.coroutines.experimental.run
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alert
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.alignParentRight
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.browse
import org.jetbrains.anko.centerVertically
import org.jetbrains.anko.customView
import org.jetbrains.anko.dip
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftOf
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.selector
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView
import org.jetbrains.anko.toast
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent
import java.util.regex.Pattern

class CatalogForOneSiteItemView : AnkoComponent<ViewGroup> {
    private object _id {
        val add = ID.generate()
        val statusEdition = ID.generate()
    }

    private lateinit var element: SiteCatalogElement
    val name = Binder("")
    val link = Binder("")
    val type = Binder("")
    val authors = Binder("")
    val statusEdition = Binder("")
    val statusTranslate = Binder("")
    val volume = Binder(0)
    val genres = Binder("")
    val about = Binder("")
    val isAdded = Binder(false to false)
    var isBackground = Binder(false)
    val isUpdate = Binder(false)

    fun createView(parent: ViewGroup): View {
        return createView(AnkoContext.create(parent.context, parent))
    }

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        frameLayout {
            lparams(width = matchParent, height = dip(50))
            bind(isBackground) {
                backgroundColor =
                        if (it) Color.LTGRAY
                        else Color.WHITE
            }

            relativeLayout {
                lparams(width = matchParent, height = matchParent)
                isClickable = true

                bind(isAdded) { (one, two) ->
                    backgroundColor =
                            if (one && two) Color.DKGRAY
                            else Color.TRANSPARENT
                }

                // Название
                textView(name) {
                    lparams(width = matchParent, height = wrapContent) {
                        margin = dip(4)
                        leftOf(_id.add)
                    }
                    maxLines = 1
                    textSize = 16f
                    textColor = Color.BLACK
                }

                // Кнопка добавить к себе в коллекцию
                val addBtn = imageView {
                    id = _id.add
                    isClickable = true
                    scaleType = FIT_XY
                    setImageResource(android.R.drawable.ic_input_add)

                    onClick {
                        val progress = ProgressDialog(this@with.ctx).apply {
                            setMessage(App.context.getString(R.string.catalog_for_one_site_progress_title))
                            isIndeterminate = true
                            setCanceledOnTouchOutside(false)
                            show()
                        }

                        val categories = toStringList(CategoryWrapper.asyncGetCategories())

                        // Получение пути из короткой ссылки
                        val pat = Pattern.compile("[a-z/0-9]+-").matcher(element.shotLink)
                        if (pat.find())
                            element.shotLink = element.shotLink
                                    .removePrefix(pat.group()).removeSuffix(".html")
                        val path = "${DIR.MANGA}/${element.catalogName}/${element.shotLink}"

                        selector(title = App.context.getString(R.string.catalog_for_one_site_selector_item),
                                 items = categories) { _, index ->
                            // Сохраняем полученную категорию
                            val category = categories[index]

                            // Создание отсутствующих папок
                            createDirs(getFullPath(path))

                            // Получение обЪекта манга для добавления в базу данных
                            element.toManga(category = category, path = path).insert()
                            isAdded.item = isAdded.item.first to true
                        }

                        val catalog = com.san.kir.manger.components.Parsing.ManageSites.asyncGetChapters(
                                newSingleThreadContext("catalogContext"),
                                element,
                                path)
                        val chapters = mutableListOf<Chapter>()

                        try {
                            for (item in catalog) {
                                run(newSingleThreadContext("catalogContext")) {
                                    chapters.add(item)
                                }
                            }
                        } catch (es: Throwable) {
                            progress.dismiss()
                            App.context.toast(R.string.catalog_for_one_site_on_error_load)
                        } finally {
                            chapters.reversed().forEach(Chapter::insert)
                            /*toast(App.context.getString(
                                    string.catalog_for_one_site_load_ok,
                                    element.name))*/
                            progress.dismiss()
                            isAdded.item = true to isAdded.item.second
                        }
                    }


                    bind(isAdded) { (one, two) ->
                        visibility = if (one && two) View.INVISIBLE else View.VISIBLE
                    }
                }.lparams(width = dip(40), height = dip(40)) {
                    alignParentRight()
                    centerVertically()
                }

                // Авторы
                textView(authors) {
                    lparams(width = matchParent, height = wrapContent) {
                        alignParentBottom()
                        margin = dip(4)
                        leftOf(_id.statusEdition)
                    }
                    maxLines = 1
                }

                // Статус выпуска
                textView(statusEdition) {
                    lparams(width = wrapContent, height = wrapContent) {
                        alignParentBottom()
                        margin = dip(4)
                        leftOf(_id.add)
                    }
                    id = _id.statusEdition
                }

                onClick {
                    // открыть окно
                    alert {
                        minimumWidth = matchParent
                        customView {
                            frameLayout {
                                lparams(width = matchParent, height = matchParent)
                                horizontalProgressBar {
                                    lparams(width = matchParent, height = wrapContent) {
                                        gravity = Gravity.TOP
                                    }
                                    isIndeterminate = true
                                    bind(isUpdate) {
                                        visibility = if (it) View.VISIBLE else View.GONE
                                    }
                                    id = ID.generate()
                                }
                                scrollView {
                                    verticalLayout {
                                        lparams(width = matchParent, height = matchParent) {
                                            margin = dip(10)
                                        }
                                        padding = dip(10)

                                        textView("Обновить") {
                                            lparams() {

                                            }
                                        }

                                        labelView("Название")
                                        textView(name) {
                                            textSize = 15f
                                            setTypeface(typeface, Typeface.BOLD)
                                        }

                                        labelView("Авторы")
                                        textView(authors) {
                                            textSize = 15f
                                            setTypeface(typeface, Typeface.BOLD)
                                        }

                                        labelView("Тип")
                                        textView(type) {
                                            textSize = 15f
                                            setTypeface(typeface, Typeface.BOLD)
                                        }

                                        labelView("Статус выпуска")
                                        textView(statusEdition) {
                                            textSize = 15f
                                            setTypeface(typeface, Typeface.BOLD)
                                        }

                                        labelView("Объем")
                                        textView {
                                            textSize = 15f
                                            setTypeface(typeface, Typeface.BOLD)
                                            bind(volume) {
                                                text = context.getString(
                                                        com.san.kir.manger.R.string.catalog_for_one_site_prefix_volume,
                                                        it
                                                )
                                            }
                                        }

                                        labelView("Статус перевода")
                                        textView(statusTranslate) {
                                            textSize = 15f
                                            setTypeface(typeface, Typeface.BOLD)
                                        }

                                        labelView("Жанры")
                                        textView(genres) {
                                            textSize = 15f
                                            setTypeface(typeface, Typeface.BOLD)
                                        }

                                        labelView("Ссылка на источник")
                                        textView(link) {
                                            textSize = 15f
                                            setTypeface(typeface, Typeface.BOLD)
                                            isClickable = true
                                            textColor = Color.BLUE
                                            onClick { browse(element.link) }
                                        }

                                        labelView("Описание")
                                        textView(about) {
                                            textSize = 15f
                                            setTypeface(typeface, Typeface.BOLD)
                                        }
                                    }
                                }
                            }
                        }
                        if (!isAdded.item.first && !isAdded.item.second)
                            positiveButton("Добавить") {
                                addBtn.performClick()
                            }

                        negativeButton("Закрыть") {}
                    }.show()
                    updateInfo()
                }
            }
        }
    }

    private var _type = 0
    fun bind(element: SiteCatalogElement, viewType: Int = _type) {
        name.item = element.name
        link.item = element.link
        type.item = element.type
        authors.item = listStrToString(element.authors)
        statusEdition.item = element.statusEdition
        statusTranslate.item = element.statusTranslate
        volume.item = element.volume
        genres.item = listStrToString(element.genres)
        about.item = element.about
        isBackground.item = viewType == 1
        isAdded.item = element.isAdded to element.isAdded
        this.element = element
        _type = viewType
        isUpdate.item = false
    }

    private fun updateInfo() {
        launch(UI) {
            isUpdate.item = true
            this@CatalogForOneSiteItemView.
                    bind(ManageSites.getFullElement(element))
        }
    }
}
