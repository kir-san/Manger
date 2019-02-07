package com.san.kir.manger.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import com.san.kir.manger.R
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.LatestChapter
import com.san.kir.manger.utils.enums.DIR
import com.san.kir.manger.utils.enums.SortLibrary
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat

object ID {
    private var count = 1

    fun generate(): Int {
        return count++
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
    get() = bytesToMb(folderSize(this))

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

val File.shortPath: String
    get() = getShortPath(path)

fun getFullPath(path: String): File = File(Environment.getExternalStorageDirectory(), path)

val imageExtensions = listOf("png", "jpg", "webp", "gif")

fun checkExtension(fileName: String): Boolean {
    return imageExtensions.any { fileName.toLowerCase().endsWith(it) }
}

fun getMangaLogo(shortPath: String): String = getMangaLogo(getFullPath(shortPath))
fun getMangaLogo(path: File): String {
    val tempList = path.listFiles { file, s ->
        val fin = File(file, s)
        fin.isFile and (fin.extension in imageExtensions)
    }
    return try {
        if (tempList.isNotEmpty())
            tempList[0].path
        else
            getMangaLogo(path.listFiles { file, s -> File(file, s).isDirectory }[0])
    } catch (ex: Exception) {
        ""
    }
}

fun getChapters(
    path: File,
    readyList: ArrayList<String> = arrayListOf()
): ArrayList<String> {

    fun isContainImg(): Boolean {
        val list = path.listFiles()
        if (list != null) {
            if (list.isEmpty())
                return false
            var imgCount = 0
            var dirCount = 0
            for (file in list) {
                if (file.isFile and (file.extension in imageExtensions))
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
        checkExtension(s)
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


fun Context.getDrawableCompat(layoutRes: Int): Drawable? {
    return ContextCompat.getDrawable(this, layoutRes)
}

fun View.getDrawableCompat(layoutRes: Int): Drawable? {
    return context.getDrawableCompat(layoutRes)
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
    const val add = "add"
    const val abc = "abc"
    const val pop = "pop"

    fun toType(type: String): SortLibrary {
        return when (type) {
            add -> SortLibrary.AddTime
            abc -> SortLibrary.AbcSort
            pop -> SortLibrary.Populate
            else -> SortLibrary.AddTime
        }
    }

    fun toString(type: SortLibrary): String {
        return when (type) {
            SortLibrary.AddTime -> add
            SortLibrary.AbcSort -> abc
            SortLibrary.Populate -> pop
        }
    }
}

fun formatDouble(value: Double?): String = DecimalFormat("#0.00").format(value)

fun bytesToMb(value: Long): Double = value.toDouble() / (1024.0 * 1024.0)

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

    val stream = FileOutputStream(png.absoluteFile)
    b.compress(Bitmap.CompressFormat.PNG, 100, stream)
    stream.close()

    log = if (image.delete()) {
        "oldFile deleted ${image.path}"
    } else {
        "oldFile not deleted ${image.path}"
    }

    return png
}

inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum = 0L
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

class TimeFormat(seconds: Long) {
    var days: Long = 0
    var hours: Long = 0
    var minutes: Long = 0
    var seconds: Long = 0

    init {
        this.seconds = seconds % 60

        val minutes = seconds / 60

        this.minutes = minutes % 60

        val hours = minutes / 60

        this.hours = hours % 24

        val days = hours / 24

        this.days = days
    }

    fun isSeconds() = minutes == 0L && isMinutes()
    fun isMinutes() = hours == 0L && isHours()
    fun isHours() = days == 0L

    fun toString(context: Context): String {
        if (days == 0L && hours == 0L && minutes == 0L && seconds == 0L)
            return context.getString(R.string.time_format_seconds, 0)

        val builder = StringBuilder()

        if (days != 0L) {
            builder.append(context.getString(R.string.time_format_days, days))
            builder.append(" ")
        }
        if (hours != 0L) {
            builder.append(context.getString(R.string.time_format_hours, hours))
            builder.append(" ")
        }
        if (minutes != 0L) {
            builder.append(context.getString(R.string.time_format_minutes, minutes))
            builder.append(" ")
        }
        if (seconds != 0L)
            builder.append(context.getString(R.string.time_format_seconds, seconds))

        return builder.toString()
    }
}
