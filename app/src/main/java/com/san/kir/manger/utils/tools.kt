package com.san.kir.manger.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.raizlabs.android.dbflow.structure.cache.LruCache
import com.san.kir.manger.App
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.dbflow.models.Chapter
import com.san.kir.manger.dbflow.wrapers.ChapterWrapper
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.Response
import okio.Okio
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.util.regex.Pattern

object ID {
    private var _count = 0

    fun generate(): Int {
        return _count++
    }
}

fun isExternalStorageWritable(): Boolean {
    if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState())
        return true
    return false
}

fun createDirs(path: File): Boolean {
    path.mkdirs()
    if (!path.isDirectory)
        log = "Error ${path.path}"
    return path.exists()
}

fun createDirs(path: String): Boolean = createDirs(File(path))

fun folderSize(directory: File): Long {
    var length: Long = 0
    val files: Array<out File>? = directory.listFiles()
    files?.let {
        for (file in it) {
            if (file.isFile) length += file.length()
            else length += folderSize(file)
        }
    }
    return length
}

val File.lengthMb: Long
    get() = (folderSize(this) / (1024 * 1024))

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


fun getShortPath(path: String): String = Regex("/${DIR.ROOT}.+").find(path)!!.value
fun getShortPath(path: File): String = getShortPath(path.path)

fun getFullPath(path: String): File = File(SET_MEMORY, path)

val extensions = listOf("png", "jpg", "webp")

fun getMangaLogo(path: File): String {
    log = path.path
    val templist = path.listFiles { file, s ->
        val fin = File(file, s)
        fin.isFile and (fin.extension in extensions)
    }
    if (templist.isNotEmpty())
        return templist[0].path
    else
        return getMangaLogo(path.listFiles { file, s -> File(file, s).isDirectory }[0])
}

/*fun getPath(context: Context, uri: Uri): String? {
    if ("content".equals(uri.scheme, true)) {
        val projection = arrayOf("_data")
        val cursor: Cursor?
        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            val column_index = cursor!!.getColumnIndexOrThrow("_data")
            if (cursor.moveToNext())
                return cursor.getString(column_index)
        } catch(e: Exception) {
        }
    } else if ("file".equals(uri.scheme, true))
        return uri.path

    return null
}*/

fun getChapters(path: File,
                readyList: ArrayList<String> = arrayListOf<String>()): ArrayList<String> {
    val tempList = readyList

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
        tempList.add(path.path)
    } else {
        path.listFiles { file, s ->
            File(file, s).isDirectory
        }?.forEach { getChapters(it, tempList) }
    }
    return tempList
}

fun getChapters(path: String, readyList: ArrayList<String> = arrayListOf<String>()) = getChapters(
        File(path), readyList)

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

fun getImageBitmap(file: File): Bitmap {
    try {
        val cachedImg = cache.get(file.path)
        if (cachedImg == null) {

            val or = BitmapFactory.Options()
            or.inJustDecodeBounds = true
            BitmapFactory.decodeFile(file.path, or)

            val fis = FileInputStream(file)
            val bis = BufferedInputStream(fis)
            val o = BitmapFactory.Options()
            o.inPreferredConfig = Bitmap.Config.RGB_565
            o.inSampleSize = 2
            val img = BitmapFactory.decodeStream(bis, null, o)
            log = "after   width = ${img.width}    height = ${img.height}"
            log = "byteCount = ${img.byteCount}"
            bis.close()
            fis.close()
            cache.put(file.path, img)
            return img
        } else {
            return cachedImg
        }
    } catch(ex: Exception) {
        Log.e(TAG, "Picture load problem: ${ex.message}", ex)
    }
    return Bitmap.createBitmap(100, 100, Bitmap.Config.RGB_565)
}

data class ResultDeleting(val current: Int, val max: Int)

fun delAllReadChapters(manga: String): ResultDeleting {
    return delChapters(ChapterWrapper.getChapters(manga).filter { it.isRead })
}

fun delChapters(vararg chapters: Chapter): ResultDeleting {
    return delChapters(chapters.toList())
}

fun delChapters(chapters: List<Chapter>): ResultDeleting {
    return delFiles(chapters.map { it.path })
}

fun delFile(path: String): ResultDeleting {
    return delFiles(listOf(path))
}

fun delFiles(filesPath: List<String>): ResultDeleting {
    var acc = 0
    filesPath.forEach { path ->
        getFullPath(path).apply { if (exists() && deleteRecursively()) acc++ }
    }
    return ResultDeleting(current = acc, max = filesPath.size)
}

fun Response.downloadTo(file: File) = runBlocking {
    val sink = Okio.buffer(Okio.sink(file)) // с помощью okio сохраняю загруженные данные в файл
    sink.writeAll(this@downloadTo.body().source())
    sink.close()
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

// избавитель от родителя
fun View.withoutParent(): View {
    val parent: ViewGroup = this.parent as ViewGroup
    parent.removeView(this)
    return this
}

// Получить путь к файлу из ссылки
fun getPathForUrl(url: String,
                  onComplete: (String) -> Unit,
                  onError: (Throwable) -> Unit) = launch(CommonPool) {
    try {
        // Если это и так уже путь к файлу, то отдаем его
        if (getFullPath(url).exists())
            launch(UI) {
                onComplete.invoke(getFullPath(url).path)
            }
        else { // Иначе
            // из ссылки получаю имя для файла
            val pat = Pattern.compile("[a-z0-9._-]+\\.[a-z]{2,4}").matcher(url)
            var name: String = ""
            while (pat.find()) name = pat.group()

            // Создаю файл в кеше с полученным именем
            val path = File(App.context.externalCacheDir, name)

            if (!path.exists()) // Если файла такого нет
                ManageSites.asyncOpenLink(url).downloadTo(path) // Скачиваю
            launch(UI) {
                onComplete.invoke(path.path)
            }
        }
    } catch (ex: Throwable) {
        launch(UI) {
            onError.invoke(ex)
        }
    }
}

fun getIconForSite(url: String,
                   onComplete: (String) -> Unit,
                   onError: (Throwable) -> Unit) = launch(CommonPool) {
    try {
        // Создаю файл в кеше с полученным именем
        val path = File(App.context.externalCacheDir, url)

        if (!path.exists()) // Если файла такого нет
            ManageSites
                    .asyncOpenLink("http://www.google.com/s2/favicons?domain=$url")
                    .downloadTo(path) // Скачиваю
        launch(UI) {
            onComplete.invoke(path.path)
        }
    } catch (ex: Throwable) {
        launch(UI) {
            onError.invoke(ex)
        }
    }
}
