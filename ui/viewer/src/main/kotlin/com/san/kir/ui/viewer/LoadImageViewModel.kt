package com.san.kir.ui.viewer

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davemorrissey.labs.subscaleview.ImageSource
import com.san.kir.core.internet.ConnectManager
import com.san.kir.core.utils.convertImagesToPng
import com.san.kir.core.utils.getFullPath
import com.san.kir.core.utils.isOkPng
import com.san.kir.core.utils.log
import com.san.kir.data.store.ViewerStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
internal class LoadImageViewModel @Inject constructor(
    private val connectManager: ConnectManager,
    val store: ViewerStore,
) : ViewModel() {
    private var imageLoadingJob: Job? = null

    private val _state = MutableStateFlow<LoadState>(LoadState.Init)
    val state = _state.asStateFlow()

    fun setInitState() {
        _state.update { LoadState.Init }
    }

    fun load(page: Page.Current?, force: Boolean = false) {
        if (force) {
            log("cancel job")
            imageLoadingJob?.cancel()
        }

        if (imageLoadingJob?.isActive == true) {
            log("job is active")
            return
        }

        imageLoadingJob = startLoadImage(page, force)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startLoadImage(
        page: Page.Current?,
        force: Boolean = false,
    ) = viewModelScope.launch {
        _state.update { LoadState.Init }

        if (page == null) {
            _state.update { LoadState.Error(Throwable("None page")) }
        } else {
            // получаем файл страницы
            val name = connectManager.nameFromUrl(page.pagelink)
            val fullPath = getFullPath(page.chapter.path).absolutePath
            var file = File(fullPath, name)
            file = File(file.parentFile, "${file.nameWithoutExtension}.png")

            // Если файл нужного формата в памяти
            if (file.exists() && file.isOkPng() && !force) {
                _state.update { LoadState.Ready(ImageSource.uri(Uri.fromFile(file))) }
                return@launch
            }

            // Если файл есть, но формат неверный, то конвертировать
            if (file.exists() && !force) {
                val png = convertImagesToPng(file)
                if (png.isOkPng()) {
                    _state.update { LoadState.Ready(ImageSource.uri(Uri.fromFile(png))) }
                    return@launch
                }
            }

            val isOnline = store.data.first().withoutSaveFiles

            // Загрузка файла без сохранения в памяти смартфона
            if (isOnline) {
                kotlin.runCatching {
                    if (page.chapter.link.isEmpty()) {
                        _state.update { LoadState.Error(Throwable("No link")) }
                    } else {
                        connectManager
                            .downloadBitmap(connectManager.prepareUrl(page.pagelink),
                                onFinish = { bm, size, time ->
                                    if (bm != null) {
                                        _state.update {
                                            LoadState.Ready(
                                                ImageSource.cachedBitmap(bm), size, time)
                                        }
                                    }
                                },
                                onProgress = { progress ->
                                    _state.update { LoadState.Load(progress) }
                                })
                    }
                }.onFailure { ex ->
                    ex.printStackTrace()
                    _state.update { LoadState.Error(ex) }
                }
                return@launch
            }

            // Загрузка файла с сохранением в памяти смартфона

            file.delete()
            file = File(fullPath, name)

            kotlin.runCatching {
                connectManager.downloadFile(
                    file, connectManager.prepareUrl(page.pagelink),
                    onProgress = { progress ->
                        _state.update { LoadState.Load(progress) }
                    },
                    onFinish = { size, time ->
                        val imageSource = ImageSource.uri(
                            Uri.fromFile(
                                if (file.extension in arrayOf("gif", "webp", "jpg", "jpeg")) {
                                    convertImagesToPng(file)
                                } else {
                                    file
                                }
                            )
                        )

                        _state.update { LoadState.Ready(imageSource, size, time) }
                    })
            }.onFailure { ex ->
                ex.printStackTrace()
                _state.update { LoadState.Error(ex) }
            }

        }
    }
}