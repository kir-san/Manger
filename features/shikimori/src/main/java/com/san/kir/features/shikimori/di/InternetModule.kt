package com.san.kir.features.shikimori.di

import android.content.Context
import com.san.kir.data.models.base.Settings
import com.san.kir.features.shikimori.ShikiAuth
import com.san.kir.features.shikimori.api.ShikimoriData
import com.san.kir.features.shikimori.bearer
import com.san.kir.features.shikimori.repositories.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
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
                        level = HttpLoggingInterceptor.Level.BODY
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
                        Timber.v("loadTokens")

                        if (token == null) {
                            val t = settingsRepository.currentToken()
                            if (t.accessToken.isNotEmpty() && t.refreshToken.isNotEmpty())
                                token = BearerTokens(t.accessToken, t.refreshToken)
                        }

                        token
                    }

                    refreshTokens {
                        Timber.v("refresh token")
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
