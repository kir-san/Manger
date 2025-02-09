package com.san.kir.features.accounts.shikimori.logic.repo

import com.san.kir.core.utils.ManualDI
import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.db.main.repo.AccountRepository
import com.san.kir.data.models.main.data
import com.san.kir.features.accounts.shikimori.logic.GraphQLQueries
import com.san.kir.features.accounts.shikimori.logic.api.ShikimoriApi
import com.san.kir.features.accounts.shikimori.logic.api.ShikimoriData
import com.san.kir.features.accounts.shikimori.logic.di.InternetClient
import com.san.kir.features.accounts.shikimori.logic.models.Auth
import com.san.kir.features.accounts.shikimori.logic.models.GraphQLResult
import com.san.kir.features.accounts.shikimori.logic.models.TokenContainer
import com.san.kir.features.accounts.shikimori.logic.models.User
import io.ktor.client.call.body
import timber.log.Timber

internal class AuthRepository(
    private val client: InternetClient,
    private val accountRepository: AccountRepository,
) {

    suspend fun login(accountId: Long?, code: String): Boolean = withIoContext {
        var auth = if (accountId == null) {
            Auth()
        } else {
            val account = accountRepository.item(accountId)
            account?.data<Auth>() ?: Auth()
        }

        if (auth.tokenContainer.isEmpty()) {
            auth = auth.copy(tokenContainer = accessToken(code))
        }

        val user = whoami(auth)
        if (user != null) {
            accountRepository.update(accountId ?: 0L, ManualDI.jsonToString(auth))
            user.hasUser
        } else {
            false
        }
    }

    suspend fun whoami(auth: Auth, accountId: Long? = null): Auth? {
        client.updateTokens(auth.tokenContainer)
        var currentAuth = auth
        client.onTokenUpdate {
            currentAuth = currentAuth.copy(tokenContainer = it)
        }

        val user = user() ?: return null
        Timber.Forest.i("whoami -> $user")
        currentAuth = currentAuth.copy(user = user)

        if (accountId != null) {
            accountRepository.update(accountId, ManualDI.jsonToString(currentAuth))
        }

        return currentAuth
    }

    suspend fun logout(accountId: Long?, full: Boolean) {
        if (accountId == null) return
        if (full) {
            accountRepository.delete(accountId)
        } else {
            accountRepository.update(accountId, "")
        }
    }

    suspend fun accessToken(code: String): TokenContainer {
        return client.submitForm(ShikimoriData.TOKEN_URL, ShikimoriData.tokenParameters(code))
            .body<TokenContainer>()
    }

    suspend fun user(): User? = withIoContext {
        runCatching {
            val response = client.post(ShikimoriApi.Graphql(), GraphQLQueries.queryCurrentUser())

            val user = runCatching { response.body<GraphQLResult>().data.whoami }
                .onFailure(Timber.Forest::e)
                .getOrNull()

            user ?: runCatching { response.body<User>() }.onFailure(Timber.Forest::e).getOrNull()
        }
            .onFailure(Timber.Forest::e)
            .getOrNull()
    }

}

@kotlinx.serialization.Serializable
internal data class AuthData(
    val nickName: String = "",
    val isLogin: Boolean = false
)

