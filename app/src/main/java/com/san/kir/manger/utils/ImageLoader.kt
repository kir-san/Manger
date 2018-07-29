package com.san.kir.manger.utils

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.san.kir.manger.components.parsing.ManageSites
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import okio.Okio


class ImageLoader(private val url: String) {
    private val POOL = newFixedThreadPoolContext(Runtime.getRuntime().availableProcessors(), "bg")

    private var color = -1
    private var errorResId = -1
    private var error: (() -> Unit)? = null
    private var success: (() -> Unit)? = null

    fun errorColor(color: Int) {
        this.color = color
    }

    fun errorResId(errorResId: Int) {
        this.errorResId = errorResId
    }

    fun onError(action: () -> Unit) {
        error = action
    }

    fun onSuccess(action: () -> Unit) {
        success = action
    }


    fun into(target: ImageView) {
        load(target)
    }

    private fun load(target: ImageView) {
        launch(POOL) {
            //        Получаем имя из url
            val name = getNameFromUrl(url)

//        Проверяем наличие файла с полученным именем в папке cache
            val path = getFullPath("${DIR.CACHE}/$name")

            if (path.exists() && path.length() > 0) {
                //        если файл есть, то отображаем его в imageView
                launch(UI) {
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
                    launch(UI) {
                        target.setImageDrawable(Drawable.createFromPath(path.absolutePath))
                        success?.invoke()
                    }
                } catch (e: Exception) {
                    //          если картинка загрузилась с ошибкой

                    if (errorResId != -1) {
                        //              если была указана картинка для ошибки указываем ее
                        launch(UI) {
                            target.setImageResource(errorResId)
                        }
                    } else if (color != -1) {
                        //              если был указан цвет для ошибки, то устанавливаем цвет
                        launch(UI) {
                            target.setBackgroundColor(color)
                        }
                    }

                    //              если ничего не было указано, то ничего не делаем
                    launch(UI) {
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

object loadImage {
    operator fun invoke(url: String, init: ImageLoader.() -> Unit = {}) =
        ImageLoader(url).apply { init() }

}
