package com.san.kir.features.accounts.shikimori.logic.di

import com.san.kir.features.accounts.shikimori.logic.api.ShikimoriData
import com.san.kir.features.accounts.shikimori.logic.models.TokenContainer
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.headers
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.AttributeKey
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.logging.HttpLoggingInterceptor


internal class InternetClient(cache: Cache, json: Json) {


    private var onTokenUpdateAction: suspend (TokenContainer) -> Unit = {}
    private var tokenContainer: TokenContainer? = null
    private val client = HttpClient(OkHttp) {
        engine {
            config {
                retryOnConnectionFailure(true)
                cache(cache)
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
        }

        defaultRequest {

            url {
                protocol = URLProtocol.HTTPS
                host = ShikimoriData.SITE_NAME
            }

            headers {
                append(HttpHeaders.UserAgent, "manger")
                append(HttpHeaders.ContentType, "application/json")
                append(HttpHeaders.Accept, "application/json")
                append(HttpHeaders.Connection, "keep-alive")
                append(HttpHeaders.Origin, ShikimoriData.BASE_URL)
            }
        }

        install(ContentNegotiation) { json(json) }
        install(Resources)
    }

    init {
        initPlugins()
    }

    suspend fun submitForm(url: String, parameters: Parameters) =
        client.submitForm(url, parameters)

    suspend inline fun <reified T : Any> post(resource: T, data: Any?) = client.post(resource) {
        contentType(ContentType.Application.Json)
        setBody(data)
    }

    suspend inline fun <reified T : Any> get(resource: T): HttpResponse = client.get(resource)

    suspend inline fun <reified T : Any> delete(resource: T): HttpResponse = client.delete(resource)

    suspend inline fun <reified T : Any> put(resource: T, data: Any?) = client.put(resource) {
        contentType(ContentType.Application.Json)
        setBody(data)
    }

    fun updateTokens(newToken: TokenContainer) {
        this.tokenContainer = newToken
    }

    fun onTokenUpdate(action: suspend (TokenContainer) -> Unit) {
        this.onTokenUpdateAction = action
    }

    fun HttpRequestBuilder.addHeader(token: String) {
        headers {
            val tokenValue = "Bearer $token"
            if (contains(HttpHeaders.Authorization)) {
                remove(HttpHeaders.Authorization)
            }
            append(HttpHeaders.Authorization, tokenValue)
        }
    }

    private fun initPlugins() {
        client.plugin(HttpSend).intercept { request ->
            val currentTokens = tokenContainer
            currentTokens?.let { request.addHeader(it.accessToken) }

            val originalCall = execute(request)
            if (originalCall.response.status != HttpStatusCode.Unauthorized || currentTokens == null)
                return@intercept originalCall

            val tokenCall = execute(HttpRequestBuilder().apply {
                url(ShikimoriData.TOKEN_URL)
                method = HttpMethod.Post
                setBody(FormDataContent(ShikimoriData.refreshTokenParameters(currentTokens.refreshToken)))
            })

            if (tokenCall.response.status != HttpStatusCode.Unauthorized) {
                val newToken: TokenContainer = tokenCall.body()
                onTokenUpdateAction(newToken)
                tokenContainer = newToken
            }

            if (tokenCall.response.status == HttpStatusCode.BadRequest) {
                tokenCall.attributes.put(AttributeKey<Boolean>("ExpectSuccessAttributeKey"), false)
                return@intercept tokenCall
            }

            tokenContainer?.let { request.addHeader(it.accessToken) }
            execute(request)
        }
    }
}
