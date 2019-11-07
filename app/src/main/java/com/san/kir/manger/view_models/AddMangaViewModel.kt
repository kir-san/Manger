package com.san.kir.manger.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.san.kir.manger.repositories.CategoryRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.room.entities.Manga

class AddMangaViewModel(app: Application) : AndroidViewModel(app) {
    private val mMangaRepository = MangaRepository(app)
    private val mCategoryRepository = CategoryRepository(app)

    fun getMangaItem(mangaUnic: String): Manga {
        return mMangaRepository.getItem(mangaUnic)
    }

    suspend fun update(manga: Manga) = mMangaRepository.update(manga)

    fun getCategoryNames(): List<String> {
        return mCategoryRepository.categoryNames()
    }
}

