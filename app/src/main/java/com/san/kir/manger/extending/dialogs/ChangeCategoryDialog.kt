package com.san.kir.manger.extending.dialogs

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.models.Manga
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChangeCategoryDialog(context: Context, anchor: View?, manga: Manga) {
    init {
        GlobalScope.launch(Dispatchers.Main) {
            PopupMenu(context, anchor).apply {
                val mCat = withContext(Dispatchers.Default) { Main.db.categoryDao.loadCategories() }

                mCat.forEachIndexed { i, cat ->
                    menu.add(i, i, i, cat.name)
                }
                setOnMenuItemClickListener { item ->
                    GlobalScope.launch(Dispatchers.Default) {
                        manga.categories = mCat[item.itemId].name
                        Main.db.mangaDao.update(manga)
                    }
                    dismiss()
                    return@setOnMenuItemClickListener true
                }
                show()
            }
        }
    }
}
