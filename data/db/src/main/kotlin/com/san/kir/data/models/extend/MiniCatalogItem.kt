package com.san.kir.data.models.extend

import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Ignore

@Stable
data class MiniCatalogItem(
    @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "catalogName") val catalogName: String = "",
    @ColumnInfo(name = "name") val name: String = "",
    @ColumnInfo(name = "statusEdition") val statusEdition: String = "",
    @ColumnInfo(name = "shotLink") val shotLink: String = "",
    @ColumnInfo(name = "link") val link: String = "",
    @ColumnInfo(name = "genres") val genres: List<String> = emptyList(),
    @ColumnInfo(name = "type") val type: String = "",
    @ColumnInfo(name = "authors") val authors: List<String> = emptyList(),
    @ColumnInfo(name = "dateId") val dateId: Int = 0,
    @ColumnInfo(name = "populate") val populate: Int = 0,
) {
    @Ignore
    var state: State = State.None

    sealed interface State {
        object Added : State
        object Update : State
        object None : State
    }
}
