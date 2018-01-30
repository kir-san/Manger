package com.san.kir.manger.room.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

@Entity(tableName = "manga")
class Manga : Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var unic = ""
    var host = ""
    var name = ""
    var authors = ""
    var logo = ""
    var about = ""
    var categories = ""
    var genres = ""
    var path = ""
    var status = ""
    var site = ""
    var color = 0
    var populate = 0
    var order = 0

    constructor()
    constructor(id: Long = 0,
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
                genresList: List<String> = listOf()) : this() {
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

        if (!authorsList.isEmpty())
            this.authorsList = authorsList

        if (!categoriesList.isEmpty())
            this.categoriesList = categoriesList

        if (!genresList.isEmpty())
            this.genresList = genresList
    }

    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt()
    )

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
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Manga> {
        override fun createFromParcel(parcel: Parcel): Manga {
            return Manga(parcel)
        }

        override fun newArray(size: Int): Array<Manga?> {
            return arrayOfNulls(size)
        }
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


/*

async {
            channelTask.consumeEach { downloadItem ->
                log("start work with size of task ${taskCounter.size}")
                try {
                                        val pages = ManageSites.getPages(downloadItem)
                    downloadItem.maxPages = pages.count()
                    downloads.update(downloadItem)

                    log("before drop = ${pages.count()}")
                    log("this progress = ${downloadItem.progressPages}")
                    val drop = pages.drop(downloadItem.progressPages)
                    log("after drop = ${drop.count()}")
                    drop.forEach {
                        // из ссылки получаю имя для файла
                        val pat = Pattern.compile("[a-z0-9._-]+\\.[a-z]{3,4}")
                                .matcher(it.removeSurrounding("\"", "\""))
                        var name = ""
                        while (pat.find())
                            name = pat.group()

                        ManageSites.openLink(it)
                                .downloadTo(File(getFullPath(downloadItem.path), name))
                        downloadItem.progressPages++
                        log("this progress = ${downloadItem.progressPages}")
                        downloads.update(downloadItem)
                    }

                    downloadItem.isCompleted = true
                    downloads.update(downloadItem)
                } catch (ex: Exception) {
                    downloadItem.isError = true
                    downloads.update(downloadItem)
                    ex.printStackTrace()
                } finally {
                    taskCounter -= downloadItem
                    log("end work with size of task ${taskCounter.size}")
                }
                if (!channelTask.iterator().hasNext()) {
                    log("hasNext task is false")
                    channelTask.close()
                    log("channel task is closed")
                }
            }
        }




        async {
            val item = intent.getParcelableExtra<DownloadItem>("item")
            log("add task is ${item.name}")
            channelTask.send(item)
            item.isError = false
            item.isStoped = false
            downloads.update(item)
            taskCounter += item
        }

*/
