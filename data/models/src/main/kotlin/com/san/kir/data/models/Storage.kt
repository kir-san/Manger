package com.san.kir.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.san.kir.core.utils.getFullPath
import com.san.kir.core.utils.lengthMb
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
@Entity(tableName = "StorageItem")
data class Storage(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var name: String = "",
    var path: String = "",
    var sizeFull: Double = 0.0,
    var sizeRead: Double = 0.0,
    var isNew: Boolean = false,
    var catalogName: String = "",
) : Parcelable

fun Storage.getSizeAndIsNew(file: File, manga: Manga?, chapters: List<Chapter>?): Storage {
    sizeFull = file.lengthMb
    isNew = manga == null
    sizeRead = chapters?.let { it ->
        it.asSequence()
            .filter { it.isRead }
            .sumOf { getFullPath(it.path).lengthMb }
    } ?: 0.0
    return this
}

