package com.san.kir.features.shikimori.logic.api

import android.net.Uri
import io.ktor.http.Parameters
import io.ktor.http.plus

object ShikimoriData {
    const val siteName = "shikimori.one"
    const val baseUrl = "https://$siteName"
    const val tokenUrl = "${baseUrl}/oauth/token"
    const val iconUrl = "$baseUrl/favicons/apple-touch-icon-180x180.png"

    private const val clientId = "HVh4u3DNW6qCmFHGCdKMt65SeFUuElar9tdxwtAzos4"
    private const val clientSecret = "_htbF82BTgPwh775gTVvM4DW2Z3eHVjcqP8TMt_IkaA"
    private const val redirectUri = "manger://shikimori-auth"

    private val baseParameters = Parameters.build {
        append("client_id", clientId)
        append("client_secret", clientSecret)
    }

    fun getTokenParameters(code: String) =
        baseParameters + Parameters.build {
            append("grant_type", "authorization_code")
            append("redirect_uri", redirectUri)
            append("code", code)
        }

    fun refreshTokenParameters(refreshToken: String) =
        baseParameters + Parameters.build {
            append("grant_type", "refresh_token")
            append("refresh_token", refreshToken)
        }


    val authorizeUrl: Uri =
        Uri.parse("$baseUrl/oauth/authorize")
            .buildUpon()
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("response_type", "code")
            .build()

}
