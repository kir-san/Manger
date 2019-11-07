package com.san.kir.manger.extending.dialogs

import android.view.View
import android.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import com.san.kir.manger.components.library.LibraryActivity
import com.san.kir.manger.room.entities.Manga
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChangeCategoryDialog(act: LibraryActivity, anchor: View?, manga: Manga) {
    init {
        act.lifecycleScope.launch(Dispatchers.Main) {
            PopupMenu(act, anchor).apply {
                val mCat = withContext(Dispatchers.Default) {
                    act.mViewModel.getCategoryItems()
                }

                mCat.forEachIndexed { i, cat ->
                    menu.add(i, i, i, cat.name)
                }
                setOnMenuItemClickListener { item ->
                    manga.categories = mCat[item.itemId].name
                    act.lifecycleScope.launch(Dispatchers.Default) {
                        act.mViewModel.update(manga)
                    }
                    dismiss()
                    return@setOnMenuItemClickListener true
                }
                show()
            }
        }
    }
}
