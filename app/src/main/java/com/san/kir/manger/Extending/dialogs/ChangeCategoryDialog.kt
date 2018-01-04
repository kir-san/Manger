package com.san.kir.manger.Extending.dialogs

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.DAO.update
import com.san.kir.manger.room.models.Manga

class ChangeCategoryDialog(context: Context, anchor: View?, manga: Manga) {
    init {
        PopupMenu(context, anchor).apply {
            val mCat = Main.db.categoryDao.loadCategories()

            mCat.forEachIndexed { i, cat ->
                menu.add(i, i, i, cat.name)
            }
            setOnMenuItemClickListener { item ->
                manga.categories = mCat[item.itemId].name

                Main.db.mangaDao.update(manga)
                dismiss()
                return@setOnMenuItemClickListener true
            }
            show()
        }
    }
}
