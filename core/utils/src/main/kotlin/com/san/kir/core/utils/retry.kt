package com.san.kir.core.utils

import kotlinx.coroutines.delay


suspend fun <T> retryWithDelay(
    retries: Int,
    delay: Long,
    default: T,
    action: suspend () -> T,
): T {
    var retryCount = retries

    // Дается три попытки получить валидный ответ
    while (retryCount != 0) {
        retryCount--

        runCatching {
            return action()
        }.fold(
            onSuccess = {
                retryCount = 0
            },
            onFailure = {
                // при получении ошибки ждем некоторое время, так как единственная ошибка
                // которая появлялась связанна с частыми обращениями к сайту
                delay(delay)
            }
        )

    }

    return default
}
