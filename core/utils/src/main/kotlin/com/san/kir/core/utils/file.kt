package com.san.kir.core.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.san.kir.core.support.DIR
import java.io.File
import java.io.FileOutputStream
import java.util.*

val externalDir: File = android.os.Environment.getExternalStorageDirectory()

fun getFullPath(path: String): File = File(externalDir, path)

val File.shortPath: String
    get() = if (path.isNotEmpty()) Regex("/${DIR.ROOT}.+").find(path)!!.value else path

val File.lengthMb: Double
    get() = bytesToMb(folderSize(this))

fun bytesToMb(value: Long): Double = value.toDouble() / (1024.0 * 1024.0)

fun folderSize(directory: File): Long {
    var length = 0L
    directory.listFiles()?.forEach { file ->
        length += if (file.isFile) file.length()
        else folderSize(file)
    }
    return length
}

fun getCountPagesForChapterInMemory(shortPath: String): Int {
    val listFiles = getFullPath(shortPath)
        .ifExists?.listFiles { _, s ->
            checkExtension(s)
        }
    return listFiles?.size ?: 0
}

val File.ifExists: File?
    get() = if (this.exists()) this else null

val imageExtensions = listOf("png", "jpg", "webp", "gif")

fun checkExtension(fileName: String): Boolean {
    return imageExtensions.any { fileName.lowercase(Locale.ROOT).endsWith(it) }
}

val File.isEmptyDirectory: Boolean
    get() =
        if (exists() and isDirectory) {
            var isOk = false
            try {
                listFiles()?.let {
                    if (it.isEmpty())
                        isOk = true
                }
            } catch (ex: NullPointerException) {
            }
            isOk
        } else
            true

fun delChapters(chapter: String): ResultDeleting {
    return delChapters(listOf(chapter))
}

fun delChapters(chapters: List<String>): ResultDeleting {
    return delFiles(chapters)
}

fun delFiles(filesPath: List<String>): ResultDeleting {
    var acc = 0
    filesPath.forEach { path ->
        getFullPath(path).apply { if (exists() && deleteRecursively()) acc++ }
    }
    return ResultDeleting(current = acc, max = filesPath.size)
}

fun File.createDirs(): Boolean {
    mkdirs()
    if (!isDirectory)
        log = "Error $path"
    return exists()
}

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

private const val DEFAULT_COMPRESS_QUALITY = 90
fun convertImagesToPng(image: File): File {
    val b = BitmapFactory.decodeFile(image.path)

    val png = File(
        image.parentFile,
        "${image.nameWithoutExtension}.png"
    )

    image.delete()

    png.createNewFile()

    if (b != null) {
        val stream = FileOutputStream(png.absoluteFile)
        b.compress(Bitmap.CompressFormat.PNG, DEFAULT_COMPRESS_QUALITY, stream)
        stream.close()
    }

    return png
}
