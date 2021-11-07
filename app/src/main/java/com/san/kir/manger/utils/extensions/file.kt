package com.san.kir.manger.utils.extensions

import com.san.kir.manger.utils.enums.DIR
import java.io.File

val File.ifExists: File?
    get() = if (this.exists()) this else null

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

val File.shortPath: String
    get() = if (path.isNotEmpty()) Regex("/${DIR.ROOT}.+").find(path)!!.value else path

fun File.createDirs(): Boolean {
    mkdirs()
    if (!isDirectory)
        log = "Error $path"
    return exists()
}

val File.lengthMb: Double
    get() = bytesToMb(folderSize(this))

// Проверка, что файл является корректным изображением формата PNG
fun File.isOkPng(): Boolean {
    kotlin.runCatching {
        val bytes = this.readBytes()
        if (bytes.size < 4) return false

        if (bytes[0] != 0x89.toByte() || bytes[1] != 0x50.toByte()) return false
        if (bytes[bytes.size - 2] != 0x60.toByte() || bytes[bytes.size - 1] != 0x82.toByte()) return false
    }.onFailure {
        return false
    }

    return true
}
