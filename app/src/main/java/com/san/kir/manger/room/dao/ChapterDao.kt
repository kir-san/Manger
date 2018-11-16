package com.san.kir.manger.room.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.room.models.Chapter

@Dao
interface ChapterDao : BaseDao<Chapter> {
    @Query("SELECT * FROM chapters WHERE manga IS :manga")
    fun getItems(manga: String): List<Chapter>

    @Query("SELECT * FROM chapters WHERE site IS :site")
    fun getItem(site: String): Chapter?

    @Query("SELECT * FROM chapters WHERE manga IS :manga ORDER BY id ASC")
    fun getItemsAsc(manga: String): List<Chapter>

    @Query("SELECT * FROM chapters WHERE manga IS :manga AND isRead IS 0 ORDER BY id ASC")
    fun getItemsNotReadAsc(manga: String): List<Chapter>
}

fun ChapterDao.count(manga: String) =
        getItems(manga).size

fun ChapterDao.countNotRead(manga: String) =
        getItems(manga).filter { !it.isRead }.size

fun ChapterDao.deleteItems(manga: String) =
        delete(*getItems(manga).toTypedArray())

enum class ChapterFilter {
    ALL_READ_ASC {
        override fun inverse() = ALL_READ_DESC
    },
    NOT_READ_ASC {
        override fun inverse() = NOT_READ_DESC
    },
    IS_READ_ASC {
        override fun inverse() = IS_READ_DESC
    },
    ALL_READ_DESC {
        override fun inverse() = ALL_READ_ASC
    },
    NOT_READ_DESC {
        override fun inverse() = NOT_READ_ASC
    },
    IS_READ_DESC {
        override fun inverse() = IS_READ_ASC
    };

    abstract fun inverse(): ChapterFilter
}
