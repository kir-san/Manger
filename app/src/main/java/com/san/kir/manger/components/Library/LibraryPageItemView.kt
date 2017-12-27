package com.san.kir.manger.components.Library

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.Extending.AnkoExtend.bind
import com.san.kir.manger.Extending.AnkoExtend.labelView
import com.san.kir.manger.Extending.AnkoExtend.squareRelativeLayout
import com.san.kir.manger.Extending.AnkoExtend.textView
import com.san.kir.manger.Extending.AnkoExtend.textViewBold15Size
import com.san.kir.manger.R
import com.san.kir.manger.components.AddManga.AddMangaActivity
import com.san.kir.manger.components.ListChapters.ListChaptersActivity
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.components.Storage.StorageDialogFragment
import com.san.kir.manger.components.Storage.StorageUtils
import com.san.kir.manger.picasso.Callback
import com.san.kir.manger.picasso.NetworkPolicy
import com.san.kir.manger.picasso.Picasso
import com.san.kir.manger.room.DAO.count
import com.san.kir.manger.room.DAO.countNotRead
import com.san.kir.manger.room.DAO.update
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.NAME_SHOW_CATEGORY
import com.san.kir.manger.utils.formatDouble
import com.san.kir.manger.utils.getDrawableCompat
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.lengthMb
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
                          private val act: LibraryActivity) : AnkoComponent<ViewGroup> {
    private val categories = Main.db.categoryDao
    private val mCat = categories.loadCategories()

    private object _id {
        val logo = ID.generate()
        val name = ID.generate()
        val notRead = ID.generate()
    }

    val chapters = Main.db.chapterDao

    var manga: Manga = Manga()

    private val name = Binder("")
    val authors = Binder("")
    private val readStatus = Binder(0 to 0)
    val logo = Binder("")
    val color = Binder(R.color.colorPrimary)
    val category = Binder("")
    private var _position = 0
    private val selected = Binder(false)

    fun bind(manga: Manga, isSelect: Boolean, position: Int) {
        this.manga = manga
        name.item = manga.name
        authors.item = manga.authors
        readStatus.item =
                chapters.countNotRead(manga.unic) to chapters.count(manga.unic)

        logo.item = manga.logo
        if (manga.color != 0) color.item = manga.color

        category.item = manga.categories

        _position = position
        selected.item = isSelect
    }

    fun createView(parent: ViewGroup) = createView(AnkoContext.create(parent.context, parent))

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {

        // список с квадратными элементами
        squareRelativeLayout {
            lparams(width = matchParent, height = wrapContent) {
                margin = dip(2)
            }
            onClick {
                if (act.actionMode.hasFinish())
                    startActivity<ListChaptersActivity>("manga_unic" to manga.unic)
                else
                    act.onListItemSelect(_position)
            }

            onLongClick { view ->
                if (act.actionMode.hasFinish())
                    PopupMenu(ctx, view).apply {
                        val menuAbout = menu.add(R.string.library_popupmenu_about)
                        val menuSetCat = menu.add(R.string.library_popupmenu_set_category)
                        val menuDeleteRead = menu.add(R.string.library_popupmenu_delete_read_chapters)
                        val menuStorage = menu.add(R.string.library_popupmenu_storage)
                        val menuDelete = menu.add(R.string.library_popupmenu_delete)
                        val menuSelect = menu.add(R.string.library_popupmenu_select)


                        setOnMenuItemClickListener {
                            when (it) {
                                menuAbout -> alert {
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
                                                        launch {
                                                            val size = getFullPath(manga.path).lengthMb
                                                            launch(UI) {
                                                                text = this@with.ctx.getString(R.string.library_page_item_size,
                                                                                               formatDouble(
                                                                                                       size))
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
                                                        scaleType = ImageView.ScaleType.FIT_CENTER
                                                        if (manga.logo.isNotEmpty())
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
                                                        else
                                                            backgroundResource = color.item
                                                    }.lparams(width = matchParent,
                                                              height = dip(400))
                                                }
                                            }
                                        }
                                    }
                                    positiveButton("Закрыть") { }
                                    negativeButton("Редактировать") {
                                        startActivity<AddMangaActivity>("unic" to manga.unic)
                                    }
                                }.show()
                                menuDelete -> alert(R.string.library_popupmenu_delete_message,
                                                    R.string.library_popupmenu_delete_title) {
                                    positiveButton(R.string.library_popupmenu_delete_ok) {
                                        act.pagerAdapter.delete(manga)
                                    }
                                    negativeButton(R.string.library_popupmenu_delete_no) {}
                                }.show()
                                menuSetCat -> {
                                    PopupMenu(ctx, view).apply {
                                        mCat.forEachIndexed { i, cat ->
                                            menu.add(i, i, i, cat.name)
                                        }
                                        setOnMenuItemClickListener { item ->
                                            manga.categories = mCat[item.itemId].name
                                            Main.db.mangaDao.update(manga)
                                            act.pagerAdapter.update()
                                            dismiss()
                                            return@setOnMenuItemClickListener true
                                        }
                                        show()
                                    }
                                }
                                menuSelect -> {
                                    act.onListItemSelect(_position)
                                }
                                menuStorage -> {
                                    StorageDialogFragment().apply {
                                        bind(manga, act)
                                        show(act.supportFragmentManager, "storage")
                                    }
                                }
                                menuDeleteRead -> {
                                    StorageUtils.deleteReadChapters(this@with.ctx, manga) { }
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
            bind(color) {
                try {
                    background = context.getDrawableCompat(it)
                } catch (ex: Resources.NotFoundException) {
                    backgroundColor = it
                }
            }
            // лого
            imageView {
                lparams(width = matchParent, height = matchParent) {
                    margin = dip(2)
                }

                id = _id.logo

                scaleType = ImageView.ScaleType.FIT_XY

                bind(logo) { uri ->
                    if (uri.isNotEmpty())
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

            textView(name) {
                lparams(width = matchParent, height = wrapContent) {
                    //                    above(_id.author)
                    padding = dip(4)
                    alignParentBottom()
                }

                id = _id.name

                backgroundResource = R.color.colorPrimary
                bind(color) {
                    try {
                        background = context.getDrawableCompat(it)
                    } catch (ex: Resources.NotFoundException) {
                        backgroundColor = it
                    }
                }
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
                bind(color) {  try {
                    background = context.getDrawableCompat(it)
                } catch (ex: Resources.NotFoundException) {
                    backgroundColor = it
                } }

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
                    && this@with.ctx.defaultSharedPreferences.getBoolean(NAME_SHOW_CATEGORY, true))
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
