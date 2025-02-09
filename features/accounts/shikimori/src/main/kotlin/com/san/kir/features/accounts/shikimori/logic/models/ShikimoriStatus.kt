package com.san.kir.features.accounts.shikimori.logic.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class ShikimoriStatus {
    @SerialName("planned") Planned,
    @SerialName("watching") Watching,
    @SerialName("rewatching") Rewatching,
    @SerialName("completed") Completed,
    @SerialName("on_hold") OnHold,
    @SerialName("dropped") Dropped;
}
