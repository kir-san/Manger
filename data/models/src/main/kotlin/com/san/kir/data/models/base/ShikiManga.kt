package com.san.kir.data.models.base

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = ShikiManga.tableName)
data class ShikiManga(

    @PrimaryKey
    @ColumnInfo(name = Col.id)
    val target_id: Long = -1,

    @ColumnInfo(name = Col.libMangaId)
    val libMangaId: Long = -1,

    @ColumnInfo(name = Col.rate)
    val rate: ShikimoriAccount.Rate = ShikimoriAccount.Rate(),

    @ColumnInfo(name = Col.data)
    val manga: ShikimoriAccount.Manga = ShikimoriAccount.Manga(),

    ) : ShikimoriAccount.AbstractMangaItem {

    override val id: Long
        get() = target_id

    override val name: String
        get() = manga.russian

    override val logo: String
        get() = manga.image.original

    override val read: Long
        get() = rate.chapters

    override val all: Long
        get() = manga.chapters

    override val status: ShikimoriAccount.Status
        get() = rate.status

    companion object {
        const val tableName = "shikimori"
    }

    object Col {
        const val id = "id"
        const val libMangaId = "lid_id"
        const val rate = "rate"
        const val data = "data"
    }
}
