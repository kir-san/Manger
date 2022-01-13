package com.san.kir.features.shikimori

import com.san.kir.core.utils.log
import com.san.kir.data.models.base.ShikimoriAccount
import com.san.kir.data.models.datastore.ShikimoriAuth
import com.san.kir.data.store.TokenStore
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.await
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class Repository @Inject constructor(
    interceptor: ShikimoriInterceptor,
    private val store: TokenStore,
) {
    private val logging = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BASIC)
    }
    private val client = OkHttpClient().newBuilder()
        .addInterceptor(interceptor)
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(ShikimoriData.baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    private val service: ShikimoriApi = retrofit.create(ShikimoriApi::class.java)

    private var tempToken = ShikimoriAuth.Token()

    suspend fun accessToken(code: String) = service.getAccessToken(code = code).await()

    private suspend fun checkToken(token: ShikimoriAuth.Token): String {
        log("token is ${token.access_token}")
        if (tempToken.isExpired) {
            tempToken = token
            if (token.isExpired) {
                tempToken = service
                    .refreshAccessToken(refresh_token = token.refresh_token)
                    .await()
                log("token is updated")
            }
        }

        store.updateToken(tempToken)

        return ShikimoriData.authToken(tempToken.access_token)
    }

    suspend fun whoami(token: ShikimoriAuth.Token): ShikimoriAuth.Whoami {
        return service.whoami(checkToken(token)).await()
    }

    suspend fun userMangas(auth: ShikimoriAuth): List<ShikimoriAccount.Rate> {
        return service.userRates(
            checkToken(auth.token),
            userID = auth.whoami.id
        ).await()
    }

    suspend fun manga(auth: ShikimoriAuth, target: ShikimoriAccount.Rate): ShikimoriAccount.Manga {
        return service.manga(
            checkToken(auth.token),
            target_id = target.target_id
        ).await()
    }

    suspend fun update(auth: ShikimoriAuth, target: ShikimoriAccount.Rate): ShikimoriAccount.Rate {
        return service.updateUserRate(
            checkToken(auth.token),
            rate_id = target.id,
            rate = target,
        ).await()
    }

    suspend fun rate(auth: ShikimoriAuth, target: ShikimoriAccount.Rate): ShikimoriAccount.Rate {
        return service.userRate(
            checkToken(auth.token),
            rate_id = target.id,
        ).await()
    }
}

internal class ShikimoriInterceptor @Inject constructor() : Interceptor {
    // Добавление юзер агента в каждый запрос
    override fun intercept(chain: Interceptor.Chain): Response {

        return chain.proceed(
            chain.request()
                .newBuilder()
                .header("User-Agent", "manger")
                .build()
        )
    }
}

