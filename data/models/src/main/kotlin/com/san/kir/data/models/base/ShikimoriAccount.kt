package com.san.kir.data.models.base

import com.google.gson.annotations.SerializedName

object ShikimoriAccount {
    data class Rate(
        val id: Long = -1,
        val target_id: Long = -1,
        val status: Status = Status.Planned,
        val chapters: Long = 0,
    )

    enum class Status {
        @SerializedName("planned")
        Planned,

        @SerializedName("watching")
        Watching,

        @SerializedName("rewatching")
        Rewatching,

        @SerializedName("completed")
        Completed,

        @SerializedName("on_hold")
        OnHold,

        @SerializedName("dropped")
        Dropped
    }

    data class Manga(
        val id: Long = -1,
        val russian: String = "",
        val image: Image = Image(),
        val url: String = "",
        val chapters: Long = 0,
        val genres: List<Genre> = emptyList(),
        val description: String = "",
        val english: List<String> = emptyList(),
    )

    data class Image(
        val original: String = "",
    )

    data class Genre(
        val name: String = "",
        val russian: String = "",
    )

    interface AbstractMangaItem {
        val id: Long
        val name: String
        val logo: String
        val read: Long
        val all: Long
        val status: Status?
            get() = null
    }
}
