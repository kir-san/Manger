package com.san.kir.data.models.base

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = ShikiDbManga.tableName)
data class ShikiDbManga(

    // ID манги, которые указан на сайте
    @PrimaryKey
    @ColumnInfo(name = Col.targetId)
    val target_id: Long = -1,

    // ID манги из локальной библиотеки, с которой связана эта манга
    @ColumnInfo(name = Col.libMangaId)
    val libMangaId: Long = -1,

    @ColumnInfo(name = Col.rate)
    val rate: ShikimoriRate = ShikimoriRate(),

    @ColumnInfo(name = Col.data)
    val manga: ShikimoriManga = ShikimoriManga(),

    ) : ShikimoriMangaItem {

    override val id: Long
        get() = target_id

    override val name: String
        get() = manga.preparedName

    override val logo: String
        get() = manga.logo

    override val read: Long
        get() = rate.chapters

    override val all: Long
        get() = manga.chapters

    override val description: String
        get() = manga.description ?: ""

    override val status: ShikimoriStatus
        get() = rate.status

    companion object {
        const val tableName = "shikimori"
    }

    object Col {
        const val targetId = "id"
        const val libMangaId = "lid_id"
        const val rate = "rate"
        const val data = "data"
    }
}
