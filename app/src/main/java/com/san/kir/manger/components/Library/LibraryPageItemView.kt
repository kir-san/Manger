package com.san.kir.manger.components.Library

import android.graphics.Color
import android.graphics.Typeface
import android.support.v7.widget.ListPopupWindow
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.SimpleAdapter
import com.san.kir.manger.EventBus.BinderRx
import com.san.kir.manger.Extending.AnkoExtend.bind
import com.san.kir.manger.Extending.AnkoExtend.labelView
import com.san.kir.manger.Extending.AnkoExtend.squareRelativeLayout
import com.san.kir.manger.Extending.AnkoExtend.textView
import com.san.kir.manger.Extending.AnkoExtend.textViewBold15Size
import com.san.kir.manger.R
import com.san.kir.manger.components.AddManga.AddMangaActivity
import com.san.kir.manger.components.ListChapters.ListChaptersActivity
import com.san.kir.manger.components.Storage.StorageItemFragment
import com.san.kir.manger.components.Storage.StorageUtils
import com.san.kir.manger.dbflow.models.Manga
import com.san.kir.manger.dbflow.wrapers.CategoryWrapper
import com.san.kir.manger.dbflow.wrapers.ChapterWrapper
import com.san.kir.manger.picasso.Callback
import com.san.kir.manger.picasso.NetworkPolicy
import com.san.kir.manger.picasso.Picasso
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.getDrawableCompat
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.lengthMb
import com.san.kir.manger.utils.name_SHOW_CATEGORY
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alert
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.alignParentEnd
import org.jetbrains.anko.alignParentRight
import org.jetbrains.anko.alignParentTop
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.below
import org.jetbrains.anko.browse
import org.jetbrains.anko.customView
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.dip
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.imageView
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onLongClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent

