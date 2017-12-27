package com.san.kir.manger.room.DAO

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.room.models.Chapter

@Dao
interface ChapterDao : BaseDao<Chapter> {
    @Query("SELECT * FROM chapters WHERE manga IS :arg0")
    fun loadChapters(manga: String): List<Chapter>

    @Query("SELECT * FROM chapters WHERE site IS :arg0")
    fun loadChapter(site: String): Chapter?

    @Query("SELECT * FROM chapters WHERE manga IS :arg0 ORDER BY id ASC")
    fun loadChaptersAllAsc(manga: String): List<Chapter>

    @Query("SELECT * FROM chapters WHERE manga IS :arg0 AND isRead IS 0 ORDER BY id ASC")
    fun loadChaptersNotReadAsc(manga: String): List<Chapter>

}

fun ChapterDao.count(manga: String) =
        loadChapters(manga).size

fun ChapterDao.countNotRead(manga: String) =
        loadChapters(manga).filter { !it.isRead }.size

fun ChapterDao.removeChapters(manga: String) =
        delete(*loadChapters(manga).toTypedArray())

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
