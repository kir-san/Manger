package com.san.kir.features.shikimori.useCases

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.features.shikimori.repositories.SettingsRepository
import com.san.kir.features.shikimori.repositories.TokenRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
internal class AuthUseCase @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val settingsRepository: SettingsRepository,
) {

    val authData = settingsRepository
        .auth()
        .mapLatest { auth ->
            AuthData(
                nickName = auth.whoami.nickname,
                isLogin = auth.isLogin
            )
        }
        .distinctUntilChanged { old, new -> old.isLogin == new.isLogin }

    // Получение токена и данных пользователя
    suspend fun login(code: String) = withIoContext {
        val newToken = tokenRepository.getAccessToken(code)
        settingsRepository.update(token = newToken)

        tokenRepository.getWhoami()?.let {new ->
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

