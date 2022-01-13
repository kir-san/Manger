package com.san.kir.features.shikimori

import com.san.kir.data.models.base.ShikimoriAccount
import com.san.kir.data.models.datastore.ShikimoriAuth
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ShikimoriApi {
    @FormUrlEncoded
    @POST(ShikimoriData.tokenUrl)
    fun getAccessToken(
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("client_id") clientId: String = ShikimoriData.clientId,
        @Field("client_secret") clientSecret: String = ShikimoriData.clientSecret,
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String = ShikimoriData.redirectUri,
    ): Call<ShikimoriAuth.Token>

    @FormUrlEncoded
    @POST(ShikimoriData.tokenUrl)
    fun refreshAccessToken(
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("client_id") clientId: String = ShikimoriData.clientId,
        @Field("client_secret") clientSecret: String = ShikimoriData.clientSecret,
        @Field("refresh_token") refresh_token: String,
    ): Call<ShikimoriAuth.Token>

    // Распознание текущего пользователя
    @GET("api/users/whoami")
    fun whoami(
        @Header("Authorization") token: String,
    ): Call<ShikimoriAuth.Whoami>

    // Получение всей манги, что добавлена в профиль пользователя
    @GET("api/v2/user_rates")
    fun userRates(
        @Header("Authorization") token: String,
        @Query("user_id") userID: Long,
        @Query("target_type") type: String = "Manga",
    ): Call<List<ShikimoriAccount.Rate>>

    // Получение данных о манге
    @GET("api/mangas/{id}")
    fun manga(
        @Header("Authorization") token: String,
        @Path("id") target_id: Long,
    ): Call<ShikimoriAccount.Manga>

    // Обновление информации о чтении
    @PATCH("api/v2/user_rates/{id}")
    fun updateUserRate(
        @Header("Authorization") token: String,
        @Path("id") rate_id: Long,
        @Body rate: ShikimoriAccount.Rate,
    ): Call<ShikimoriAccount.Rate>

    // Получение информации о чтении
    @GET("api/v2/user_rates/{id}")
    fun userRate(
        @Header("Authorization") token: String,
        @Path("id") rate_id: Long,
    ): Call<ShikimoriAccount.Rate>
}

