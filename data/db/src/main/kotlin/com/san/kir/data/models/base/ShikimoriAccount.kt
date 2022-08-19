package com.san.kir.data.models.base

import com.google.gson.annotations.SerializedName


data class ShikimoriRate(
    @SerializedName("id") val id: Long = -1,
    @SerializedName("user_id") val userId: Long = -1,
    @SerializedName("target_id") val targetId: Long = -1,
    @SerializedName("status") val status: ShikimoriStatus = ShikimoriStatus.Planned,
    @SerializedName("chapters") val chapters: Long = 0,
    @SerializedName("target_type") val targetType: String = "Manga",
    @SerializedName("score") val score: Long = -1,
    @SerializedName("rewatches") val rewatches: Long = 0,
)

enum class ShikimoriStatus {
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

data class ShikimoriManga(
    val id: Long = -1,
    val name: String? = null,
    val russian: String = "",
    val image: ShikimoriImage = ShikimoriImage(),
    val url: String = "",
    val chapters: Long = 0,
    val genres: List<ShikimoriGenre> = emptyList(),
    val description: String? = null,
    val english: List<String?>? = emptyList(),
    val kind: String? = null,
    val score: Float? = null,
    val volumes: Long? = null,
) {
    val isEmpty: Boolean
        get() = id == -1L

    val preparedName: String
        get() = russian.ifEmpty { name ?: "" }

    val logo: String
        get() = image.original
}

data class ShikimoriImage(
    val original: String = "",
)

data class ShikimoriGenre(
    val name: String = "",
    val russian: String = "",
)

interface ShikimoriMangaItem {
    val id: Long
    val name: String
    val logo: String
    val read: Long
    val all: Long
    val description: String
    val status: ShikimoriStatus?
        get() = null
}

