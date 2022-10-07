package com.san.kir.features.shikimori.logic.plugins

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.auth.AuthProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.RefreshTokensParams
import io.ktor.client.plugins.plugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.AuthScheme
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.http.auth.parseAuthorizationHeader
import io.ktor.util.AttributeKey
import io.ktor.util.InternalAPI
import io.ktor.util.KtorDsl
import kotlinx.coroutines.CompletableDeferred
import timber.log.Timber
import java.util.concurrent.atomic.AtomicReference


@KtorDsl
class ShikiAuth private constructor(
    val providers: MutableList<AuthProvider> = mutableListOf()
) {
    companion object Plugin : HttpClientPlugin<ShikiAuth, ShikiAuth> {
        private val AuthCircuitBreaker: AttributeKey<Unit> = AttributeKey("auth-request")

        override val key: AttributeKey<ShikiAuth> = AttributeKey("DigestAuth")

        override fun prepare(block: ShikiAuth.() -> Unit): ShikiAuth {
            return ShikiAuth().apply(block)
        }

        @OptIn(InternalAPI::class)
        override fun install(plugin: ShikiAuth, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                plugin.providers.filter { it.sendWithoutRequest(context) }.forEach {
                    it.addRequestHeaders(context)
                }
            }

            scope.plugin(HttpSend).intercept { context ->
                val origin = execute(context)
                Timber.tag("ShikiAuth").i("status is ${origin.response.status}")
                if (origin.response.status != HttpStatusCode.Unauthorized) return@intercept origin
                Timber.tag("ShikiAuth").i("attributes is ${origin.request.attributes}")
                if (origin.request.attributes.contains(AuthCircuitBreaker)) return@intercept origin

                var call = origin

                val candidateProviders = HashSet(plugin.providers)
                Timber.tag("ShikiAuth").i("${candidateProviders}")

                while (call.response.status == HttpStatusCode.Unauthorized) {
                    val headerValue = call.response.headers[HttpHeaders.WWWAuthenticate]

                    val authHeader = headerValue?.let { parseAuthorizationHeader(headerValue) }
                    val provider = when {
                        authHeader == null && candidateProviders.size == 1 -> candidateProviders.first()
                        authHeader == null -> return@intercept call
                        else -> candidateProviders.find { it.isApplicable(authHeader) }
                            ?: return@intercept call
                    }
                    if (!provider.refreshToken(call.response)) return@intercept call

                    candidateProviders.remove(provider)

                    val request = HttpRequestBuilder()
                    request.takeFromWithExecutionContext(context)
                    provider.addRequestHeaders(request, authHeader)
                    request.attributes.put(AuthCircuitBreaker, Unit)

                    call = execute(request)
                }
                return@intercept call
            }
        }
    }
}

internal fun HttpClientConfig<*>.ShikiAuth(block: ShikiAuth.() -> Unit) {
    install(ShikiAuth, block)
}

fun ShikiAuth.bearer(block: BearerAuthConfig.() -> Unit) {
    with(BearerAuthConfig().apply(block)) {
        this@bearer.providers.add(
            BearerAuthProvider(
                _refreshTokens,
                _loadTokens,
                _sendWithoutRequest,
                realm
            )
        )
    }
}

