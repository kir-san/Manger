package com.san.kir.manger.components.add_manga

import android.content.Context
import android.content.res.Resources
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import com.san.kir.manger.R
import com.san.kir.manger.R.string
import com.san.kir.manger.extending.BaseActivity
import com.san.kir.manger.extending.ankoExtend.onClick
import com.san.kir.manger.extending.ankoExtend.typeText
import com.san.kir.manger.extending.ankoExtend.typeTextMultiLine
import com.san.kir.manger.extending.dialogs.ColorPicker
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.utils.AnkoActivityComponent
import com.san.kir.manger.utils.loadImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.checkBox
import org.jetbrains.anko.dip
import org.jetbrains.anko.editText
import org.jetbrains.anko.imageView
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.spinner
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent

class AddMangaView(private val act: AddMangaActivity) : AnkoActivityComponent() {
    private var _manga = Manga()

    private lateinit var ctx: Context
    private lateinit var name: EditText
    private lateinit var authors: EditText
    private lateinit var about: EditText
    private lateinit var categories: Spinner
    private lateinit var genres: EditText
    private lateinit var path: EditText
    private lateinit var status: Spinner
    private lateinit var site: EditText
    private lateinit var color: ImageView
    private lateinit var logoText: EditText
    private lateinit var logo: ImageView
    private lateinit var isUpdate: CheckBox

    override fun createView(ui: AnkoContext<BaseActivity>) = with(ui) {
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
                        typeTextMultiLine()
                        minLines = 1
                        maxLines = 10
                        setHorizontallyScrolling(false)
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

                    textView(text = R.string.add_manga_update)
                    isUpdate = checkBox(R.string.add_manga_update_available).lparams(
                        width = matchParent,
                        height = wrapContent
                    )

                    textView(text = string.add_manga_color)
                    color = imageView {
                        backgroundResource = android.R.color.holo_blue_bright
                    }.lparams(width = matchParent, height = dip(40))

                    textView(text = string.add_manga_logo)
                    logoText = editText().typeText()
                        .lparams(width = matchParent, height = wrapContent)
                    logo = imageView {
                    }.lparams(width = matchParent, height = dip(350))
                }
            }
            this@AddMangaView.ctx = context
        }
    }

    fun setManga(manga: Manga) {
        act.launch(Dispatchers.Default) {
            _manga = manga
            val categoryNames = act.mViewModel.getCategoryNames()
            val listStatus = listOf(
                ctx.getString(R.string.manga_status_unknown),
                ctx.getString(R.string.manga_status_continue),
                ctx.getString(R.string.manga_status_complete)
            )

            withContext(Dispatchers.Main) {
                name.setText(manga.name)
                authors.setText(manga.authors)
                about.setText(manga.about)
                genres.setText(manga.genres)
                path.setText(manga.path)
                site.setText(manga.site)
                logoText.setText(manga.logo)

                isUpdate.isChecked = manga.isUpdate

                categories.adapter = ArrayAdapter<String>(
                    categories.context,
                    android.R.layout.simple_spinner_item,
                    categoryNames
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }

                categories.setSelection(categoryNames.indexOf(manga.categories))

                status.adapter = ArrayAdapter<String>(
                    status.context,
                    android.R.layout.simple_spinner_item,
                    listStatus
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                status.setSelection(listStatus.indexOf(manga.status))

                if (manga.color != 0) {
                    color.backgroundColor = manga.color
                }
                _manga.color = manga.color
                color.onClick {
                    ColorPicker(color.context, _manga.color) { newColor ->
                        _manga.color = newColor
                        color.backgroundColor = newColor
                    }
                }

                if (manga.logo.isEmpty()) {
                    try {
                        logo.backgroundResource = if (manga.color != 0) manga.color
                        else android.R.color.holo_green_dark
                    } catch (ex: Resources.NotFoundException) {
                        logo.backgroundColor = manga.color
                    }
                } else
                    loadImage(manga.logo)
                        .onError {
                            if (manga.color != 0) {
                                logo.backgroundColor = manga.color
                            } else {
                                logo.backgroundColorResource = android.R.color.holo_green_dark
                            }
                        }
                        .into(logo)

            }
        }
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
        _manga.isUpdate = isUpdate.isChecked
        return _manga
    }
}
