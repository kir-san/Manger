package com.san.kir.features.accounts.shikimori.logic.api

import android.net.Uri
import androidx.core.net.toUri
import io.ktor.http.Parameters
import io.ktor.http.plus

internal object ShikimoriData {
    const val SITE_NAME = "shikimori.one"
    const val BASE_URL = "https://$SITE_NAME"
    const val TOKEN_URL = "${BASE_URL}/oauth/token"
    const val ICON_URL = "$BASE_URL/favicons/apple-touch-icon-180x180.png"

    private const val CLIENT_ID = "HVh4u3DNW6qCmFHGCdKMt65SeFUuElar9tdxwtAzos4"
    private const val CLIENT_SECRET = "_htbF82BTgPwh775gTVvM4DW2Z3eHVjcqP8TMt_IkaA"
    private const val REDIRECT_URI = "manger://shikimori-auth"

    private val baseParameters = Parameters.build {
        append("client_id", CLIENT_ID)
        append("client_secret", CLIENT_SECRET)
    }

    fun tokenParameters(code: String) =
        baseParameters + Parameters.build {
            append("grant_type", "authorization_code")
            append("redirect_uri", REDIRECT_URI)
            append("code", code)
        }

    fun refreshTokenParameters(refreshToken: String) =
        baseParameters + Parameters.build {
            append("grant_type", "refresh_token")
            append("refresh_token", refreshToken)
        }


    val authorizeUrl: Uri =
        "$BASE_URL/oauth/authorize".toUri()
            .buildUpon()
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("scope", "user_rates")
            .build()

}
