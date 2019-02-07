package com.san.kir.manger.components.library

import android.content.res.Resources
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.san.kir.manger.R
import com.san.kir.manger.components.list_chapters.ListChaptersActivity
import com.san.kir.manger.extending.ankoExtend.onClickListener
import com.san.kir.manger.extending.ankoExtend.onLongClickListener
import com.san.kir.manger.extending.dialogs.LibraryItemMenu
import com.san.kir.manger.extending.launchCtx
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.MangaColumn
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.getDrawableCompat
import com.san.kir.manger.utils.loadImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.startActivity

abstract class LibraryItemView(
    val act: LibraryActivity,
    private val cat: Category
) : RecyclerViewAdapterFactory.AnkoView<Manga>() {
    lateinit var root: ViewGroup
    lateinit var name: TextView
    lateinit var logo: ImageView
    lateinit var notReadChapters: TextView
    lateinit var category: TextView

    private lateinit var item: Manga

    private lateinit var listener: View.OnClickListener
    private lateinit var longListener: View.OnLongClickListener

    private var job: Job? = null
    private var job2: Job? = null

    override fun bind(item: Manga, isSelected: Boolean, position: Int) {
        this.item = item

        name.text = item.name
        if (item.color != 0) {
            try {
                val drawableCompat = act.getDrawableCompat(item.color).apply {
                    this?.alpha = 210
                }
                name.background = drawableCompat
                notReadChapters.background = drawableCompat
                root.background = act.getDrawableCompat(item.color)
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

        val key = act.getString(R.string.settings_library_show_category_key)
        val default =
            act.getString(R.string.settings_library_show_category_default) == "true"
        val isShow = act.defaultSharedPreferences.getBoolean(key, default)
        if (cat.name == CATEGORY_ALL && isShow) {
            category.text = item.categories
            category.visibility = View.VISIBLE
        }

        listener = onClickListener {
            act.startActivity<ListChaptersActivity>(MangaColumn.unic to item.unic)
        }
        longListener = onLongClickListener { view ->
            LibraryItemMenu(act, view, item)
        }
    }

    override fun onAttached() {
        root.setOnClickListener(listener)
        root.setOnLongClickListener(longListener)

        job2 = loadImage(item.logo)
            .into(logo)

        job = act.launchCtx {
            val countNotRead = act.mViewModel.countNotReadChapters(item)

            withContext(Dispatchers.Main) {
                notReadChapters.text = act.getString(
                    R.string.library_page_item_read_status,
                    countNotRead
                )
            }
        }
    }

    override fun onDetached() {
        root.setOnClickListener(null)
        root.setOnLongClickListener(null)
        notReadChapters.setOnClickListener(null)

        job?.cancel()
        job2?.cancel()
    }
}
