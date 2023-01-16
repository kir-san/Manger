package com.san.kir.features.shikimori.logic.repo

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.db.dao.SettingsDao
import com.san.kir.data.db.dao.ShikimoriDao
import com.san.kir.data.db.repositories.AbstractSettingsRepository
import com.san.kir.data.models.base.Settings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
internal class SettingsRepository @Inject constructor(
    settingsDao: SettingsDao,
    private val shikimoriDao: ShikimoriDao,
) : AbstractSettingsRepository(settingsDao) {

    fun auth() = settings().mapLatest { it.auth }

    suspend fun currentAuth() = currentSettings().auth
    suspend fun currentToken() = currentAuth().token

    suspend fun update(token: Settings.ShikimoriAuth.ShikimoriAccessToken) = withIoContext {
        settingsDao.update(
            currentSettings().copy(
                auth = currentAuth().copy(token = token)
            )
        )
    }

    suspend fun update(whoami: Settings.ShikimoriAuth.ShikimoriWhoami) = withIoContext {
        settingsDao.update(
            currentSettings().copy(
                auth = currentAuth().copy(isLogin = true, whoami = whoami)
            )
        )
    }

    suspend fun update(isLogin: Boolean) = withIoContext {
        settingsDao.update(
            currentSettings().copy(
                auth = currentAuth().copy(isLogin = isLogin)
            )
        )
    }

    suspend fun clearAuth() = withIoContext {
        settingsDao.update(
            currentSettings().copy(auth = Settings.ShikimoriAuth())
        )
        shikimoriDao.clearAll()
    }

}
