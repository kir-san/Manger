package com.san.kir.manger.components.add_manga

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.appcompat.toolbar
import com.san.kir.ankofork.backgroundColorResource
import com.san.kir.ankofork.design.themedAppBarLayout
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.margin
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.sdk28.backgroundColor
import com.san.kir.ankofork.sdk28.backgroundResource
import com.san.kir.ankofork.sdk28.checkBox
import com.san.kir.ankofork.sdk28.editText
import com.san.kir.ankofork.sdk28.imageView
import com.san.kir.ankofork.sdk28.linearLayout
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.scrollView
import com.san.kir.ankofork.sdk28.spinner
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.support.nestedScrollView
import com.san.kir.ankofork.verticalLayout
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.extending.dialogs.ColorPicker
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.utils.ActivityView
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.utils.extensions.appBar
import com.san.kir.manger.utils.extensions.applyInsetsForCutOut
import com.san.kir.manger.utils.extensions.doOnApplyWindowInstets
import com.san.kir.manger.utils.extensions.typeText
import com.san.kir.manger.utils.extensions.typeTextMultiLine
import com.san.kir.manger.utils.loadImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddMangaView(private val act: AddMangaActivity) : ActivityView() {
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
        verticalLayout {
            lparams(width = matchParent, height = matchParent)

            systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

            applyInsetsForCutOut()

            appBar(act)

            nestedScrollView {
                lparams(width = matchParent, height = matchParent)
                clipToPadding = true

                doOnApplyWindowInstets { v, insets, padding ->
                    v.updatePadding(bottom = padding.bottom + insets.systemWindowInsetBottom)
                    insets
                }

                verticalLayout {
                    lparams(width = matchParent, height = wrapContent) {
                        margin = dip(16)
                    }

                    textView(text = R.string.add_manga_name)
                    name = editText().typeText()
                        .lparams(width = matchParent, height = wrapContent) {
                            bottomMargin = dip(8)
                        }

                    textView(text = R.string.add_manga_author)
                    authors = editText().typeText()
                        .lparams(width = matchParent, height = wrapContent) {
                            bottomMargin = dip(8)
                        }

                    textView(text = R.string.add_manga_about)
                    about = editText {
                        typeTextMultiLine()
                        minLines = 1
                        maxLines = 10
                        setHorizontallyScrolling(false)
                    }.lparams(width = matchParent, height = wrapContent) {
                        bottomMargin = dip(8)
                    }

                    textView(text = R.string.add_manga_categories)
                    categories = spinner().lparams(width = matchParent, height = wrapContent) {
                        bottomMargin = dip(8)
                    }

                    textView(text = R.string.add_manga_genres)
                    genres = editText().typeText()
                        .lparams(width = matchParent, height = wrapContent) {
                            bottomMargin = dip(8)
                        }

                    textView(text = R.string.add_manga_path)
                    path = editText {
                        typeText()
                        isCursorVisible = false
                        isFocusable = false
                        isLongClickable = false
                    }.lparams(width = matchParent, height = wrapContent) {
                        bottomMargin = dip(8)
                    }

                    textView(text = R.string.add_manga_status)
                    status = spinner().lparams(width = matchParent, height = wrapContent) {
                        bottomMargin = dip(8)
                    }

                    textView(text = R.string.add_manga_site)
                    site = editText().typeText()
                        .lparams(width = matchParent, height = wrapContent) {
                            bottomMargin = dip(8)
                        }

                    textView(text = R.string.add_manga_update)
                    isUpdate = checkBox(R.string.add_manga_update_available)
                        .lparams(width = matchParent, height = wrapContent) {
                            bottomMargin = dip(8)
                        }

                    textView(text = R.string.add_manga_color)
                    color = imageView {
                        backgroundResource = android.R.color.holo_blue_bright
                    }.lparams(width = matchParent, height = dip(40)) {
                        bottomMargin = dip(8)
                    }

                    textView(text = R.string.add_manga_logo)
                    logoText = editText().typeText()
                        .lparams(width = matchParent, height = wrapContent)
                    logo = imageView().lparams(width = matchParent, height = dip(350))
                }
            }
            this@AddMangaView.ctx = context
        }
    }


    @SuppressLint("SetTextI18n")
    suspend fun setManga(manga: Manga) {
        _manga = manga
        val categoryNames = act.mViewModel.getCategoryNames()

        val listStatus = listOf(
            ctx.getString(R.string.manga_status_unknown),
            ctx.getString(R.string.manga_status_continue),
            ctx.getString(R.string.manga_status_complete)
        )

        name.setText(manga.name)
        authors.setText(manga.authors)
        about.setText(manga.about)
        genres.setText(manga.genres)
        path.setText(manga.path)
        site.setText(manga.host + manga.shortLink)
        logoText.setText(manga.logo)

        isUpdate.isChecked = manga.isUpdate

        categories.adapter = ArrayAdapter(
            categories.context, android.R.layout.simple_spinner_item, categoryNames
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        categories.setSelection(categoryNames.indexOf(manga.categories))

        status.adapter = ArrayAdapter<String>(
            status.context, android.R.layout.simple_spinner_item, listStatus
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

        loadImage(manga.logo)
            .beforeTry {
                try {
                    logo.backgroundResource = if (manga.color != 0) manga.color
                    else android.R.color.holo_green_dark
                    true
                } catch (ex: Resources.NotFoundException) {
                    logo.backgroundColor = manga.color
                    false
                }
            }
            .into(logo)
    }

    fun getManga(): Manga {
        _manga.name = name.text.toString()
        _manga.authors = authors.text.toString()
        _manga.about = about.text.toString()
        _manga.categories = categories.selectedItem.toString()
        _manga.genres = genres.text.toString()
        _manga.path = path.text.toString()
        _manga.status = status.selectedItem.toString()
        _manga.logo = logoText.text.toString()
        _manga.isUpdate = isUpdate.isChecked
        return _manga
    }
}
