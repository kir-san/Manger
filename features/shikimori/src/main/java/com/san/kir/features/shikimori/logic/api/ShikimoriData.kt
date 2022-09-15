package com.san.kir.features.shikimori.logic.api

import android.net.Uri
import io.ktor.http.*

object ShikimoriData {
    const val siteName = "shikimori.one"
    const val baseUrl = "https://$siteName"
    const val tokenUrl = "oauth/token"
    const val iconUrl = "$baseUrl/favicons/apple-touch-icon-180x180.png"

    const val clientId = "HVh4u3DNW6qCmFHGCdKMt65SeFUuElar9tdxwtAzos4"
    const val clientSecret = "_htbF82BTgPwh775gTVvM4DW2Z3eHVjcqP8TMt_IkaA"
    const val redirectUri = "manger://shikimori-auth"

    private val baseParameters = parametersOf(
        "client_id" to listOf(clientId),
        "client_secret" to listOf(clientSecret)
    )

    fun getTokenParameters(code: String) =
        baseParameters + parametersOf(
            "grant_type" to listOf("authorization_code"),
            "redirect_uri" to listOf(redirectUri),
            "code" to listOf(code)
        )

    fun refreshTokenParameters(refreshToken: String) =
        baseParameters + parametersOf(
            "grant_type" to listOf("refresh_token"),
            "refresh_token" to listOf(refreshToken)
        )


    val authorizeUrl: Uri =
        Uri.parse("$baseUrl/oauth/authorize")
            .buildUpon()
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("response_type", "code")
            .build()

    fun authToken(token: String) = "Bearer $token"

}
