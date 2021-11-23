package com.san.kir.manger.room.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.san.kir.manger.utils.enums.ChapterFilter

@Entity(tableName = MangaColumn.tableName)
class Manga() : Parcelable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = MangaColumn.id)
    var id: Long = 0

    @Deprecated("дублирующий параметр, только сбивает с толку", replaceWith = ReplaceWith("manga.name"))
    @ColumnInfo(name = MangaColumn.unic)
    var unic = ""
    var host = ""

    @ColumnInfo(name = MangaColumn.name)
    var name = ""
    var authors = ""

    @ColumnInfo(name = MangaColumn.logo)
    var logo = ""
    var about = ""

    @ColumnInfo(name = MangaColumn.categories)
    var categories = ""
    var genres = ""
    var path = ""
    var status = ""

    @Deprecated("больше не использовать")
    var site = ""
    @ColumnInfo(name = MangaColumn.color)
    var color = 0

    @ColumnInfo(name = MangaColumn.populate)
    var populate = 0

    @ColumnInfo(name = MangaColumn.order)
    var order = 0
    var isAlternativeSort = true
    var isUpdate = true
    var chapterFilter = ChapterFilter.ALL_READ_ASC
    var isAlternativeSite = false
    var shortLink = ""

    @Ignore
    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        unic = parcel.readString() ?: ""
        host = parcel.readString() ?: ""
        name = parcel.readString() ?: ""
        authors = parcel.readString() ?: ""
        logo = parcel.readString() ?: ""
        about = parcel.readString() ?: ""
        categories = parcel.readString() ?: ""
        genres = parcel.readString() ?: ""
        path = parcel.readString() ?: ""
        status = parcel.readString() ?: ""
        site = parcel.readString() ?: ""
        color = parcel.readInt()
        populate = parcel.readInt()
        order = parcel.readInt()
        isAlternativeSort = parcel.readByte() != 0.toByte()
        isUpdate = parcel.readByte() != 0.toByte()
        isAlternativeSite = parcel.readByte() != 0.toByte()
        shortLink = parcel.readString() ?: ""
    }

    constructor(
        id: Long = 0,
        unic: String = "",
        host: String = "",
        name: String = "",
        authors: String = "",
        logo: String = "",
        about: String = "",
        categories: String = "",
        genres: String = "",
        path: String = "",
        status: String = "",
        site: String = "",
        color: Int = 0,
        populate: Int = 0,
        order: Int = 0,
        authorsList: List<String> = listOf(),
        categoriesList: List<String> = listOf(),
        genresList: List<String> = listOf(),
        isUpdate: Boolean = true,
        chapterFilter: ChapterFilter = ChapterFilter.ALL_READ_ASC,
        isAlternativeSite: Boolean = false,
        shortLink: String = ""
    ) : this() {
        this.id = id
        this.unic = unic
        this.host = host
        this.name = name
        this.authors = authors
        this.logo = logo
        this.about = about
        this.categories = categories
        this.genres = genres
        this.path = path
        this.status = status
        this.site = site
        this.color = color
        this.populate = populate
        this.order = order
        this.isUpdate = isUpdate

        if (authorsList.isNotEmpty())
            this.authorsList = authorsList

        if (categoriesList.isNotEmpty())
            this.categoriesList = categoriesList

        if (genresList.isNotEmpty())
            this.genresList = genresList

        this.chapterFilter = chapterFilter
        this.isAlternativeSite = isAlternativeSite
        this.shortLink = shortLink
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(unic)
        parcel.writeString(host)
        parcel.writeString(name)
        parcel.writeString(authors)
        parcel.writeString(logo)
        parcel.writeString(about)
        parcel.writeString(categories)
        parcel.writeString(genres)
        parcel.writeString(path)
        parcel.writeString(status)
        parcel.writeString(site)
        parcel.writeInt(color)
        parcel.writeInt(populate)
        parcel.writeInt(order)
        parcel.writeByte(if (isAlternativeSort) 1 else 0)
        parcel.writeByte(if (isUpdate) 1 else 0)
        parcel.writeByte(if (isAlternativeSite) 1 else 0)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Manga> {
        override fun createFromParcel(parcel: Parcel) = Manga(parcel)

        override fun newArray(size: Int): Array<Manga?> = arrayOfNulls(size)
    }

}

var Manga.authorsList: List<String>
    get() = authors.split(",").map { it.removePrefix(" ").removeSuffix(" ") }
    set(value) {
        authors = value.toString().removeSurrounding("[", "]")
    }

var Manga.categoriesList: List<String>
    get() = categories.split(",").map { it.removePrefix(" ").removeSuffix(" ") }
    set(value) {
        categories = value.toString().removeSurrounding("[", "]")
    }

var Manga.genresList: List<String>
    get() = genres.split(",").map { it.removePrefix(" ").removeSuffix(" ") }
    set(value) {
        genres = value.toString().removeSurrounding("[", "]")
    }

object MangaColumn {
    const val tableName = "manga"
    const val id = "id"
    const val unic = "unic"
    const val name = "name"
    const val categories = "categories"
    const val populate = "populate"
    const val order = "order"
    const val logo = "logo"
    const val color = "color"
}
