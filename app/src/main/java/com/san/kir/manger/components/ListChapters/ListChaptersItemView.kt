package com.san.kir.manger.components.ListChapters

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.Extending.AnkoExtend.bind
import com.san.kir.manger.R
import com.san.kir.manger.components.ChaptersDownloader.ChaptersDownloader
import com.san.kir.manger.components.Viewer.ViewerActivity
import com.san.kir.manger.dbflow.models.Chapter
import com.san.kir.manger.dbflow.models.DownloadItem
import com.san.kir.manger.utils.CHAPTER_STATUS
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.delChapters
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.isFirstRun
import com.san.kir.manger.utils.isNotEmptyDirectory
import com.san.kir.manger.utils.log
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alert
import org.jetbrains.anko.alignParentEnd
import org.jetbrains.anko.alignParentLeft
import org.jetbrains.anko.alignParentRight
import org.jetbrains.anko.alignParentTop
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.below
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.centerVertically
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftOf
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.longToast
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.progressBar
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onLongClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textView
import org.jetbrains.anko.toast
import org.jetbrains.anko.topPadding
import org.jetbrains.anko.wrapContent
import java.io.IOException

class ListChaptersItemView(val activity: ListChaptersActivity) : AnkoComponent<ViewGroup> {

    private object _id { // id элементов для связи между собой
        val readStatus = ID.generate()
        val date = ID.generate()
        val action = ID.generate()
        val progressBar = ID.generate()
        val progressText = ID.generate()
        val name = ID.generate()
        // не удалять данные переменные иначе не работают правильно биндинги
    }

    var chapter = Chapter() // Глава манги
    lateinit var task: DownloadItem // Задача для загрузки главы

    var position = 0 // Позиция в списке

    val name = Binder("") // Название главы
    val date = Binder("") // Дата появления главы на в сети
    val progress = Binder(0 to 0) // Текущий прогресс и максимальное значение
    val action = Binder(CHAPTER_STATUS.UNKNOW) // Текущее доступное действие для главы
    val isRead = Binder(false) // Прочитана или нет глава

    val isDownload = Binder(false) // Скачивается ли сейчас глава

    val progressDownload = Binder(1) // Прогресс загрузки главы
    val maxDownload = Binder(1) // Максимальное значение загрузки главы

    val selected = Binder(false) // Выделена ли глава


    fun createView(parent: ViewGroup): View {
        return createView(AnkoContext.create(parent.context, parent))
    }

