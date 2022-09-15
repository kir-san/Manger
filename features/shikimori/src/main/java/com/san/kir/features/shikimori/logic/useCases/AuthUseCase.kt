package com.san.kir.features.shikimori.logic.useCases

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.features.shikimori.logic.repo.SettingsRepository
import com.san.kir.features.shikimori.logic.repo.TokenRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
internal class AuthUseCase @Inject constructor(
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
        .distinctUntilChanged { old, new -> old.isLogin == new.isLogin }

    // Получение токена и данных пользователя
    suspend fun login(code: String) = withIoContext {
        val newToken = tokenRepository.getAccessToken(code)
        settingsRepository.update(token = newToken)

        delay(4.seconds)

        tokenRepository.getWhoami()?.let { new ->
            settingsRepository.update(isLogin = true, token = newToken, whoami = new)
        } ?: let {
            settingsRepository.update(isLogin = false)
        }

    }

    suspend fun logout() = withIoContext {
        settingsRepository.clearAuth()
    }
}

internal data class AuthData(
    val nickName: String = "",
    val isLogin: Boolean = false,
)

