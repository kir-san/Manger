package com.san.kir.features.accounts.shikimori.logic.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class User(
    @SerialName("id") val id: Long = 0L,
    @SerialName("nickname") val nickname: String = "",
    @SerialName("avatarUrl") val avatar: String = "",
    @SerialName("error") val error: String? = "",
    @SerialName("error_description") val errorDescription: String? = "",
)
