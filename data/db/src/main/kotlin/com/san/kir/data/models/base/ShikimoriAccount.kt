package com.san.kir.data.models.base

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShikimoriRate(
    @SerialName("id") val id: Long = -1,
    @SerialName("user_id") val userId: Long = -1,
    @SerialName("target_id") val targetId: Long = -1,
    @SerialName("status") val status: ShikimoriStatus = ShikimoriStatus.Planned,
    @SerialName("chapters") val chapters: Long = 0,
    @SerialName("target_type") val targetType: String = "Manga",
    @SerialName("score") val score: Long = -1,
    @SerialName("rewatches") val rewatches: Long = 0,
)

@Serializable
enum class ShikimoriStatus {
    @SerialName("planned")
    Planned,

    @SerialName("watching")
    Watching,

    @SerialName("rewatching")
    Rewatching,

    @SerialName("completed")
    Completed,

    @SerialName("on_hold")
    OnHold,

    @SerialName("dropped")
    Dropped
}

@Serializable
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

@Serializable
data class ShikimoriImage(
    val original: String = "",
)

@Serializable
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

