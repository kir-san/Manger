package com.san.kir.features.shikimori.repositories

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.db.dao.ShikimoriDao
import com.san.kir.data.models.base.Settings
import com.san.kir.data.models.base.ShikiDbManga
import com.san.kir.data.models.base.ShikimoriImage
import com.san.kir.data.models.base.ShikimoriManga
import com.san.kir.data.models.base.ShikimoriRate
import com.san.kir.features.shikimori.api.ShikimoriApi
import com.san.kir.features.shikimori.api.ShikimoriData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

internal class ProfileItemRepository @Inject constructor(
    private val shikimoriDao: ShikimoriDao,
    private val client: HttpClient,
) : ItemsRepository {

    override fun loadItems() = shikimoriDao.loadItems()

    override fun loadItemById(id: Long) = shikimoriDao.loadItemByTargetId(id)

    override suspend fun items() = loadItems().first()

    override suspend fun itemById(libId: Long): ShikiDbManga? = shikimoriDao.itemByLibId(libId)

    suspend fun addOrUpdate(rate: ShikimoriRate): ShikiDbManga {
        var dbItem = shikimoriDao.itemByTargetId(rate.targetId)

        if (dbItem != null) {
            // Существующие обновляем
            dbItem = dbItem.copy(rate = rate)
            shikimoriDao.update(dbItem)
        } else {
            // Отсутствующие элементы сразу добавляются
            dbItem = ShikiDbManga(targetId = rate.targetId, rate = rate)
            shikimoriDao.insert(dbItem)
        }

        // Информация о манге обновляется только если ее нет
        if (dbItem.manga.isEmpty) {
            manga(rate)
                .onSuccess { newManga ->
                    shikimoriDao.update(
                        dbItem.copy(manga = newManga)
                    )
                }
        }

        return dbItem
    }

    suspend fun removeByRate(rate: ShikimoriRate) =
        withIoContext { shikimoriDao.removeByTargetId(rate.targetId) }

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
                .apply { Timber.v("result ${bodyAsText()}") }
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
                Timber.i("new rate is $rate")
                addOrUpdate(rate)
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

    suspend fun search(target: String): List<ShikimoriManga> = withIoContext {
        var mangas = emptyList<ShikimoriManga>()

        kotlin.runCatching {
            mangas = client.get(ShikimoriApi.Mangas(search = target)).body()
        }.onSuccess {
            // Преобразование url лого в корректное состояние
            mangas = mangas.map { item ->
                item.copy(image = ShikimoriImage(ShikimoriData.baseUrl + item.image.original))
            }
        }

        mangas
    }
}
