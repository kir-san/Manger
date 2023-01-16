package com.san.kir.background.logic

import com.san.kir.background.logic.repo.ChapterRepository
import com.san.kir.core.internet.ConnectManager
import com.san.kir.core.support.DownloadState
import com.san.kir.core.utils.convertImagesToPng
import com.san.kir.core.utils.coroutines.ioLaunch
import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.core.utils.getFullPath
import com.san.kir.core.utils.isOkPng
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.preparedPath
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import timber.log.Timber
import java.io.File
import java.net.SocketException

class ChapterDownloader(
    private var chapter: Chapter,
    private val chapterRepository: ChapterRepository,
    private val connectManager: ConnectManager,
    concurrent: Int,
    private val checkNetwork: suspend () -> Boolean,
    private val onProgress: suspend (Chapter) -> Unit,
) {
    private val semaphore = Semaphore(concurrent)
    private val lock = Mutex()

    private var downloadPages = 0
    private var downloadSize = 0L
    private var downloadTime = 0L

    private suspend fun updateChapter(state: DownloadState = DownloadState.LOADING) {
        lock.withLock {
            chapter = chapter.copy(
                downloadPages = downloadPages,
                downloadSize = downloadSize,
                downloadTime = downloadTime,
                status = state
            )
            chapterRepository.update(chapter)
        }
    }

    suspend fun run(): Result<Unit> {
        val startTime = System.currentTimeMillis()
        updateChapter(DownloadState.QUEUED)
        onProgress(chapter)

        return runCatching {
            downloadChapter()
        }.onSuccess {
            downloadTime = System.currentTimeMillis() - startTime
            updateChapter(DownloadState.COMPLETED)
            chapterRepository.updateStatistic(chapter)
            onProgress(chapter)
        }.onFailure {
            Timber.e(it)
            updateChapter(DownloadState.ERROR)
            onProgress(chapter)
        }
    }

    private suspend fun downloadChapter() = coroutineScope {
        val downloadPath = getFullPath(chapter.preparedPath)
        val pages = withIoContext { chapterRepository.pages(chapter) }

        // Если новополученные странницы не пустые, то испольльзуем их, иначе те что были в главе
        val currentPages =
            if (pages.isNotEmpty() && pages.all { it.isNotEmpty() }) pages
            else chapter.pages

        // Применение последних полученных страниц
        chapter = chapter.copy(pages = currentPages)
        updateChapter()
        onProgress(chapter)

        currentPages.map { link ->
            ioLaunch {
                semaphore.withPermit {
                    downloadPage(link, downloadPath)
                }
            }
        }.joinAll()
    }

    private suspend fun downloadPage(link: String, downloadDirPath: File) {
        val name = connectManager.nameFromUrl(link)
        var file = File(downloadDirPath, name)
        file = File(file.parentFile, "${file.nameWithoutExtension}.png")

        when {
            file.exists() && file.isOkPng() -> lock.withLock {
                downloadPages++
                downloadSize += file.length()
            }

            file.exists()                   -> {
                val png = convertImagesToPng(file)

                if (png.isOkPng())
                    lock.withLock {
                        downloadPages++
                        downloadSize += png.length()
                    }
                else {
                    png.delete()
                    tryDownloadPage(link, file)
                }
            }

            else                            -> tryDownloadPage(link, file)
        }

        updateChapter()
        onProgress(chapter)
    }

    private suspend fun tryDownloadPage(link: String, downloadFile: File) {
        var contentLength = 0L
        var tryCount = 3

        // 3 попытки загрузить изображение
        while (tryCount != 0) {
            if (link.isEmpty()) return


            val result = connectManager
                .downloadFile(downloadFile, link)

            // Если произошла ошибка из-за отстутсвия интернета,
            // то перезапуск загрузки после его появления
            if (result.isFailure && checkNetwork().not()) continue

            when (result.exceptionOrNull()) {
                is SocketException -> {
                    tryCount--
                    continue
                }
            }

            contentLength = result.getOrThrow().contentLength

            // Если размер исходного и загруженного одинаков, то страница загружена
            if (downloadFile.exists() && downloadFile.length() == contentLength) {
                val png = convertImagesToPng(downloadFile)
                if (png.isOkPng()) break
                png.delete()
            }
            tryCount--
        }

        lock.withLock {
            downloadSize += contentLength
            downloadPages++
        }
    }

    private suspend inline fun update(chapter: Chapter) =
        chapter.also { chapterRepository.update(it) }
}
