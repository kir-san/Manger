package com.san.kir.manger.room.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.san.kir.manger.room.dao.ChapterDao
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.dao.getFromPath
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.utils.extensions.lengthMb
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "StorageItem")
data class Storage(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var name: String = "",
    var path: String = "",
    var sizeFull: Double = 0.0,
    var sizeRead: Double = 0.0,
    var isNew: Boolean = false,
    var catalogName: String = ""
) : Parcelable

suspend fun Storage.getSizeAndIsNew(mangaDao: MangaDao, chapterDao: ChapterDao): Storage {
    val file = getFullPath(path)
    mangaDao.getFromPath(file).let { manga ->
        sizeFull = file.lengthMb
        isNew = manga == null
        sizeRead = manga?.let { it ->
            chapterDao.getItems(it.unic)
                .asSequence()
                .filter { it.isRead }
                .sumOf { getFullPath(it.path).lengthMb }
        } ?: 0.0
    }
    return this
}

