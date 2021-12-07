package com.san.kir.ui.viewer

import android.net.Uri
import com.davemorrissey.labs.subscaleview.ImageSource
import com.san.kir.core.internet.ConnectManager
import com.san.kir.core.utils.convertImagesToPng
import com.san.kir.core.utils.getFullPath
import com.san.kir.core.utils.isOkPng
import com.san.kir.core.utils.log
import com.san.kir.data.store.ViewerStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject

internal class LoadImage @Inject constructor(
    private val connectManager: ConnectManager,
    val store: ViewerStore,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun flow(
        page: Page.Current?,
        force: Boolean = false,
    ) = channelFlow {
        log("loadImage")
        send(LoadState.Init)

        if (page == null) {
            send(LoadState.Error(Throwable("None page")))
            close()
            return@channelFlow
        } else {
            // получаем файл страницы
            val name = connectManager.nameFromUrl(page.pagelink)
            val fullPath = getFullPath(page.chapter.path).absolutePath
            var file = File(fullPath, name)
            file = File(file.parentFile, "${file.nameWithoutExtension}.png")

            // Если файл нужного формата в памяти
            if (file.exists() && file.isOkPng() && !force) {
                send(LoadState.Ready(ImageSource.uri(Uri.fromFile(file))))
                close()
                return@channelFlow
            }

            // Если файл есть, но формат неверный, то конвертировать
            if (file.exists() && !force) {
                val png = convertImagesToPng(file)
                if (png.isOkPng()) {
                    send(LoadState.Ready(ImageSource.uri(Uri.fromFile(png))))
                    close()
                    return@channelFlow
                }
            }

            val isOnline = store.data.first().withoutSaveFiles

            // Загрузка файла без сохранения в памяти смартфона
            if (isOnline) {
                kotlin.runCatching {
                    if (page.chapter.link.isEmpty()) {
                        send(LoadState.Error(Throwable("No link")))
                    } else {
                        connectManager.downloadBitmap(connectManager.prepareUrl(page.pagelink)) {
                            trySend(LoadState.Load(it))
                        }.also { bitmap ->
                            bitmap?.let { bm ->
                                send(LoadState.Ready(ImageSource.bitmap(bm)))
                            }
                        }
                    }
                }.onFailure {
                    it.printStackTrace()
                    send(LoadState.Error(it))
                }
                close()
                return@channelFlow
            }

            // Загрузка файла с сохранением в памяти смартфона

            file.delete()
            file = File(fullPath, name)

            kotlin.runCatching {
                connectManager.downloadFile(file,
                    connectManager.prepareUrl(page.pagelink)) {
                    trySend(LoadState.Load(it))
                }
            }.onFailure {
                it.printStackTrace()
                send(LoadState.Error(it))
            }


            val imageSource = ImageSource.uri(
                Uri.fromFile(
                    if (file.extension in arrayOf("gif", "webp", "jpg", "jpeg")) {
                        convertImagesToPng(file)
                    } else {
                        file
                    }
                )
            )

            send(LoadState.Ready(imageSource))
            close()
        }
    }
}
