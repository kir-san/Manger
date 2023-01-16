package com.san.kir.features.shikimori.logic.useCases

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.features.shikimori.logic.repo.SettingsRepository
import com.san.kir.features.shikimori.logic.repo.TokenRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import timber.log.Timber
import javax.inject.Inject

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
        .distinctUntilChanged()

    // Получение токена и данных пользователя
    suspend fun login(code: String) = withIoContext {
        val newToken = tokenRepository.getAccessToken(code)
        val whoami = whoami()

        if (whoami == null) settingsRepository.update(isLogin = false)
        else settingsRepository.update(newToken)
    }

    suspend fun whoami() = withIoContext {
        val whoami = tokenRepository.getWhoami() ?: return@withIoContext null
        Timber.i("whoami -> $whoami")
        settingsRepository.update(whoami)
        whoami
    }

    suspend fun logout() = withIoContext {
        settingsRepository.clearAuth()
    }
}

internal data class AuthData(
    val nickName: String = "",
    val isLogin: Boolean = false,
)

