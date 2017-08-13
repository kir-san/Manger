package com.san.kir.manger.dbflow.models

import android.os.Parcel
import android.os.Parcelable
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.annotation.Unique
import com.raizlabs.android.dbflow.structure.BaseModel
import com.san.kir.manger.dbflow.AppDatabase

@Table(name = "manga", database = AppDatabase::class)
class Manga : BaseModel, Parcelable {

    @PrimaryKey(autoincrement = true) @Column var id: Long = 0
    @Unique @Column var unic: String = String()
    @Column var host: String = String()
    @Column var name: String = String()
    @Column var authors: String = String()

    var authorsList: List<String>
        get() = authors.split(",").map { it.removePrefix(" ").removeSuffix(" ") }
        set(value) {
            authors = value.toString().removeSurrounding("[", "]")
        }

    @Column var logo: String = String()
    @Column var about: String = String()
    @Column var categories: String = String()

    var categoriesList: List<String>
        get() = categories.split(",").map { it.removePrefix(" ").removeSuffix(" ") }
        set(value) {
            categories = value.toString().removeSurrounding("[", "]")
        }

    @Column var genres: String = String()

    var genresList: List<String>
        get() = genres.split(",").map { it.removePrefix(" ").removeSuffix(" ") }
        set(value) {
            genres = value.toString().removeSurrounding("[", "]")
        }

    @Column var path: String = String()
    @Column var status: String = String()
    @Column var site: String = String()
    @Column var color: Int = 0

    constructor()

    constructor(name: String,
                host: String = String(),
                author: String = "",
                authors: List<String> = listOf(),
                logo: String = "",
                about: String = "",
                category: String = "",
                categories: List<String> = listOf(),
                genre: String = "",
                genres: List<String> = listOf(),
                path: String = "",
                status: String = "",
                site: String = "",
                colorM: Int = 0) {
        this.name = name

        unic = name
        this.host = host
        if (authors.isEmpty())
            this.authors = author
        else
            this.authorsList = authors

        this.logo = logo
        this.about = about

        if (categories.isEmpty())
            this.categories = category
        else
            this.categoriesList = categories

        if (genres.isEmpty())
            this.genres = genre
        else
            this.genresList = genres

        this.path = path
        this.status = status
        this.site = site
        this.color = colorM
    }

    constructor(source: Parcel) : this(name = source.readString(),
                                       host = source.readString(),
                                       author = source.readString(),
                                       logo = source.readString(),
                                       about = source.readString(),
                                       category = source.readString(),
                                       genre = source.readString(),
                                       path = source.readString(),
                                       status = source.readString(),
                                       site = source.readString(),
                                       colorM = source.readInt())

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(name)
        dest?.writeString(host)
        dest?.writeString(authors)
        dest?.writeString(logo)
        dest?.writeString(about)
        dest?.writeString(categories)
        dest?.writeString(genres)
        dest?.writeString(path)
        dest?.writeString(status)
        dest?.writeString(site)
        dest?.writeInt(color)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Manga> = object : Parcelable.Creator<Manga> {
            override fun createFromParcel(source: Parcel): Manga {
                return Manga(source)
            }

            override fun newArray(size: Int): Array<Manga?> {
                return arrayOfNulls(size)
            }
        }
    }
}
