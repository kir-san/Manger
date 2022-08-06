package com.san.kir.data.models.base

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.san.kir.core.support.ChapterFilter
import kotlinx.serialization.SerialName

@Entity(tableName = Settings.tableName)
data class Settings(
    @ColumnInfo(name = Col.id)
    @PrimaryKey(autoGenerate = false)
    val id: Long = 1,

    @Embedded
    val chapters: Chapters = Chapters(),

    @Embedded
    val download: Download = Download(),

    @ColumnInfo(name = Col.isFirstLaunch)
    val isFirstLaunch: Boolean = true,

    @Embedded
    val main: Main = Main(),

    @Embedded
    val viewer: Viewer = Viewer(),

    @Embedded
    val auth: ShikimoriAuth = ShikimoriAuth()
) {
    companion object {
        const val tableName = "settings"
    }

    object Col {
        const val id = "id"
        const val isIndividual = "isIndividual"
        const val isTitle = "isTitle"
        const val filterStatus = "filterStatus"
        const val concurrent = "concurrent"
        const val retry = "retry"
        const val wifi = "wifi"
        const val isFirstLaunch = "isFirstLaunch"
        const val theme = "theme"
        const val isShowCategory = "isShowCategory"
        const val editMenu = "editMenu"
        const val orientation = "orientation"
        const val cutOut = "cutOut"
        const val withoutSaveFiles = "withoutSaveFiles"
        const val isLogin = "isLogin"
        const val taps = "taps"
        const val swipes = "swipes"
        const val keys = "keys"
        const val accessToken = "access_token"
        const val tokenType = "token_type"
        const val expiresIn = "expires_in"
        const val refreshToken = "refresh_token"
        const val scope = "scope"
        const val createdAt = "created_at"
        const val shikimoriWhoamiId = "shikimori_whoami_id"
        const val nickname = "nickname"
        const val avatar = "avatar"
    }

    data class Chapters(
        @ColumnInfo(name = Col.isIndividual)
        val isIndividual: Boolean = true,

        @ColumnInfo(name = Col.isTitle)
        val isTitle: Boolean = true,

        @ColumnInfo(name = Col.filterStatus)
        val filterStatus: ChapterFilter = ChapterFilter.ALL_READ_ASC,
    )

    data class Download(
        @ColumnInfo(name = Col.concurrent)
        val concurrent: Boolean = true,

        @ColumnInfo(name = Col.retry)
        val retry: Boolean = false,

        @ColumnInfo(name = Col.wifi)
        val wifi: Boolean = false,
    )

    data class Main(
        @ColumnInfo(name = Col.theme)
        val theme: Boolean = true,

        @ColumnInfo(name = Col.isShowCategory)
        val isShowCategory: Boolean = true,

        @ColumnInfo(name = Col.editMenu)
        val editMenu: Boolean = false,
    )

    data class Viewer(
        @ColumnInfo(name = Col.orientation)
        val orientation: Orientation = Orientation.AUTO_LAND,

        @ColumnInfo(name = Col.cutOut)
        val cutOut: Boolean = true,

        @Embedded
        val control: Control = Control(),

        @ColumnInfo(name = Col.withoutSaveFiles)
        val withoutSaveFiles: Boolean = false,
    ) {
        enum class Orientation(val number: Int) {
            PORT(0), PORT_REV(1), LAND(2), LAND_REV(3), AUTO(4), AUTO_PORT(5), AUTO_LAND(6),
        }

        data class Control(
            @ColumnInfo(name = Col.taps)
            val taps: Boolean = false,

            @ColumnInfo(name = Col.swipes)
            val swipes: Boolean = true,

            @ColumnInfo(name = Col.keys)
            val keys: Boolean = false,
        )
    }

    data class ShikimoriAuth(
        @ColumnInfo(name = Col.isLogin)
        val isLogin: Boolean = false,

        @Embedded
        val token: ShikimoriAccessToken = ShikimoriAccessToken(),

        @Embedded
        val whoami: ShikimoriWhoami = ShikimoriWhoami(),
    ) {
        @kotlinx.serialization.Serializable
        data class ShikimoriAccessToken(
            @ColumnInfo(name = Col.accessToken)
            @SerialName(Col.accessToken)
            val accessToken: String = "",

            @ColumnInfo(name = Col.tokenType)
            @SerialName(Col.tokenType)
            val tokenType: String = "",

            @ColumnInfo(name = Col.expiresIn)
            @SerialName(Col.expiresIn)
            val expiresIn: Long = 0,

            @ColumnInfo(name = Col.refreshToken)
            @SerialName(Col.refreshToken)
            val refreshToken: String = "",

            @ColumnInfo(name = Col.scope)
            @SerialName(Col.scope)
            val scope: String = "",

            @ColumnInfo(name = Col.createdAt)
            @SerialName(Col.createdAt)
            val createdAt: Long = 0,
        ) {
            val isExpired: Boolean
                get() = (System.currentTimeMillis() / 1000) > (createdAt + expiresIn - 3600)
        }

        @kotlinx.serialization.Serializable
        data class ShikimoriWhoami(
            @ColumnInfo(name = Col.shikimoriWhoamiId)
            @SerialName(Col.id)
            val id: Long = 0,

            @ColumnInfo(name = Col.nickname)
            @SerialName(Col.nickname)
            val nickname: String = "",

            @ColumnInfo(name = Col.avatar)
            @SerialName(Col.avatar)
            val avatar: String = "",
        )
    }
}
