package com.san.kir.manger.room.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.san.kir.manger.utils.enums.ChapterStatus
import com.san.kir.manger.utils.extensions.getCountPagesForChapterInMemory
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.utils.extensions.isEmptyDirectory


@Entity(tableName = "chapters")
data class Chapter(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var manga: String,
    var name: String,
    var date: String,
    var path: String,
    var isRead: Boolean,
    var site: String,
    var progress: Int,
    var pages: List<String>,
    var isInUpdate: Boolean
) : Parcelable {
    @Ignore
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.createStringArrayList() ?: arrayListOf(),
        parcel.readByte() != 0.toByte()
    )

    @Ignore
    constructor(
        manga: String = "",
        name: String = "",
        date: String = "",
        path: String = "",
        isRead: Boolean = false,
        site: String = "",
        progress: Int = 0,
        pages: List<String> = listOf(),
        isInUpdate: Boolean = false
    ) : this(0, manga, name, date, path, isRead, site, progress, pages, isInUpdate)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(manga)
        parcel.writeString(name)
        parcel.writeString(date)
        parcel.writeString(path)
        parcel.writeByte(if (isRead) 1 else 0)
        parcel.writeString(site)
        parcel.writeInt(progress)
        parcel.writeStringList(pages)
        parcel.writeByte(if (isRead) 1 else 0)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Chapter> {
        override fun createFromParcel(parcel: Parcel) = Chapter(parcel)
        override fun newArray(size: Int): Array<Chapter?> = arrayOfNulls(size)
    }
}

val Chapter.countPages: Int get() = getCountPagesForChapterInMemory(path)

val Chapter.action: Int
    get() {  // Определение доступного действия для главы
        getFullPath(path).apply {
            when {
                // если ссылка есть и если папка пуста или папки нет, то можно скачать
                site.isNotEmpty() && (isEmptyDirectory || !exists()) -> return ChapterStatus.DOWNLOADABLE
                // если папка непустая, то статус соответствует удалению
                !isEmptyDirectory -> return ChapterStatus.DELETE
                // папка не существет и ссылки на загрузку нет, то больше ничего не сделаешь
                !exists() and site.isEmpty() -> return ChapterStatus.NOT_LOADED
            }
        }
        return ChapterStatus.UNKNOWN // такого быть не должно, но если случится дайте знать
    }
