package com.san.kir.features.shikimori

import android.net.Uri
import com.san.kir.data.models.base.ShikimoriAccount

object ShikimoriData {
    const val siteName = "shikimori.one"
    const val baseUrl = "https://$siteName"
    const val tokenUrl = "oauth/token"
    const val iconUrl = "$baseUrl/favicons/apple-touch-icon-180x180.png"

    const val clientId = "HVh4u3DNW6qCmFHGCdKMt65SeFUuElar9tdxwtAzos4"
    const val clientSecret = "_htbF82BTgPwh775gTVvM4DW2Z3eHVjcqP8TMt_IkaA"
    const val redirectUri = "manger://shikimori-auth"

    val authorizeUrl: Uri =
        Uri.parse("$baseUrl/oauth/authorize")
            .buildUpon()
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("response_type", "code")
            .build()

    fun authToken(token: String) = "Bearer $token"

}
