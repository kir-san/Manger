package com.san.kir.manger.components.LatestChapters

import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.components.ChaptersDownloader.ChaptersDownloader
import com.san.kir.manger.dbflow.models.DownloadItem
import com.san.kir.manger.dbflow.models.LatestChapter
import com.san.kir.manger.utils.CHAPTER_STATUS

class LatestChaptersViewModel {
    var chapter = LatestChapter() // Глава манги
    lateinit var task: DownloadItem // Задача для загрузки главы

    val name = Binder("") // Название главы
    val date = Binder("") // Дата появления главы на в сети
    val action = Binder(CHAPTER_STATUS.UNKNOW) // Текущее доступное действие для главы

    val isDownload = Binder(false) // Скачивается ли сейчас глава
    val isRead = Binder(false) // Прочитана или нет глава

    val progressDownload = Binder(1) // Прогресс загрузки главы
    val maxDownload = Binder(1) // Максимальное значение загрузки главы

    val manga = Binder("") // Название манги

// TODO для выделения элементов
//    val selected = Binder(false) // Выделена ли глава

    // Сохранение дейсвия в соответстующую переменную
    fun updateAction() {
        action.item = chapter.action
    }

    // Обновить данные для загрузки главы
    fun updateDownload(new: Boolean = true) {
        if (!new) { // Нужно чтобы не использовать NULL типы
            task.max.unBind(0) // Отписаться от данных
            task.progress.unBind(0) // перед обновлением данных
        }
        progressDownload.item = 0 // Сбросить прогресс
        task = ChaptersDownloader.getTask(chapter) // Получить новую задачу
        task.max.bindAndRun(0) { maxDownload.item = it } // Подписаться на новые данные
        task.progress.bindAndRun(0) { progressDownload.item = it } // Подписаться на новые данные
    }

    // Привязка значений
    fun bind(chapter: LatestChapter /* TODO , isSelect: Boolean*/) {
        this.chapter = chapter
        name.item = chapter.name
        date.item = chapter.date
        isRead.item = chapter.isRead
        manga.item = chapter.manga

        updateAction()
        updateDownload()

        if (ChaptersDownloader.catalog.contains(task)) {
            isDownload.item = (true)
            ChaptersDownloader.bus.onEvent(2) {
                if (it.link == chapter.site) {
                    updateAction()
                    isDownload.item = (false)
                }
            }
        }

        /* TODO selected.item = isSelect*/
    }
}