    override fun createView(ui: AnkoContext<ViewGroup>): View {

        val liteGrey = Color.parseColor("#a5a2a2")
        val white = Color.parseColor("#FFF4F2F2")
        val colorSelected = Color.parseColor("#9934b5e4")


        var cancel_task: Long = 0 // Сохранение времени для отмены скачивания

        return with(ui) {

            val buttonSize = dip(45)

            // Корень (требуется для отображения прочитана ли глава)
            linearLayout {
                lparams(width = matchParent, height = wrapContent) {
                    margin = dip(1)
                }

                bind(isRead) {
                    // Если глава прочитана
                    backgroundColor =
                            if (it) liteGrey // Сменить цвет на потемнее
                            else white // Иначе белый
                }

                // Подкорень (требуется для отображения выделения и всего остального)
                relativeLayout {
                    lparams(width = matchParent, height = dip(55))

                    isClickable = true
                    isLongClickable = true

                    onClick {
                        launch(UI) {
                            // Открыть главу для чтения
                            if (activity.actionMode == null) // Если выключен экшнМод
                                if (isDownload.item) // Если глава уже качается, то показать сообщение
                                    toast(R.string.list_chapters_open_is_download)
                                else { // Иначе
                                    val chapter = chapter
                                    // Если папка существует и не пуста, то открыть для чтения
                                    if (getFullPath(chapter.path).isNotEmptyDirectory) {
                                        isFirstRun = true // Указаывает что активити запускается впервые
                                        startActivity<ViewerActivity>(
                                                "manga_name" to chapter.manga,
                                                "chapter" to chapter.name,
                                                "page_position" to chapter.progress)
                                    } else // Иначе показать сообщение
                                        longToast(R.string.list_chapters_open_not_exists)
                                }
                            else // Иначе выделить елемент
                                activity.onListItemSelect(position)
                        }
                    }
                    onLongClick {
                        // Выделить элемент
                        activity.onListItemSelect(position)
                    }
                    bind(selected) {
                        // В зависимости от выделения менять цвет пункта
                        backgroundColor =
                                if (it) colorSelected
                                else Color.TRANSPARENT
                    }


                    // Название главы
                    textView {
                        id = _id.name
                        maxLines = 1
                        padding = dip(4)
                        typeface = Typeface.DEFAULT_BOLD

                        bind(name) {
                            // Присвоение названия главы после загрузки
                            text = it
                        }
                    }.lparams(width = wrapContent, height = wrapContent) {
                        alignParentTop()
                        alignParentLeft()
                        gravity = Gravity.CENTER_VERTICAL
                        leftOf(_id.action) // слева от кнопки действий
                    }

                    // Статус прочитанной главы
                    textView {
                        id = _id.readStatus

                        bottomPadding = dip(4)
                        leftPadding = dip(3)
                        rightPadding = dip(3)
                        topPadding = dip(4)

                        bind(progress) { (current, all) ->
                            // Установка значений в шаблон для вывода текста
                            text = resources.getString(R.string.list_chapters_read,
                                                       current,
                                                       all)
                        }
                    }.lparams(width = wrapContent, height = wrapContent) {
                        alignParentLeft()
                        below(_id.name) // ниже чем название
                        leftOf(_id.date) // слева от даты
                    }

                    // Дата
                    textView {
                        id = _id.date

                        padding = dip(4)

                        bind(date) {
                            // Установка текста даты
                            text = it
                        }
                    }.lparams(width = wrapContent, height = wrapContent) {
                        below(_id.name) // Под названием
                        gravity = Gravity.CENTER_VERTICAL
                        leftOf(_id.action) // Слева от кнопок действий
                    }

                    // Допустимые действия для главы
                    imageView {
                        id = _id.action

                        isClickable = true
                        scaleType = ImageView.ScaleType.FIT_CENTER

                        onClick {
                            launch(UI) {
                                // Скачать или удалить
                                when (action.item) {
                                    CHAPTER_STATUS.DELETE -> { // если режим удаления
                                        // показать окно для подтверждения действия
                                        alert(R.string.list_chapters_delete_text) {
                                            positiveButton(R.string.list_chapters_delete_yes) {
                                                try {
                                                    val (acc, max) = delChapters(chapter) // При удалении главы
                                                    if (acc != max) // проверить результат
                                                    // если не удалено
                                                        toast(R.string.list_chapters_delete_not_delete)
                                                    else {
                                                        // если удалено
                                                        toast(R.string.list_chapters_delete_okay_delete)
                                                        updateAction()
                                                    }
                                                } catch (ex: IOException) {
                                                    // В случае ошибки
                                                    toast(R.string.list_chapters_delete_error)
                                                    ex.printStackTrace()
                                                }
                                            }
                                            negativeButton(R.string.list_chapters_delete_no) {}
                                        }.show()
                                    }
                                    CHAPTER_STATUS.DOWNLOADABLE -> { // Если выбрана загрузка файла
                                        toast("Пробую скачать главу")
                                        isDownload.item = true // Переключить режим в загрузку
                                        updateDownload(false)
                                        ChaptersDownloader.addTask(task) // Добавить задачу в загрузщик
                                        ChaptersDownloader.bus.onEvent(2) {
                                            // Подписаться на его действия
                                            if (it.link == task.link) { // Если глава соответствует выполняемой
                                                updateAction() // Обновить кнопку действий
                                                isDownload.item = false // Выключить режим загрузки
                                                progress.item = progress.item.first to progress.item.second // Обновить значения
                                                updateProgressChapter()
                                            }
                                        }
                                    }
                                    else -> { // Если вообще произошла абракадабра, вывод в лог несколько сообщений
                                        log = chapter.path // Путь хранения главы
                                        log = getFullPath(chapter.path).path // Полный путь до главы
                                        // Не знаю зачем, но пусть так
                                    }
                                }
                            }
                        }

                        bind(isDownload) {
                            // Включение видимости и ее отключение в зависимости от режима загрузки
                            visibility =
                                    if (it) View.INVISIBLE
                                    else View.VISIBLE
                        }
                        bind(action) {
                            // Менять иконку кнопки
                            backgroundResource =
                                    when (it) {
                                        CHAPTER_STATUS.DELETE -> R.drawable.ic_action_delete_black // Кнопка удаления
                                        CHAPTER_STATUS.NOT_LOADED -> R.drawable.ic_action_download_black // Кнопка отсутсвия действий
                                        CHAPTER_STATUS.DOWNLOADABLE -> R.drawable.ic_action_download_green // Кнопка скачивания
                                        else -> R.drawable.ic_action_download_black // Все остальные
                                    }
                        }
                    }.lparams(width = buttonSize, height = buttonSize) {
                        alignParentEnd()
                        alignParentRight()
                        centerVertically()
                    }

                    // Отображения статуса загрузки
                    progressBar {
                        id = _id.progressBar

                        isClickable = true

                        onClick {
                            launch(UI) {
                                // Отмена загрузки
                                // Если прошло меньше 2 секунд с момента предыдущего нажатия на прогрессБар
                                if (cancel_task + 2000 > System.currentTimeMillis()) {
                                    ChaptersDownloader.cancelTask(task) // Отменить задачу
                                    updateAction() // Обновить кнопку действий
                                    isDownload.item = false // Выключить режим загрузки
                                    updateProgressChapter()
                                } else // Иначе показать сообщение
                                    toast(R.string.list_chapters_download_cancel)
                                cancel_task = System.currentTimeMillis() // Сохранить текущее время
                            }
                        }

                        bind(isDownload) {
                            // Переключение видимости в зависимости от режима загрузки
                            visibility =
                                    if (it) View.VISIBLE
                                    else View.INVISIBLE
                        }
                    }.lparams(width = buttonSize, height = buttonSize) {
                        alignParentEnd()
                        centerVertically()
                        alignParentRight()
                    }

                    // Текстовый прогресс загрузки главы
                    textView {
                        id = _id.progressText

                        gravity = Gravity.CENTER

                        bind(isDownload) {
                            // Переключение видимости в зависимости от режима загрузки
                            visibility =
                                    if (it) View.VISIBLE
                                    else View.INVISIBLE
                        }
                        bind(progressDownload) {
                            // Обновления текста
                            post {
                                // данный метод нужен для работы в нужном потоке
                                val progress = it * 100 / maxDownload.item
                                text = resources.getString(R.string.list_chapters_download_progress,
                                                           progress)
                            }
                        }

                    }.lparams(width = buttonSize, height = buttonSize) {
                        alignParentEnd()
                        centerVertically()
                        alignParentRight()
                    }

                }
            }

        }
    }

    // Сохранение дейсвия в соответстующую переменную
    fun updateAction() {
        action.item = chapter.action
    }

    // Обновить количество страниц главы
    fun updateProgressChapter() {
        progress.item = chapter.progress to chapter.countPages
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
    fun bind(chapter: Chapter, isSelect: Boolean, position: Int) {
        this.chapter = chapter
        name.item = chapter.name
        date.item = chapter.date
        updateProgressChapter()
        updateAction()
        isRead.item = chapter.isRead

        updateDownload()

        if (ChaptersDownloader.catalog.contains(task)) { // Если данная глава скачивается в данный момент
            isDownload.item = true // Включить режим загрузки
            ChaptersDownloader.bus.onEvent(2) {
                // Подписаться на события загрузчика, когда закончится загрузка
                if (it.link == chapter.site) { /// Если глава соответствует
                    updateAction() // Обновить действия
                    isDownload.item = false // Выключить режим загрузки
                    updateProgressChapter() // Обновить количество страниц
                }
            }
        }
        selected.item = isSelect
        this.position = position
    }
}
