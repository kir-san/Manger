package com.san.kir.features.shikimori.logic.repo

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.models.base.Settings
import com.san.kir.features.shikimori.logic.api.ShikimoriApi
import com.san.kir.features.shikimori.logic.api.ShikimoriData
import com.san.kir.features.shikimori.logic.plugins.BearerAuthProvider
import com.san.kir.features.shikimori.logic.plugins.ShikiAuth
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.plugin
import io.ktor.client.plugins.resources.get
import io.ktor.client.request.forms.submitForm
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
                // Очитстка текущего токена в плагине, для его новой инициализации
                client.plugin(ShikiAuth).providers
                    .filterIsInstance<BearerAuthProvider>()
                    .forEach {
                        it.clearToken()
                        Timber.i("old Token cleared")
                    }
            }
        }


    suspend fun getWhoami(): Settings.ShikimoriAuth.ShikimoriWhoami? = withIoContext {
        try {
            client.get(ShikimoriApi.Users.Whoami()).body<Settings.ShikimoriAuth.ShikimoriWhoami>()
        } catch (ex: Throwable) {
            Timber.e(ex)
            null
        }
    }
}

