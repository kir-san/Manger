package com.san.kir.manger.utils.extensions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.san.kir.manger.App
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.utils.ResultDeleting
import java.io.File
import java.io.FileOutputStream
import java.util.*

fun getFullPath(path: String): File = File(App.externalDir, path)

fun getCountPagesForChapterInMemory(shortPath: String): Int {
    val listFiles = getFullPath(shortPath)
        .ifExists?.listFiles { _, s ->
        checkExtension(s)
    }
    return listFiles?.size ?: 0
}

val imageExtensions = listOf("png", "jpg", "webp", "gif")

fun checkExtension(fileName: String): Boolean {
    return imageExtensions.any { fileName.lowercase(Locale.ROOT).endsWith(it) }
}

fun bytesToMb(value: Long): Double = value.toDouble() / (1024.0 * 1024.0)

fun folderSize(directory: File): Long {
    var length = 0L
    directory.listFiles()?.forEach { file ->
        length += if (file.isFile) file.length()
        else folderSize(file)
    }
    return length
}

fun delChapters(chapters: List<Chapter>): ResultDeleting {
    return delFiles(chapters.map { it.path })
}

fun delChapters(vararg chapters: Chapter): ResultDeleting {
    return delFiles(chapters.map { it.path })
}

fun delFiles(filesPath: List<String>): ResultDeleting {
    var acc = 0
    filesPath.forEach { path ->
        getFullPath(path).apply { if (exists() && deleteRecursively()) acc++ }
    }
    return ResultDeleting(current = acc, max = filesPath.size)
}

private const val DEFAULT_COMPRESS_QUALITY = 90
fun convertImagesToPng(image: File): File {
    val b = BitmapFactory.decodeFile(image.path)

    val png = File(
        image.parentFile,
        "${image.nameWithoutExtension}.png"
    )

    log = if (png.createNewFile()) {
        "png created ${png.path}"
    } else {
        "png not created ${png.path}"
    }

    if (b != null) {
        val stream = FileOutputStream(png.absoluteFile)
        b.compress(Bitmap.CompressFormat.PNG, DEFAULT_COMPRESS_QUALITY, stream)
        stream.close()
    }

    log = if (image.delete()) {
        "oldFile deleted ${image.path}"
    } else {
        "oldFile not deleted ${image.path}"
    }

    return png
}
