package com.san.kir.features.shikimori.logic.useCases

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.features.shikimori.logic.plugins.BearerAuthProvider
import com.san.kir.features.shikimori.logic.plugins.ShikiAuth
import com.san.kir.features.shikimori.logic.repo.SettingsRepository
import com.san.kir.features.shikimori.logic.repo.TokenRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.plugin
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
internal class AuthUseCase @Inject constructor(
    private val client: HttpClient,
    private val tokenRepository: TokenRepository,
    private val settingsRepository: SettingsRepository,
) {

    val authData = settingsRepository
        .settings()
        .mapLatest { settings ->
            AuthData(
                nickName = settings.auth.whoami.nickname,
                isLogin = settings.auth.isLogin
                        && settings.auth.token.accessToken.isNotEmpty()
                        && settings.auth.token.refreshToken.isNotEmpty()
            )
        }
        .distinctUntilChanged()

    // Получение токена и данных пользователя
    suspend fun login(code: String) = withIoContext {
        val newToken = tokenRepository.getAccessToken(code)

        tokenRepository.getWhoami()?.let { new ->
            settingsRepository.update(isLogin = true, token = newToken, whoami = new)
        } ?: let {
            settingsRepository.update(isLogin = false)
        }

    }

    suspend fun logout() = withIoContext {
        settingsRepository.clearAuth()

        client.plugin(ShikiAuth).providers
            .filterIsInstance<BearerAuthProvider>()
            .forEach(BearerAuthProvider::clearToken)
    }
}

internal data class AuthData(
    val nickName: String = "",
    val isLogin: Boolean = false,
)

