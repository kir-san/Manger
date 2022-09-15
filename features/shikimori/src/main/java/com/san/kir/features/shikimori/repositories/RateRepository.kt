package com.san.kir.features.shikimori.repositories

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.models.base.Settings
import com.san.kir.data.models.base.ShikimoriRate
import com.san.kir.features.shikimori.api.ShikimoriApi
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import timber.log.Timber
import javax.inject.Inject

internal class RateRepository @Inject constructor(
    private val profileRepository: ProfileItemRepository,
    private val client: HttpClient,
) {

    suspend fun add(target: ShikimoriRate): Result<ShikimoriRate> = withIoContext {
        kotlin.runCatching {
            val newRate: ShikimoriRate =
                client.post(ShikimoriApi.V2.UserRates()) { setBody(target) }.body()

            profileRepository.addOrUpdate(newRate)

            newRate
        }
    }

    suspend fun update(target: ShikimoriRate): Result<ShikimoriRate> = withIoContext {
        kotlin.runCatching {
            val newRate: ShikimoriRate =
                client.put(ShikimoriApi.V2.UserRates.Id(id = target.id)) { setBody(target) }.body()

            profileRepository.addOrUpdate(target)

            newRate
        }
    }
}
