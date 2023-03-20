package com.san.kir.data.models.utils

fun String.preparePath(): String {
    var path = this
    if ("://" in path) {
        val parts = path.split("://")
        if (parts.size == 2) {
            val start = parts.first().substring(0, parts.first().lastIndexOf("/"))
            val end =
                parts.last().substring(parts.last().indexOf("/"), parts.last().length - 1)
            path = start + end
        }
    }
    return path.replace("?", "")
        .replace(" ", "_")
}
