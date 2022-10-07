package com.san.kir.features.shikimori.logic.di

import android.content.Context
import com.san.kir.data.models.base.Settings
import com.san.kir.features.shikimori.logic.api.ShikimoriData
import com.san.kir.features.shikimori.logic.plugins.ShikiAuth
import com.san.kir.features.shikimori.logic.plugins.bearer
import com.san.kir.features.shikimori.logic.repo.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.serialization.gson.gson
import okhttp3.Cache
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
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

            ShikiAuth {
                bearer {
                    var token: BearerTokens? = null
                    loadTokens {
                        val t = settingsRepository.currentToken()
                        Timber.v("local Tokens $t")
                        if (t.accessToken.isNotEmpty() && t.refreshToken.isNotEmpty())
                            token = BearerTokens(t.accessToken, t.refreshToken)

                        Timber.v("loadedToken $token")
                        token
                    }

                    refreshTokens {
                        Timber.v("refresh token $token")
                        val newToken: Settings.ShikimoriAuth.ShikimoriAccessToken =
                            client.submitForm(
                                url = ShikimoriData.tokenUrl,
                                formParameters = ShikimoriData.refreshTokenParameters(token!!.refreshToken)
                            ) { markAsRefreshTokenRequest() }.body()

                        settingsRepository.update(newToken)
                        token = BearerTokens(newToken.accessToken, newToken.refreshToken)
                        token
                    }
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

        return client
    }
}
