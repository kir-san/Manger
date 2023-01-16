package com.san.kir.data.models.base

import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.san.kir.core.support.ChapterFilter

@Entity(tableName = "settings")
data class Settings(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = false)
    val id: Long = 1,

    @Embedded
    val chapters: Chapters = Chapters(),

    @Embedded
    val download: Download = Download(),

    @ColumnInfo(name = "isFirstLaunch")
    val isFirstLaunch: Boolean = true,

    @Embedded
    val main: Main = Main(),

    @Embedded
    val viewer: Viewer = Viewer(),

    @Embedded
    val auth: ShikimoriAuth = ShikimoriAuth(),
) {

    @Stable
    data class Chapters(
        @ColumnInfo(name = "isIndividual")
        val isIndividual: Boolean = true,

        @ColumnInfo(name = "isTitle")
        val isTitle: Boolean = true,

        @ColumnInfo(name = "filterStatus")
        val filterStatus: ChapterFilter = ChapterFilter.ALL_READ_ASC,
    )

    @Stable
    data class Download(
        @ColumnInfo(name = "concurrent")
        val concurrent: Boolean = true,

        @ColumnInfo(name = "retry")
        val retry: Boolean = false,

        @ColumnInfo(name = "wifi")
        val wifi: Boolean = false,
    )

    @Stable
    data class Main(
        @ColumnInfo(name = "theme")
        val theme: Boolean = true,

        @ColumnInfo(name = "isShowCategory")
        val isShowCategory: Boolean = true,

        @ColumnInfo(name = "editMenu")
        val editMenu: Boolean = false,
    )

    @Stable
    data class Viewer(
        @ColumnInfo(name = "orientation")
        val orientation: Orientation = Orientation.AUTO_LAND,

        @ColumnInfo(name = "cutOut")
        val cutOut: Boolean = true,

        @Embedded
        val control: Control = Control(),

        @ColumnInfo(name = "withoutSaveFiles")
        val withoutSaveFiles: Boolean = false,

        @ColumnInfo(name = "scrollbars")
        val useScrollbars: Boolean = true,
    ) {
        val controls: List<Boolean>
            get() = listOf(control.taps, control.swipes, control.keys)

        fun controls(items: List<Boolean>) = Control(items[0], items[1], items[2])

        enum class Orientation {
            PORT, PORT_REV, LAND, LAND_REV, AUTO, AUTO_PORT, AUTO_LAND,
        }

        data class Control(
            @ColumnInfo(name = "taps")
            val taps: Boolean = false,

            @ColumnInfo(name = "swipes")
            val swipes: Boolean = true,

            @ColumnInfo(name = "keys")
            val keys: Boolean = false,
        )
    }

    data class ShikimoriAuth(
        @ColumnInfo(name = "isLogin")
        val isLogin: Boolean = false,

        @Embedded
        val token: ShikimoriAccessToken = ShikimoriAccessToken(),

        @Embedded
        val whoami: ShikimoriWhoami = ShikimoriWhoami(),
    ) {
        data class ShikimoriAccessToken(
            @ColumnInfo(name = "access_token")
            @SerializedName("access_token")
            val accessToken: String = "",

            @ColumnInfo(name = "token_type")
            @SerializedName("token_type")
            val tokenType: String = "",

            @ColumnInfo(name = "expires_in")
            @SerializedName("expires_in")
            val expiresIn: Long = 0,

            @ColumnInfo(name = "refresh_token")
            @SerializedName("refresh_token")
            val refreshToken: String = "",

            @ColumnInfo(name = "scope")
            @SerializedName("scope")
            val scope: String = "",

            @ColumnInfo(name = "created_at")
            @SerializedName("created_at")
            val createdAt: Long = 0,
        ) {
            val isExpired: Boolean
                get() = (System.currentTimeMillis() / 1000) > (createdAt + expiresIn - 3600)
        }

        data class ShikimoriWhoami(
            @ColumnInfo(name = "shikimori_whoami_id")
            @SerializedName("id")
            val id: Long = 0,

            @ColumnInfo(name = "nickname")
            @SerializedName("nickname")
            val nickname: String = "",

            @ColumnInfo(name = "avatar")
            @SerializedName("avatar")
            val avatar: String = "",
        )
    }
}
