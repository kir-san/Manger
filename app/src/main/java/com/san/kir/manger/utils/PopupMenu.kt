package com.san.kir.manger.utils

import android.view.*
import android.widget.PopupMenu
import com.san.kir.manger.R
import com.san.kir.manger.components.AddManga.AddMangaActivity
import com.san.kir.manger.components.Storage.StorageItem
import org.jetbrains.anko.*


object PopupMenu {
    fun storage(v: View?, item: StorageItem) {
        PopupMenu(v?.context, v).apply {
            var menu_add: MenuItem? = null
            var menu_del_read_chapters: MenuItem? = null
            if (item.isNew) {
                menu_add = menu.add(R.string.storage_popmenu_add)
            } else {
                menu_del_read_chapters = menu.add(R.string.storage_popmenu_del_read_chapters)
            }

            setOnMenuItemClickListener {
                when (it.itemId) {
                    menu_add?.itemId -> {
                        v!!.context.startActivity<AddMangaActivity>(StorageItem::class.java.canonicalName to item)
                        return@setOnMenuItemClickListener true
                    }
                    menu_del_read_chapters?.itemId -> {
                        val result = delAllReadChapters(item.name)
                        v!!.context.longToast("Вы прочитали ${result.max} глав.\n" +
                                                      "Мы смогли удалить ${result.current}")
                        return@setOnMenuItemClickListener true
                    }
                    else -> {
                        return@setOnMenuItemClickListener false
                    }
                }
            }
            show()
        }
    }
}
