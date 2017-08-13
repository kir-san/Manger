package com.san.kir.manger.components.AddManga

import android.R.color
import android.support.v7.widget.ListPopupWindow
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SimpleAdapter
import com.san.kir.manger.EventBus.BinderRx
import com.san.kir.manger.Extending.AnkoExtend.bind
import com.san.kir.manger.Extending.AnkoExtend.editText
import com.san.kir.manger.Extending.AnkoExtend.photoView
import com.san.kir.manger.Extending.AnkoExtend.typeText
import com.san.kir.manger.Extending.AnkoExtend.typeTextMultiLine
import com.san.kir.manger.R
import com.san.kir.manger.R.string
import com.san.kir.manger.dbflow.models.Manga
import com.san.kir.manger.dbflow.wrapers.CategoryWrapper
import com.san.kir.manger.dbflow.wrapers.toStringList
import com.san.kir.manger.picasso.Callback
import com.san.kir.manger.picasso.NetworkPolicy.OFFLINE
import com.san.kir.manger.picasso.Picasso
import com.san.kir.manger.utils.getDrawableCompat
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.dip
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onItemSelectedListener
import org.jetbrains.anko.spinner
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent
import java.lang.Exception
import java.util.*

class AddMangaView : AnkoComponent<AddMangaActivity> {

    private companion object {
        private val namE_IMAGE = "image"
        private val mColors: ArrayList<Map<String, Int>> =
                arrayListOf(
                        mapOf(namE_IMAGE to color.holo_blue_bright),
                        mapOf(namE_IMAGE to color.holo_blue_dark),
                        mapOf(namE_IMAGE to color.holo_blue_light),
                        mapOf(namE_IMAGE to color.holo_green_dark),
                        mapOf(namE_IMAGE to color.holo_green_light),
                        mapOf(namE_IMAGE to color.holo_orange_dark),
                        mapOf(namE_IMAGE to color.holo_orange_light),
                        mapOf(namE_IMAGE to color.holo_purple),
                        mapOf(namE_IMAGE to color.holo_red_dark),
                        mapOf(namE_IMAGE to color.holo_red_light)
                )
        private val listStatus = listOf("Не выбрано",
                                        "Выпуск продолжается",
                                        "Выпуск завершен")
    }

    private var _manga = Manga()
    private val name = BinderRx(_manga.name)
    private val authors = BinderRx(_manga.authors)
    private val logo = BinderRx(_manga.logo)
    private val about = BinderRx(_manga.about)
    private val categories = BinderRx(_manga.categories)
    //    val category = BinderRx(categories.item)
    private val genres = BinderRx(_manga.genres)
    private val path = BinderRx(_manga.path)
    private val status = BinderRx(_manga.status)
    private val site = BinderRx(_manga.site)
    private val color = BinderRx(_manga.color)


    fun setManga(manga: Manga) {
        _manga = manga
        name.item = manga.name
        authors.item = manga.authors
        logo.item = manga.logo
        about.item = manga.about
        categories.item = manga.categories
        genres.item = manga.genres
        path.item = manga.path
        status.item = manga.status
        site.item = manga.site
        color.item = manga.color
    }

    fun getManga(): Manga {
        _manga.name = name.item
        _manga.authors = authors.item
        _manga.logo = logo.item
        _manga.about = about.item
        _manga.categories = categories.item
        _manga.genres = genres.item
        _manga.path = path.item
        _manga.status = status.item
        _manga.site = site.item
        _manga.color = color.item
        return _manga
    }

    fun createView(parent: AddMangaActivity): View {
        return createView(AnkoContext.create(parent, parent))
    }

    override fun createView(ui: AnkoContext<AddMangaActivity>) = with(ui) {
        linearLayout {
            lparams(width = matchParent, height = matchParent)

            scrollView {
                lparams(width = matchParent, height = matchParent)

                verticalLayout {
                    lparams(width = matchParent, height = wrapContent)

                    textView(text = string.add_manga_name)
                    editText(name).lparams(width = matchParent, height = wrapContent)
                            .typeText()

                    textView(text = string.add_manga_author)
                    editText(authors).lparams(width = matchParent, height = wrapContent)
                            .typeText()

                    textView(text = string.add_manga_about)
                    editText(about).lparams(width = matchParent, height = wrapContent)
                            .typeTextMultiLine()
                            .setEms(10)

                    textView(text = string.add_manga_categories)
                    spinner {
                        lparams(width = matchParent, height = wrapContent)
                        adapter = ArrayAdapter<String>(context,
                                                       android.R.layout.simple_spinner_item,
                                                       toStringList(CategoryWrapper.getCategories())).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
                        bind(categories) {
                            setSelection(toStringList(CategoryWrapper.getCategories()).indexOf(it))
                        }
                        onItemSelectedListener {
                            onItemSelected { _, _, _, _ ->
                                categories.item = selectedItem.toString()
                            }
                        }
                    }

                    textView(text = string.add_manga_genres)
                    editText(genres).lparams(width = matchParent, height = wrapContent)
                            .typeText()

                    textView(text = string.add_manga_path)
                    editText(path) {
                        lparams(width = matchParent, height = wrapContent)
                        typeText()
                        isCursorVisible = false
                        isFocusable = false
                        isLongClickable = false
                    }

                    textView(text = string.add_manga_status)
                    spinner {
                        lparams(width = matchParent, height = wrapContent)
                        adapter = ArrayAdapter<String>(context,
                                                       android.R.layout.simple_spinner_item,
                                                       listStatus).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
                        bind(status) {
                            setSelection(listStatus.indexOf(it))
                        }
                        onItemSelectedListener {
                            onItemSelected { _, _, _, _ ->
                                status.item = selectedItem.toString()
                            }
                        }
                    }

                    textView(text = string.add_manga_site)
                    editText(site).lparams(width = matchParent, height = wrapContent)
                            .typeText()

                    textView(text = string.add_manga_color)
                    photoView {
                        lparams(width = matchParent, height = dip(40))
                        isClickable = true

                        bind(color) {
                            background = getDrawableCompat(if (it != 0) it
                                                           else android.R.color.holo_blue_bright)
                        }

                        onClick {
                            ListPopupWindow(this@with.ctx).apply {

                                setDropDownGravity(Gravity.CENTER_HORIZONTAL)

                                anchorView = this@photoView
                                width = wrapContent
                                setAdapter(SimpleAdapter(this@with.ctx,
                                                         mColors,
                                                         R.layout.dialog_add_manga_popup_color_picker,
                                                         arrayOf(namE_IMAGE),
                                                         intArrayOf(R.id.dialog_add_manga_popup_color_item)))
                                isModal = true
                                setOnItemClickListener { _, _, i, _ ->
                                    color.item = mColors[i][namE_IMAGE] as Int
                                    dismiss()
                                }
                                show()
                            }
                        }
                    }

                    textView(text = string.add_manga_logo)
                    editText(logo).lparams(width = matchParent, height = wrapContent).typeText()
                    photoView {
                        lparams(width = matchParent, height = dip(350))
                        bind(logo) {
                            Picasso.with(this@with.ctx)
                                    .load(it)
                                    .networkPolicy(OFFLINE)
                                    .into(this, object : Callback {
                                        override fun onError(e: Exception?) {
                                            Picasso.with(this@with.ctx)
                                                    .load(it)
                                                    .error(if (color.item != 0) color.item
                                                           else android.R.color.holo_green_dark)
                                                    .into(this@photoView)
                                        }

                                        override fun onSuccess() {}
                                    })
                        }
                    }
                }
            }
        }
    }
}
