package com.san.kir.features.shikimori.logic.di

import android.content.Context
import com.san.kir.data.models.base.Settings.ShikimoriAuth.ShikimoriAccessToken
import com.san.kir.features.shikimori.logic.api.ShikimoriData
import com.san.kir.features.shikimori.logic.repo.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.headers
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.serialization.gson.gson
import okhttp3.Cache
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class InternetModule {

    @Singleton
    @Provides
    fun provideCache(
        @ApplicationContext context: Context,
    ): Cache {
        val cacheSize = 5L * 1024 * 1024
        val cacheDir = File(context.cacheDir, "shiki")

        return Cache(cacheDir, cacheSize)
    }

    @Singleton
    @Provides
    fun provideKtorClient(
        cache: Cache,
        settingsRepository: SettingsRepository,
    ): HttpClient {
        val client = HttpClient(OkHttp) {
            engine {
                config {
                    retryOnConnectionFailure(true)
                    cache(cache)
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                    })
                }
            }

            defaultRequest {

                url {
                    protocol = URLProtocol.HTTPS
                    host = ShikimoriData.siteName
                }

                headers {
                    append(HttpHeaders.UserAgent, "manger")
                }
            }

            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                    setLenient()
                }
            }

            install(Resources)
        }

        var dbToken: ShikimoriAccessToken? = null
        suspend fun getToken(): ShikimoriAccessToken {
            if (dbToken == null)
                dbToken = settingsRepository.currentToken()
            return requireNotNull(dbToken)
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

        client.plugin(HttpSend).intercept { request ->
            var token = getToken()

            request.addHeader(token.accessToken)
            val originalCall = execute(request)

            if (originalCall.response.status != HttpStatusCode.Unauthorized)
                return@intercept originalCall

            val tokenCall = execute(HttpRequestBuilder().apply {
                url(ShikimoriData.tokenUrl)
                method = HttpMethod.Post
                setBody(FormDataContent(ShikimoriData.refreshTokenParameters(token.refreshToken)))
            })

            if (tokenCall.response.status != HttpStatusCode.Unauthorized) {
                val newToken: ShikimoriAccessToken = tokenCall.body()
                settingsRepository.update(newToken)
                dbToken = newToken
            }

            token = getToken()

            request.addHeader(token.accessToken)
            execute(request)
        }

        return client
    }
}
