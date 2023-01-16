package com.san.kir.data.models.base

import android.os.Parcelable
import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.san.kir.core.utils.getFullPath
import com.san.kir.core.utils.lengthMb
import kotlinx.parcelize.Parcelize
import java.io.File

@Stable
@Parcelize
@Entity(tableName = "StorageItem")
data class Storage(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val name: String = "",
    val path: String = "",
    val sizeFull: Double = 0.0,
    val sizeRead: Double = 0.0,
    val isNew: Boolean = false,
    val catalogName: String = "",
) : Parcelable

fun Storage.getSizeAndIsNew(file: File, isNew: Boolean, chapters: List<Chapter>?): Storage {
    return copy(
        sizeFull = file.lengthMb,
        isNew = isNew,
        sizeRead = chapters?.filter { it.isRead }?.sumOf { getFullPath(it.path).lengthMb } ?: 0.0
    )
}

