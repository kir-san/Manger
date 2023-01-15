package com.san.kir.features.shikimori.logic.repo

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.models.base.Settings
import com.san.kir.features.shikimori.logic.api.ShikimoriApi
import com.san.kir.features.shikimori.logic.api.ShikimoriData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get
import io.ktor.client.request.forms.submitForm
import io.ktor.http.HttpStatusCode
import timber.log.Timber
import javax.inject.Inject

internal class TokenRepository @Inject constructor(
    private val client: HttpClient,
    private val settingsRepository: SettingsRepository,
) {

    suspend fun getAccessToken(code: String): Settings.ShikimoriAuth.ShikimoriAccessToken =
        withIoContext {
            client.submitForm(
                url = ShikimoriData.tokenUrl,
                formParameters = ShikimoriData.getTokenParameters(code)
            ).body<Settings.ShikimoriAuth.ShikimoriAccessToken>().apply {
                // Сохранение нового токена
                settingsRepository.update(token = this)
            }
        }

    suspend fun getWhoami(): Settings.ShikimoriAuth.ShikimoriWhoami? = withIoContext {
        runCatching {
            val response = client.get(ShikimoriApi.Users.Whoami())
            if (response.status == HttpStatusCode.Unauthorized)
                null
            else
                response.body<Settings.ShikimoriAuth.ShikimoriWhoami>()

        }.onFailure(Timber::e).getOrNull()
    }
}

