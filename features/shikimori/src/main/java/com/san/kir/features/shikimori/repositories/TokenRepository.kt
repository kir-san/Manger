package com.san.kir.features.shikimori.repositories

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.models.base.Settings
import com.san.kir.features.shikimori.ShikiAuth
import com.san.kir.features.shikimori.api.ShikimoriApi
import com.san.kir.features.shikimori.api.ShikimoriData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.forms.*
import javax.inject.Inject

internal class TokenRepository @Inject constructor(private val client: HttpClient) {

    suspend fun getAccessToken(code: String): Settings.ShikimoriAuth.ShikimoriAccessToken =
        withIoContext {
            client.submitForm(
                url = ShikimoriData.tokenUrl,
                formParameters = ShikimoriData.getTokenParameters(code)
            ).body<Settings.ShikimoriAuth.ShikimoriAccessToken>().apply {
                // Очитстка текущего токена в плагине, для его новой инициализации
                client.plugin(ShikiAuth).providers
                    .filterIsInstance<BearerAuthProvider>()
                    .firstOrNull()?.clearToken()
            }
        }


    suspend fun getWhoami(): Settings.ShikimoriAuth.ShikimoriWhoami? = withIoContext {
        try {
            client.get(ShikimoriApi.Users.Whoami()).body<Settings.ShikimoriAuth.ShikimoriWhoami>()
        } catch (ex: Throwable) {
            null
        }
    }
}

