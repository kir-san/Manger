package com.san.kir.data.store

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.san.kir.manger.ShikimoriAuth
import com.san.kir.manger.ShikimoriAuthKt.token
import com.san.kir.manger.ShikimoriAuthKt.whoami
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import com.san.kir.data.models.datastore.ShikimoriAuth as Model

class TokenStore @Inject constructor(context: Application) {
    private val tag: String = "ShikimoriAuthRepo"
    private val store = context.shikimoriAuthStore

    val data: Flow<Model> = store.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(tag, "Error reading sort order preferences.", exception)
                emit(ShikimoriAuth.getDefaultInstance())
            } else {
                throw exception
            }
        }
        .map { store ->
            Model(
                isLogin = store.isLogin,
                token = Model.Token(
                    access_token = store.token.accessToken,
                    token_type = store.token.tokenType,
                    expires_in = store.token.expiresIn,
                    refresh_token = store.token.refreshToken,
                    scope = store.token.scope,
                    created_at = store.token.createdAt
                ),
                whoami = Model.Whoami(
                    id = store.whoami.id,
                    nickname = store.whoami.nickname,
                    avatar = store.whoami.avatar,
                )
            )
        }

    suspend fun updateToken(state: Model.Token) {
        store.updateData { preference ->
            preference.toBuilder().apply {
                token = token {
                    accessToken = state.access_token
                    tokenType = state.token_type
                    expiresIn = state.expires_in
                    refreshToken = state.refresh_token
                    scope = state.scope
                    createdAt = state.created_at
                }
            }.build()
        }
    }

    suspend fun updateWhoami(state: Model.Whoami) {
        store.updateData { preference ->
            preference.toBuilder().apply {
                whoami = whoami {
                    id = state.id
                    nickname = state.nickname
                    avatar = state.avatar
                }
            }.build()
        }
    }


    suspend fun setLogin(state: Boolean) {
        store.updateData { preference ->
            preference.toBuilder().apply { isLogin = state }.build()
        }
        if (state.not()) {
            updateToken(Model.Token())
            updateWhoami(Model.Whoami())
        }
    }
}

val Context.shikimoriAuthStore: DataStore<ShikimoriAuth> by dataStore(
    fileName = "token.proto",
    serializer = ShikimoriAuthSerializer
)
