package com.san.kir.manger.components.library

import android.content.res.Resources
import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.san.kir.manger.R
import com.san.kir.manger.components.listChapters.ListChaptersActivity
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.extending.dialogs.LibraryItemMenu
import com.san.kir.manger.room.dao.count
import com.san.kir.manger.room.dao.countNotRead
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.MangaColumn
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.getDrawableCompat
import com.san.kir.manger.utils.loadImage
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.customView
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onLongClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout

abstract class LibraryItemView(
    val act: LibraryActivity,
    private val cat: Category
) : RecyclerViewAdapterFactory.AnkoView<Manga>() {
    val chapters = Main.db.chapterDao
    lateinit var root: FrameLayout
    lateinit var name: TextView
    lateinit var logo: ImageView
    lateinit var notReadChapters: TextView
    lateinit var selected: ImageView
    lateinit var category: TextView
    lateinit var isUpdate: ProgressBar

    override fun bind(item: Manga, isSelected: Boolean, position: Int) {
        launch(UI) {
            val context = root.context
            root.onClick {
                if (act.actionMode.hasFinish())
                    context.startActivity<ListChaptersActivity>(MangaColumn.unic to item.unic)
                else
                    act.onListItemSelect(position)
            }

            root.onLongClick { view ->
                if (act.actionMode.hasFinish())
                    LibraryItemMenu(context, view, item, act, position)
            }

            name.text = item.name

            if (item.color != 0) {
                try {
                    val drawableCompat = context.getDrawableCompat(item.color).apply {
                        this?.alpha = 210
                    }
                    name.background = drawableCompat
                    notReadChapters.background = drawableCompat
                    root.background = context.getDrawableCompat(item.color)
                } catch (ex: Resources.NotFoundException) {
                    val newColor = Color.argb(
                        210,
                        Color.red(item.color),
                        Color.green(item.color),
                        Color.blue(item.color)
                    )
                    name.backgroundColor = newColor
                    notReadChapters.backgroundColor = newColor
                    root.backgroundColor = item.color
                }
            }

            if (item.logo.isNotEmpty()) {
                loadImage(item.logo) {
                    into(logo)
                }
            }

            val countNotRead = chapters.countNotRead(item.unic)
            val count = chapters.count(item.unic)

            notReadChapters.text = context.getString(
                com.san.kir.manger.R.string.library_page_item_read_status,
                countNotRead
            )
            notReadChapters.onClick {
                context.alert {
                    this.customView {
                        verticalLayout {
                            textView(context.getString(R.string.library_all_chapters, count))
                            textView(
                                context.getString(
                                    R.string.library_not_read_chapters,
                                    countNotRead
                                )
                            )
                            textView(
                                context.getString(
                                    R.string.library_read_chapters,
                                    count - countNotRead
                                )
                            )
                        }
                    }
                }.show()
            }

            selected.backgroundColor =
                    if (isSelected) Color.parseColor("#af34b5e4")
                    else Color.TRANSPARENT


            val key = context.getString(R.string.settings_library_show_category_key)
            val default =
                context.getString(R.string.settings_library_show_category_default) == "true"
            val isShow = context.defaultSharedPreferences.getBoolean(key, default)
            if (cat.name == CATEGORY_ALL && isShow) {
                category.text = item.categories
                category.visibility = View.VISIBLE
            }
        }
    }
}