class BearerAuthProvider(
    private val refreshTokens: suspend RefreshTokensParams.() -> BearerTokens?,
    loadTokens: suspend () -> BearerTokens?,
    private val sendWithoutRequestCallback: (HttpRequestBuilder) -> Boolean = { true },
    private val realm: String?
) : AuthProvider {

    @Suppress("OverridingDeprecatedMember")
    @Deprecated(
        "Please use sendWithoutRequest function instead",
        ReplaceWith("error(\"Deprecated\")")
    )
    override val sendWithoutRequest: Boolean
        get() = error("Deprecated")

    private val tokensHolder = AuthTokenHolder(loadTokens)

    override fun sendWithoutRequest(request: HttpRequestBuilder): Boolean =
        sendWithoutRequestCallback(request)

    /**
     * Checks if current provider is applicable to the request.
     */
    override fun isApplicable(auth: HttpAuthHeader): Boolean {
        if (auth.authScheme != AuthScheme.Bearer) return false
        if (realm == null) return true
        if (auth !is HttpAuthHeader.Parameterized) return false

        return auth.parameter("realm") == realm
    }

    /**
     * Adds an authentication method headers and credentials.
     */
    override suspend fun addRequestHeaders(
        request: HttpRequestBuilder,
        authHeader: HttpAuthHeader?
    ) {
        val token = tokensHolder.loadToken() ?: return

        request.headers {
            val tokenValue = "Bearer ${token.accessToken}"
            if (contains(HttpHeaders.Authorization)) {
                remove(HttpHeaders.Authorization)
            }
            append(HttpHeaders.Authorization, tokenValue)
        }
    }

    override suspend fun refreshToken(response: HttpResponse): Boolean {
        Timber.d("refreshToken")
        val newToken = tokensHolder.setToken {
            refreshTokens(
                RefreshTokensParams(
                    response.call.client,
                    response,
                    tokensHolder.loadToken()
                )
            )
        }
        return newToken != null
    }

    fun clearToken() {
        tokensHolder.clearToken()
    }
}

@KtorDsl
class BearerAuthConfig {
    internal var _refreshTokens: suspend RefreshTokensParams.() -> BearerTokens? = { null }
    internal var _loadTokens: suspend () -> BearerTokens? = { null }
    internal var _sendWithoutRequest: (HttpRequestBuilder) -> Boolean = { true }

    var realm: String? = null

    /**
     * Configures a callback that refreshes a token when the 401 status code is received.
     */
    fun refreshTokens(block: suspend RefreshTokensParams.() -> BearerTokens?) {
        _refreshTokens = block
    }

    /**
     * Configures a callback that loads a cached token from a local storage.
     * Note: Using the same client instance here to make a request will result in a deadlock.
     */
    fun loadTokens(block: suspend () -> BearerTokens?) {
        _loadTokens = block
    }

    /**
     * Sends credentials without waiting for [HttpStatusCode.Unauthorized].
     */
    fun sendWithoutRequest(block: (HttpRequestBuilder) -> Boolean) {
        _sendWithoutRequest = block
    }
}

internal class AuthTokenHolder<T>(
    private val loadTokens: suspend () -> T?
) {
    private val refreshTokensDeferred = AtomicReference<CompletableDeferred<T?>?>(null)
    private val loadTokensDeferred = AtomicReference<CompletableDeferred<T?>?>(null)

    internal fun clearToken() {
        loadTokensDeferred.set(null)
        refreshTokensDeferred.set(null)
    }

    internal suspend fun loadToken(): T? {
        var deferred: CompletableDeferred<T?>?
        while (true) {
            deferred = loadTokensDeferred.get()
            val newValue = deferred ?: CompletableDeferred()
            if (loadTokensDeferred.compareAndSet(deferred, newValue)) break
        }

        if (deferred != null) {
            return deferred.await()
        }

        val newTokens = loadTokens()
        loadTokensDeferred.get()!!.complete(newTokens)
        return newTokens
    }

    internal suspend fun setToken(block: suspend () -> T?): T? {
        var deferred: CompletableDeferred<T?>?
        while (true) {
            deferred = refreshTokensDeferred.get()
            val newValue = deferred ?: CompletableDeferred()
            if (refreshTokensDeferred.compareAndSet(deferred, newValue)) break
        }

        val newToken = if (deferred == null) {
            val newTokens = block()
            refreshTokensDeferred.get()!!.complete(newTokens)
            refreshTokensDeferred.set(null)
            newTokens
        } else {
            deferred.await()
        }
        loadTokensDeferred.set(CompletableDeferred(newToken))
        return newToken
    }
}

