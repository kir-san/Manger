package com.san.kir.manger.components.AddManga

import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import com.san.kir.manger.Extending.AnkoExtend.photoView
import com.san.kir.manger.Extending.AnkoExtend.typeText
import com.san.kir.manger.Extending.AnkoExtend.typeTextMultiLine
import com.san.kir.manger.Extending.dialogs.ColorPicker
import com.san.kir.manger.R.string
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.photoview.PhotoView
import com.san.kir.manger.photoview.onError
import com.san.kir.manger.picasso.NetworkPolicy.OFFLINE
import com.san.kir.manger.picasso.Picasso
import com.san.kir.manger.room.DAO.categoryNames
import com.san.kir.manger.room.models.Manga
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.editText
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.spinner
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent

class AddMangaView : AnkoComponent<AddMangaActivity> {

    private companion object {
        private val listStatus = listOf("Не выбрано",
                                        "Выпуск продолжается",
                                        "Выпуск завершен")
    }

    private val categoryDao = Main.db.categoryDao
    private var _manga = Manga()

    private lateinit var name: EditText
    private lateinit var authors: EditText
    private lateinit var about: EditText
    private lateinit var categories: Spinner
    private lateinit var genres: EditText
    private lateinit var path: EditText
    private lateinit var status: Spinner
    private lateinit var site: EditText
    private lateinit var color: PhotoView
    private lateinit var logoText: EditText
    private lateinit var logo: PhotoView

    fun createView(parent: AddMangaActivity) = createView(AnkoContext.create(parent, parent))

    override fun createView(ui: AnkoContext<AddMangaActivity>) = with(ui) {
        linearLayout {
            lparams(width = matchParent, height = matchParent)

            scrollView {
                lparams(width = matchParent, height = matchParent)

                verticalLayout {
                    lparams(width = matchParent, height = wrapContent)

                    textView(text = string.add_manga_name)
                    name = editText().typeText()
                            .lparams(width = matchParent, height = wrapContent)

                    textView(text = string.add_manga_author)
                    authors = editText().typeText()
                            .lparams(width = matchParent, height = wrapContent)

                    textView(text = string.add_manga_about)
                    about = editText {
                        setEms(10)
                        typeTextMultiLine()
                    }.lparams(width = matchParent, height = wrapContent)

                    textView(text = string.add_manga_categories)
                    categories = spinner {
                    }.lparams(width = matchParent, height = wrapContent)

                    textView(text = string.add_manga_genres)
                    genres = editText().typeText()
                            .lparams(width = matchParent, height = wrapContent)

                    textView(text = string.add_manga_path)
                    path = editText {
                        typeText()
                        isCursorVisible = false
                        isFocusable = false
                        isLongClickable = false
                    }.lparams(width = matchParent, height = wrapContent)

                    textView(text = string.add_manga_status)
                    status = spinner {
                    }.lparams(width = matchParent, height = wrapContent)

                    textView(text = string.add_manga_site)
                    site = editText().typeText()
                            .lparams(width = matchParent, height = wrapContent)

                    textView(text = string.add_manga_color)
                    color = photoView {
                        backgroundResource = android.R.color.holo_blue_bright
                    }.lparams(width = matchParent, height = dip(40))

                    textView(text = string.add_manga_logo)
                    logoText = editText().typeText()
                            .lparams(width = matchParent, height = wrapContent)
                    logo = photoView {
                    }.lparams(width = matchParent, height = dip(350))
                }
            }
        }
    }

    fun setManga(manga: Manga) {
        _manga = manga
        name.setText(manga.name)
        authors.setText(manga.authors)
        about.setText(manga.about)

        categories.adapter = ArrayAdapter<String>(categories.context,
                                                  android.R.layout.simple_spinner_item,
                                                  categoryDao.categoryNames()).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        categories.setSelection(categoryDao.categoryNames().indexOf(manga.categories))

        genres.setText(manga.genres)
        path.setText(manga.path)

        status.adapter = ArrayAdapter<String>(status.context,
                                              android.R.layout.simple_spinner_item,
                                              listStatus).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        status.setSelection(listStatus.indexOf(manga.status))

        site.setText(manga.site)

        if (manga.color != 0) {
            color.backgroundColor = manga.color
        }
        _manga.color = manga.color
        color.onClick {
            ColorPicker(color.context, _manga.color) {
                _manga.color = it
                color.backgroundColor = it
            }
        }

        logoText.setText(manga.logo)

        if (manga.logo.isEmpty()) {
            logo.backgroundResource = if (manga.color != 0) manga.color
            else android.R.color.holo_green_dark
        } else
            Picasso.with(logo.context)
                    .load(manga.logo)
                    .networkPolicy(OFFLINE)
                    .into(logo, onError {
                        Picasso.with(logo.context)
                                .load(manga.logo)
                                .error(if (manga.color != 0) manga.color
                                       else android.R.color.holo_green_dark)
                                .into(logo)
                    })
    }

    fun getManga(): Manga {
        _manga.name = name.text.toString()
        _manga.authors = authors.text.toString()
        _manga.about = about.text.toString()
        _manga.categories = categories.selectedItem.toString()
        _manga.genres = genres.text.toString()
        _manga.path = path.text.toString()
        _manga.status = status.selectedItem.toString()
        _manga.site = site.text.toString()
        _manga.logo = logoText.text.toString()
        return _manga
    }
}
