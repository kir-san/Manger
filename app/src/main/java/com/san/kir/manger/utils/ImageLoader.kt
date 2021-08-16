package com.san.kir.manger.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import android.widget.ImageView
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.utils.enums.DIR
import com.san.kir.manger.utils.extensions.createDirs
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.utils.extensions.imageExtensions
import com.san.kir.manger.utils.extensions.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors

class ImageLoader(private val url: String) {

    private val pool = Executors
        .newFixedThreadPool(1)
        .asCoroutineDispatcher()

    private val mMemoryCache = MemoryCache()
    private val mDiskCache = DiskCache()

    private var color = -1
    private var errorResId = -1
    private var error: (() -> Unit)? = null
    private var success: ((ImageBitmap) -> Unit)? = null
    private var trying: (() -> Boolean)? = null

    fun errorColor(color: Int): ImageLoader {
        this.color = color
        return this
    }

    fun errorResId(errorResId: Int): ImageLoader {
        this.errorResId = errorResId
        return this
    }

    fun onError(action: () -> Unit): ImageLoader {
        error = action
        return this
    }

    fun onSuccess(action: (ImageBitmap) -> Unit): ImageLoader {
        success = action
        return this
    }

    fun beforeTry(action: () -> Boolean): ImageLoader {
        trying = action
        return this
    }

    fun into(target: ImageView): Job? {
        return if (trying == null || !trying!!.invoke())
            load(target)
        else
            null
    }

    fun start(): Job? {
        return if (trying == null || !trying!!.invoke())
            load()
        else
            null
    }

    private fun load(target: ImageView? = null): Job {
        return GlobalScope.launch(pool) {
            //        Получаем имя из url
            val name = getNameFromUrl(url)

            mMemoryCache.get(name)?.also {
                withContext(Dispatchers.Main) {
                    target?.setImageBitmap(it)
                    success?.invoke(it.asImageBitmap())
                }
                return@launch
            }

            getDiskBitmap(name)?.also {
                withContext(Dispatchers.Main) {
                    target?.setImageBitmap(it)
                    success?.invoke(it.asImageBitmap())
                }
                return@launch
            }

            getNetworkBitmap(url, name)?.also {
                withContext(Dispatchers.Main) {
                    target?.setImageBitmap(it)
                    success?.invoke(it.asImageBitmap())
                }
                return@launch
            } ?: run {
                target?.context?.let { tryUpdateLogoLink(url, it) }
            }

            withContext(Dispatchers.Main) {
                // если картинка загрузилась с ошибкой
                when {
                    errorResId != -1 -> {
                        // если была указана картинка для ошибки указываем ее
                        try {
                            target?.setImageResource(errorResId)
                        } catch (ex: Resources.NotFoundException) {
                            target?.setBackgroundColor(errorResId)
                        }
                    }
                    color != -1 -> {
                        // если был указан цвет для ошибки, то устанавливаем цвет
                        target?.setBackgroundColor(color)
                    }
                    else -> {
                    }
                }
            }
            error?.invoke()
        }
    }

    private fun getNameFromUrl(url: String): String {
        var parent = url.split("/").last()

        if (imageExtensions.none { parent.endsWith(it, true) }) {
            parent = "$parent.jpg"
        }

        return parent
    }

    private fun getDiskBitmap(name: String): Bitmap? {
        decode(name)?.also {
            mMemoryCache.put(name, it)
            return it
        }

        return null
    }

    private fun decode(name: String): Bitmap? {
        val f = mDiskCache.get(name)
        return if (f.exists() && f.length() > 0) {
            BitmapFactory.decodeFile(f.absolutePath)
        } else {
            f.delete()
            null
        }
    }

    private fun getNetworkBitmap(url: String, name: String): Bitmap? {
        if (url.isBlank()) return null
        val (_, _, result) = Fuel.download(url)
            .fileDestination { _, _ ->
                val createFile = mDiskCache.createFile(name)
                createFile
            }
            .response()

        return when (result) {
            is Result.Success -> getDiskBitmap(name)
            is Result.Failure -> {
                log("url = $url")
                result.getException().printStackTrace()
                null
            }
        }
    }

    private suspend fun tryUpdateLogoLink(url: String, context: Context) {
        val mangaRepository = MangaRepository(context)
        mangaRepository.getFromLogoUrl(url)?.also { manga ->
            ManageSites.getElementOnline(manga.host + manga.shortLink)?.also { element ->
                manga.logo = element.logo
                mangaRepository.update(manga)
            }
        }
    }
}

private class MemoryCache : LruCache<String, Bitmap>(cacheSize) {
    companion object {
        private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        private val cacheSize = maxMemory / 8
    }

    override fun sizeOf(key: String, value: Bitmap): Int {
        return value.byteCount / 1024
    }
}

private class DiskCache {
    fun get(name: String): File {
        return getFullPath("${DIR.CACHE}/$name")
    }

    fun createFile(name: String): File {
        val path = get(name)
        (path.parentFile).createDirs()
        path.createNewFile()
        return path
    }
}

@Suppress("ClassName")
object loadImage {
    operator fun invoke(url: String) =
        ImageLoader(url)

    operator fun invoke(url: String, init: ImageLoader.() -> Unit = {}) =
        ImageLoader(url).init()
}
