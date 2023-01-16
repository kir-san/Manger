package com.san.kir.features.shikimori.logic.repo

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.db.dao.ShikimoriDao
import com.san.kir.data.models.base.Settings
import com.san.kir.data.models.base.ShikiDbManga
import com.san.kir.data.models.base.ShikimoriImage
import com.san.kir.data.models.base.ShikimoriManga
import com.san.kir.data.models.base.ShikimoriRate
import com.san.kir.features.shikimori.logic.api.ShikimoriApi
import com.san.kir.features.shikimori.logic.api.ShikimoriData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

internal class ProfileItemRepository @Inject constructor(
    private val shikimoriDao: ShikimoriDao,
    private val client: HttpClient,
) : ItemsRepository {

    override fun loadItems() = shikimoriDao.loadItems()

    override fun loadItemById(id: Long) = shikimoriDao.loadItemByTargetId(id)

    override suspend fun items() = withIoContext { loadItems().first() }

    override suspend fun itemById(libId: Long): ShikiDbManga? =
        withIoContext { shikimoriDao.itemByLibId(libId) }

    private suspend fun addOrUpdate(rate: ShikimoriRate): ShikiDbManga {
        val dbItem = shikimoriDao.itemByTargetId(rate.targetId)

        var item =
            // Существующие обновляем
            dbItem?.copy(rate = rate) ?:
            // Отсутствующие элементы сразу добавляются
            ShikiDbManga(targetId = rate.targetId, rate = rate)

        // Информация о манге обновляется только если ее нет
        if (item.manga.isEmpty) {
            manga(rate).onSuccess { newManga ->
                item = item.copy(manga = newManga)
            }
        }

        if (dbItem == null)
            shikimoriDao.insert(item)
        else
            shikimoriDao.update(item)

        return item
    }

    private suspend fun removeByRate(rate: ShikimoriRate) =
        shikimoriDao.removeByTargetId(rate.targetId)

    suspend fun bindItem(rate: ShikimoriRate, libraryMangaId: Long) = withIoContext {
        Timber.i(
            "bindItem\n" +
                    "rate is ${rate.targetId}\n" +
                    "libraryMangaId is $libraryMangaId"
        )

        shikimoriDao.updateLibIdByTargetId(rate.targetId, libraryMangaId)
    }

    suspend fun unbindItem(rate: ShikimoriRate) =
        withIoContext { shikimoriDao.updateLibIdByTargetId(rate.targetId, -1L) }

    suspend fun rates(
        auth: Settings.ShikimoriAuth,
        targetId: Long? = null,
    ): Result<List<ShikimoriRate>> = withIoContext {
        kotlin.runCatching {
            client
                .get(
                    ShikimoriApi.V2.UserRates(
                        user_id = auth.whoami.id,
                        target_id = targetId,
                        target_type = "Manga"
                    )
                )
                .apply { Timber.v("result ${bodyAsText().count()}") }
                .body()
        }
    }

    // Получение данных из сети и сохранение в базе данных
    suspend fun updateRates(
        auth: Settings.ShikimoriAuth,
        targetId: Long? = null,
    ) = withIoContext {
        rates(auth, targetId).onSuccess { newRates ->
            newRates.forEach { rate ->
                addOrUpdate(rate)
                delay(150L)
            }
        }
    }

    suspend fun rate(target: ShikimoriRate): Result<ShikimoriRate> = withIoContext {
        kotlin.runCatching {
            client.get(ShikimoriApi.V2.UserRates.Id(id = target.id)).body()
        }
    }

    suspend fun add(target: ShikimoriRate): Result<ShikimoriRate> = withIoContext {
        Timber.v("add $target")
        kotlin.runCatching {
            val newRate: ShikimoriRate =
                client.post(ShikimoriApi.V2.UserRates()) {
                    contentType(ContentType.Application.Json)
                    setBody(target)
                }.body()

            addOrUpdate(newRate)

            newRate
        }
    }

    suspend fun remove(target: ShikimoriRate) = withIoContext {
        kotlin.runCatching {
            client.delete(ShikimoriApi.V2.UserRates.Id(id = target.id))
            removeByRate(target)
        }
    }

    suspend fun update(target: ShikimoriRate): Result<ShikimoriRate> = withIoContext {
        kotlin.runCatching {
            val newRate: ShikimoriRate =
                client.put(ShikimoriApi.V2.UserRates.Id(id = target.id)) {
                    contentType(ContentType.Application.Json)
                    setBody(target)
                }.body()

            addOrUpdate(target)

            newRate
        }
    }

    suspend fun manga(target: ShikimoriRate) = manga(target.targetId)

    // Получение манги с сайта
    suspend fun manga(targetId: Long): Result<ShikimoriManga> = withIoContext {
        kotlin.runCatching {
            val manga: ShikimoriManga = client.get(ShikimoriApi.Mangas.Id(id = targetId)).body()
            manga.copy(
                // Преобразование url лого в корректное состояние
                image = ShikimoriImage(ShikimoriData.baseUrl + manga.image.original)
            )
        }
    }

    suspend fun search(target: String): Result<List<ShikimoriManga>> = withIoContext {
        kotlin.runCatching {
            client
                .get(ShikimoriApi.Mangas(search = target))
                .body<List<ShikimoriManga>>()
                .map { item ->
                    // Преобразование url лого в корректное состояние
                    item.copy(
                        image = ShikimoriImage(ShikimoriData.baseUrl + item.image.original)
                    )
                }
        }
    }
}
