package com.san.kir.data.models.utils

public fun String.preparePath(): String {
    var path = this
    if (contains("://")) {
        val parts = split("://")
        if (parts.size == 2) {
            val start = parts.first().substring(
                startIndex = 0,
                endIndex = parts.first().lastIndexOf("/")
            )
            val end = parts.last().substring(
                startIndex = parts.last().indexOf("/"),
                endIndex = parts.last().length - 1
            )

            path = start + end
        }
    }

    return path
        .replace("?", "")
        .replace(" ", "_")
        .replace("\"", "")
        .replace(":", "")
        .replace("*", "")
}
