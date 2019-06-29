package com.san.kir.manger.utils

import android.os.Environment
import java.io.File

fun getFullPath(path: String): File = File(Environment.getExternalStorageDirectory(), path)

fun getCountPagesForChapterInMemory(shortPath: String): Int {
    val listFiles = getFullPath(shortPath).ifExists?.listFiles { _, s ->
        checkExtension(s)
    }
    return listFiles?.size ?: 0
}

val File.ifExists: File?
    get() = if (this.exists()) this else null

val imageExtensions = listOf("png", "jpg", "webp", "gif")

fun checkExtension(fileName: String): Boolean {
    return imageExtensions.any { fileName.toLowerCase().endsWith(it) }
}

val File.isEmptyDirectory: Boolean
    get() =
        if (exists() and isDirectory) {
            var isOk = false
            try {
                if (listFiles().isEmpty())
                    isOk = true
            } catch (ex: NullPointerException) {
            }
            isOk
        } else
            true
