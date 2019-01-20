package com.san.kir.manger.utils

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.san.kir.manger.components.parsing.ManageSites
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors


class ImageLoader(private val url: String) {
    private val pool =
        Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors())
            .asCoroutineDispatcher()

    private var color = -1
    private var errorResId = -1
    private var error: (() -> Unit)? = null
    private var success: (() -> Unit)? = null

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

    fun onSuccess(action: () -> Unit): ImageLoader {
        success = action
        return this
    }


    fun into(target: ImageView): Job {
        return load(target)
    }

    private fun load(target: ImageView): Job {
        return GlobalScope.launch(pool) {
            //        Получаем имя из url
            val name = getNameFromUrl(url)

//        Проверяем наличие файла с полученным именем в папке cache
            val path = getFullPath("${DIR.CACHE}/$name")

            log("path = ${path}")

            if (path.exists() && path.length() > 0) {
                //        если файл есть, то отображаем его в imageView
                withContext(Dispatchers.Main) {
                    target.setImageDrawable(Drawable.createFromPath(path.absolutePath))
                    success?.invoke()
                }
            } else {
                //        если файла нет, то загружаем его из интернета
                try {
                    //          если картинка загрузилась без ошибок, то сохраняем в папке cache
                    Okio.buffer(Okio.sink(path)).apply {
                        writeAll(ManageSites.openLink(url).body()!!.source())
                        close()
                    }
                    //              и отображаем его в imageView
                    withContext(Dispatchers.Main) {
                        target.setImageDrawable(Drawable.createFromPath(path.absolutePath))
                        success?.invoke()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        //          если картинка загрузилась с ошибкой
                        if (errorResId != -1) {
                            //              если была указана картинка для ошибки указываем ее
                            target.setImageResource(errorResId)
                        } else if (color != -1) {
                            //              если был указан цвет для ошибки, то устанавливаем цвет
                            target.setBackgroundColor(color)
                        }

                        //              если ничего не было указано, то ничего не делаем

                        error?.invoke()
                    }
                }
            }
        }
    }

    private fun getNameFromUrl(url: String): String {
        var name = url.split("/").last()

        if (imageExtensions.none { name.endsWith(it, true) }) {
            name = "$name.jpg"
        }

        return name
    }
}

@Suppress("ClassName")
object loadImage {
    operator fun invoke(url: String) =
        ImageLoader(url)

    operator fun invoke(url: String, init: ImageLoader.() -> Unit = {}) =
        ImageLoader(url).init()
}
