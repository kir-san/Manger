package com.san.kir.data.models.base

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shikimori")
data class ShikiDbManga(

    // ID манги, которые указан на сайте
    @PrimaryKey
    @ColumnInfo(name = "id")
    val targetId: Long = -1,

    // ID манги из локальной библиотеки, с которой связана эта манга
    @ColumnInfo(name = "lid_id")
    val libMangaId: Long = -1,

    @ColumnInfo(name = "rate")
    val rate: ShikimoriRate = ShikimoriRate(),

    @ColumnInfo(name = "data")
    val manga: ShikimoriManga = ShikimoriManga(),

    ) : ShikimoriMangaItem {

    override val id: Long
        get() = targetId

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

}
