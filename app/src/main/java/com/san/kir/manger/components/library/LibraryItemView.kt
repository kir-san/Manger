package com.san.kir.manger.components.library

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.defaultSharedPreferences
import com.san.kir.ankofork.sdk28.backgroundColor
import com.san.kir.ankofork.startActivity
import com.san.kir.manger.R
import com.san.kir.manger.components.list_chapters.ListChaptersActivity
import com.san.kir.manger.extending.dialogs.LibraryItemMenu
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.MangaColumn
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.extensions.getDrawableCompat
import com.san.kir.manger.utils.extensions.onClickListener
import com.san.kir.manger.utils.extensions.onLongClickListener
import com.san.kir.manger.utils.loadImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class LibraryItemView(
    val act: LibraryActivity, private val cat: Category
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
                root.background = act.getDrawableCompat(item.color)
            } catch (ex: Resources.NotFoundException) {
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

        job = act.lifecycleScope.launch(Dispatchers.Main) {
            val countNotRead = withContext(Dispatchers.Default) {
                act.mViewModel.countNotReadChapters(item)
            }

            notReadChapters.text = act.getString(
                R.string.library_page_item_read_status,
                countNotRead
            )
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
