package com.san.kir.data.models.main

import com.san.kir.core.utils.ManualDI
import timber.log.Timber

public data class AccountManga(
    val id: Long = 0,
    val accountId: Long = 0,
    val targetId: Long = -1,
    val libraryId: Long = -1,
    val mangaId: Long = -1,
    val data: String = "",
) {
    public companion object {
        public inline fun <reified T : Any> from(
            accountId: Long,
            targetId: Long,
            mangaId: Long,
            data: T,
            libraryId: Long = -1
        ): AccountManga {
            return AccountManga(
                accountId = accountId,
                targetId = targetId,
                libraryId = libraryId,
                mangaId = mangaId,
                data = ManualDI.jsonToString(data)
            )
        }
    }
}

public inline fun <reified T> AccountManga.data(): T? {
    return runCatching { ManualDI.stringToJson<T>(data) }.onFailure { Timber.tag(ManualDI.TAG).e(it) }.getOrNull()
}