class LibraryPageItemView(private val isMain: Boolean = false,
                          private val fragment: LibraryFragment) : AnkoComponent<ViewGroup> {
    private val name_CAT = "category"
    private val mCat = CategoryWrapper.getCategories().map { mapOf(name_CAT to it.name) }

    private object _id {
        val logo = ID.generate()
        val name = ID.generate()
        val notRead = ID.generate()
    }

    var manga: Manga = Manga()

    val name = BinderRx("")
    val authors = BinderRx("")
    val readStatus = BinderRx(0 to 0)
    val logo = BinderRx("")
    val color = BinderRx(R.color.colorPrimary)
    val category = BinderRx("")
    private var _position = 0
    val selected = BinderRx(false)

    fun bind(manga: Manga, isSelect: Boolean, position: Int) {
        this.manga = manga
        name.item = manga.name
        authors.item = manga.authors
        readStatus.item =
                ChapterWrapper.countNotRead(manga.unic) to ChapterWrapper.count(manga.unic)

        logo.item = manga.logo
        if (manga.color != 0) color.item = manga.color

        category.item = manga.categories

        _position = position
        selected.item = isSelect
    }

    fun createView(parent: ViewGroup): View {
        return createView(AnkoContext.create(parent.context, parent))
    }

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {

        // список с квадратными элементами
        squareRelativeLayout {
            lparams(width = matchParent, height = wrapContent) {
                margin = dip(2)
            }
            onClick {
                if (fragment.actionMode == null)
                    startActivity<ListChaptersActivity>("manga_unic" to manga.unic)
                else
                    fragment.onListItemSelect(_position)
            }

            onLongClick {
                if (fragment.actionMode == null)
                    PopupMenu(ctx, it).apply {
                        val menu_about = menu.add(R.string.library_popupmenu_about)
                        val menu_set_cat = menu.add(R.string.library_popupmenu_set_category)
                        val menu_delete_read = menu.add(R.string.library_popupmenu_delete_read_chapters)
                        val menu_storage = menu.add(R.string.library_popupmenu_storage)
                        val menu_delete = menu.add(R.string.library_popupmenu_delete)
                        val menu_select = menu.add(R.string.library_popupmenu_select)


                        setOnMenuItemClickListener {
                            when (it) {
                                menu_about -> alert {
                                    minimumWidth = matchParent
                                    customView {
                                        frameLayout {
                                            lparams(width = matchParent, height = matchParent)
                                            scrollView {
                                                verticalLayout {
                                                    lparams(width = matchParent,
                                                            height = matchParent) {
                                                        margin = dip(10)
                                                    }
                                                    padding = dip(10)

                                                    labelView("Название")
                                                    textViewBold15Size(manga.name)

                                                    labelView("Категория")
                                                    textViewBold15Size(manga.categories)

                                                    labelView("Авторы")
                                                    textViewBold15Size(manga.authors)

                                                    labelView("Статус выпуска")
                                                    textViewBold15Size(manga.status)

                                                    labelView("Жанры")
                                                    textViewBold15Size(manga.genres)

                                                    labelView("Место хранения")
                                                    textViewBold15Size(manga.path)

                                                    labelView("Текущий объем")
                                                    textViewBold15Size(text = "Считаю2s...") {
                                                        launch(CommonPool) {
                                                            val size = getFullPath(manga.path).lengthMb
                                                            launch(UI) {
                                                                text = "$size Мб"
                                                            }
                                                        }
                                                    }

                                                    labelView("Ссылка на источник")
                                                    textViewBold15Size(manga.site) {
                                                        isClickable = true
                                                        textColor = Color.BLUE
                                                        onClick { browse(manga.site) }
                                                    }

                                                    labelView("Описание")
                                                    textViewBold15Size(manga.about)

                                                    labelView("Лого")
                                                    imageView {
                                                        lparams(width = matchParent,
                                                                height = dip(400))

                                                        scaleType = ImageView.ScaleType.FIT_CENTER

                                                        Picasso.with(this@with.ctx)
                                                                .load(manga.logo)
                                                                .networkPolicy(NetworkPolicy.OFFLINE)
                                                                .into(this, object : Callback {
                                                                    override fun onSuccess() {}
                                                                    override fun onError(e: Exception?) {
                                                                        Picasso.with(this@with.ctx)
                                                                                .load(manga.logo)
                                                                                .error(color.item)
                                                                                .into(this@imageView)
                                                                    }
                                                                })
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    positiveButton("Закрыть") { }
                                    negativeButton("Редактировать") {
                                        startActivity<AddMangaActivity>("unic" to manga.unic)
                                    }
                                }.show()
                                menu_delete -> alert(R.string.library_popupmenu_delete_message,
                                                     R.string.library_popupmenu_delete_title) {
                                    positiveButton(R.string.library_popupmenu_delete_ok) {
                                        fragment.pagerAdapter.delete(manga)
                                    }
                                    negativeButton(R.string.library_popupmenu_delete_no) {}
                                }.show()
                                menu_set_cat -> {
                                    ListPopupWindow(this@with.ctx).apply {
                                        setDropDownGravity(Gravity.CENTER_HORIZONTAL)

                                        anchorView = this@squareRelativeLayout
//                                    width = wrapContent
                                        setAdapter(SimpleAdapter(this@with.ctx,
                                                                 mCat,
                                                                 R.layout.dialog_library_item_set_category,
                                                                 arrayOf(name_CAT),
                                                                 intArrayOf(android.R.id.text1)
                                        ))
//                                    isModal = true
                                        setOnItemClickListener { _, _, i, _ ->
                                            manga.categories = mCat[i][name_CAT] as String
                                            manga.update()
                                            fragment.pagerAdapter.update()
                                            dismiss()
                                        }
                                        show()
                                    }
                                }
                                menu_select -> {
                                    fragment.onListItemSelect(_position)
                                }
                                menu_storage -> {
                                    with(StorageItemFragment()) {
                                        bind(manga, fragment)
                                        show(fragment.fragmentManager, "storage")
                                    }
                                }
                                menu_delete_read -> {
                                    StorageUtils.deleteReadChapters(this@with, manga)
                                }
                                else -> {
                                    return@setOnMenuItemClickListener false
                                }
                            }
                            return@setOnMenuItemClickListener true
                        }
                        show()
                    }
            }


            backgroundResource = com.san.kir.manger.R.color.colorPrimary
            bind(color) { background = context.getDrawableCompat(it) }

            // лого
            imageView {
                lparams(width = matchParent, height = matchParent) {
                    margin = dip(2)
                }

                id = _id.logo

                scaleType = ImageView.ScaleType.FIT_XY

                bind(logo) { uri ->
                    Picasso.with(this@with.ctx)
                            .load(uri)
                            .networkPolicy(com.san.kir.manger.picasso.NetworkPolicy.OFFLINE)
                            .into(this, object : Callback {
                                override fun onSuccess() {}
                                override fun onError(e: Exception?) {
                                    com.san.kir.manger.picasso.Picasso.with(this@with.ctx)
                                            .load(uri)
                                            .error(color.item)
                                            .into(this@imageView)
                                }
                            })
                }
            }

            // название
            textView(name) {
                lparams(width = matchParent, height = wrapContent) {
                    //                    above(_id.author)
                    padding = dip(4)
                    alignParentBottom()
                }

                id = _id.name

                backgroundResource = R.color.colorPrimary
                bind(color) { background = context.getDrawableCompat(it) }

                maxLines = 1
                typeface = Typeface.DEFAULT_BOLD
            }

            // прогресс чтения
            textView {
                lparams(width = wrapContent, height = wrapContent) {
                    alignParentTop()
                    alignParentRight()
                    padding = dip(4)
                }
                id = _id.notRead

                bind(readStatus) {
                    text = resources.getString(com.san.kir.manger.R.string.library_page_item_read_status,
                                               it.first)
                }
                backgroundResource = R.color.colorPrimary
                bind(color) { background = context.getDrawableCompat(it) }

                onClick {
                    alert {
                        this.customView {
                            verticalLayout {
                                textView("Всего глав: ${readStatus.item.second}")
                                textView("Непрочитанно: ${readStatus.item.first}")
                                textView("Прочитанно: ${readStatus.item.second - readStatus.item.first}")
                            }
                        }
                    }.show()
                }
            }

            // Отображение названия категории
            // Отображается если текущая категория основная и активированна функция в настройках
            if (isMain
                    && this@with.ctx.defaultSharedPreferences.getBoolean(name_SHOW_CATEGORY, true))
                textView(category) {
                    lparams(width = wrapContent, height = wrapContent) {
                        alignParentEnd()
                        below(_id.notRead)
                    }
                    backgroundColor = Color.BLACK
                    textColor = Color.WHITE
                }

            imageView {
                lparams(width = matchParent, height = matchParent)
                bind(selected) {
                    // В зависимости от выделения менять цвет пункта
                    backgroundColor =
                            if (it) Color.parseColor("#af34b5e4")
                            else Color.TRANSPARENT
                }
            }
        }
    }

}
