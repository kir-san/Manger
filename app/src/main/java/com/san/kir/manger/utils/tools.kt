package com.san.kir.manger.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.util.LruCache
import android.view.View
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.LatestChapter
import java.io.File
import java.text.DecimalFormat

object ID {
    private var _count = 1

    fun generate(): Int {
        return _count++
    }
}

fun createDirs(path: File): Boolean {
    path.mkdirs()
    if (!path.isDirectory)
        log = "Error ${path.path}"
    return path.exists()
}

fun folderSize(directory: File): Long {
    var length = 0L
    directory.listFiles()?.forEach { file ->
        length += if (file.isFile) file.length()
        else folderSize(file)
    }
    return length
}

val File.lengthMb: Double
    get() = bytesToMbytes(folderSize(this))

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

val File.isNotEmptyDirectory: Boolean
    get() = !isEmptyDirectory


fun getShortPath(path: String) =
        if (path.isNotEmpty()) Regex("/${DIR.ROOT}.+").find(path)!!.value else path

fun getShortPath(path: File): String = getShortPath(path.path)

fun getFullPath(path: String): File = File(Environment.getExternalStorageDirectory(), path)

val extensions = listOf("png", "jpg", "webp")

fun getMangaLogo(shortPath: String): String = getMangaLogo(getFullPath(shortPath))
fun getMangaLogo(path: File): String {
    val templist = path.listFiles { file, s ->
        val fin = File(file, s)
        fin.isFile and (fin.extension in extensions)
    }
    return try {
        if (templist.isNotEmpty())
            templist[0].path
        else
            getMangaLogo(path.listFiles { file, s -> File(file, s).isDirectory }[0])
    } catch (ex: Exception) {
        ""
    }
}

fun getChapters(path: File,
                readyList: ArrayList<String> = arrayListOf()): ArrayList<String> {

    fun isContainImg(): Boolean {
        val list = path.listFiles()
        if (list != null) {
            if (list.isEmpty())
                return false
            var imgCount = 0
            var dirCount = 0
            for (file in list) {
                if (file.isFile and (file.extension in extensions))
                    imgCount++
                else if (file.isDirectory)
                    dirCount++
            }
            if ((imgCount > 0) and (dirCount == 0))
                return true
        }
        return false
    }

    if (isContainImg()) {
        readyList.add(path.path)
    } else {
        path.listFiles { file, s ->
            File(file, s).isDirectory
        }?.forEach { getChapters(it, readyList) }
    }
    return readyList
}

fun getCountPagesForChapterInMemory(shortPath: String): Int {
    val listFiles = getFullPath(shortPath).ifExists?.listFiles { _, s ->
        (s.toLowerCase().endsWith(".png") || s.toLowerCase().endsWith(".jpg") || s.toLowerCase().endsWith(
                ".webp"))
    }
    return listFiles?.size ?: 0
}

fun listStrToString(list: List<String>): String {
    val temp = StringBuilder()
    val lastIndex = list.size - 1
    for (i in 0..lastIndex) {
        if ((temp.isNotEmpty()) and (i < list.size))
            temp.append(", ")
        temp.append(list[i])
    }
    return temp.toString()
}

var log: String = ""
    set(msg) {
        Log.v(TAG, msg)
    }

fun Any.log(msg: String) {
    Log.v(this::class.java.simpleName, msg)
}


fun Context.getDrawableCompat(layoutRes: Int): Drawable {
    return ContextCompat.getDrawable(this, layoutRes)
}

fun View.getDrawableCompat(layoutRes: Int): Drawable {
    return context.getDrawableCompat(layoutRes)
}

val cache = object : LruCache<String, Bitmap>(3) {
//    override fun sizeOf(key: String?, value: Bitmap): Int {
//        return value.byteCount / 1024
//    }
}

data class ResultDeleting(val current: Int, val max: Int)

fun delChapters(vararg chapters: Chapter): ResultDeleting {
    return delChapters(chapters.toList())
}

fun delChapters(vararg chapters: LatestChapter): ResultDeleting {
    return delFiles(chapters.map { it.path })
}

fun delChapters(chapters: List<Chapter>): ResultDeleting {
    return delFiles(chapters.map { it.path })
}

fun delFiles(filesPath: List<String>): ResultDeleting {
    var acc = 0
    filesPath.forEach { path ->
        getFullPath(path).apply { if (exists() && deleteRecursively()) acc++ }
    }
    return ResultDeleting(current = acc, max = filesPath.size)
}

object SortLibraryUtil {
    val add = "add"
    val abc = "abc"

    fun toType(type: String): SortLibrary {
        return when (type) {
            add -> SortLibrary.AddTime
            abc -> SortLibrary.AbcSort
            else -> SortLibrary.AddTime
        }
    }

    fun toString(type: SortLibrary): String {
        return when (type) {
            SortLibrary.AddTime -> add
            SortLibrary.AbcSort -> abc
        }
    }
}

fun formatDouble(value: Double?) = DecimalFormat("#0.00").format(value)

fun bytesToMbytes(value: Long) = value.toDouble() / (1024.0 * 1024.0)
