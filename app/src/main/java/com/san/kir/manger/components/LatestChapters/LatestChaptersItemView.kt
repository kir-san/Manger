package com.san.kir.manger.components.LatestChapters

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView.ScaleType.FIT_CENTER
import com.san.kir.manger.Extending.AnkoExtend.bind
import com.san.kir.manger.R
import com.san.kir.manger.R.string
import com.san.kir.manger.components.ChaptersDownloader.ChaptersDownloader
import com.san.kir.manger.utils.CHAPTER_STATUS
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.delFile
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.log
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
import org.jetbrains.anko.lines
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.progressBar
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.sameTop
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.textView
import org.jetbrains.anko.toast
import org.jetbrains.anko.topPadding
import org.jetbrains.anko.wrapContent
import java.io.IOException

class LatestChaptersItemView(val vm: LatestChaptersViewModel) : AnkoComponent<ViewGroup> {
    private object _id { // id элементов для связи между собой
        val manga = ID.generate()
        val date = ID.generate()
        val action = ID.generate()
        val progressBar = ID.generate()
        val progressText = ID.generate()
        val name = ID.generate()
    }

    fun createView(parent: ViewGroup): View {
        return createView(AnkoContext.create(parent.context, parent))
    }

    override fun createView(ui: AnkoContext<ViewGroup>): View {

        val liteGrey = Color.parseColor("#a5a2a2")
        val white = Color.parseColor("#ffffff")
        val colorSelected = Color.parseColor("#9934b5e4")


        var cancel_task: Long = 0 // Сохранение времени для отмены скачивания

        return with(ui) {

            val buttonSize = dip(45)

            // Корень (требуется для отображения прочитана ли глава)
            linearLayout {
                lparams(width = matchParent, height = wrapContent) {
                    margin = dip(1)
                }

                bind(vm.isRead) {
                    // Если глава прочитана
                    backgroundColor =
                            if (it) liteGrey // Сменить цвет на потемнее
                            else white // Иначе белый
                }

                // Подкорень (требуется для отображения выделения и всего остального)
                relativeLayout {
                    lparams(width = matchParent, height = dip(55))

                    // TODO добавить выделение элементов для списка последних глав
                    // TODO добавить кнопку очистки списка

                    /*onLongClick {
                        // Выделить элемент
                        vm.activity.onListItemSelect(vm.position)
                        return@onLongClick false
                    }*/

                    /*bind(vm.selected) {
                        // В зависимости от выделения менять цвет пункта
                        backgroundColor =
                                if (it) colorSelected
                                else Color.TRANSPARENT
                    }*/


                    // Название главы
                    textView {
                        id = _id.name
                        maxLines = 1
                        padding = dip(4)
                        typeface = Typeface.DEFAULT_BOLD

                        bind(vm.name) { text = it }
                    }.lparams(width = wrapContent, height = wrapContent) {
                        alignParentTop()
                        alignParentLeft()
                        gravity = Gravity.CENTER_VERTICAL
                        leftOf(_id.action) // слева от кнопки действий
                    }

                    // Дата
                    textView {
                        id = _id.date

                        padding = dip(4)

                        bind(vm.date) {
                            // Установка текста даты
                            text = it
                        }
                    }.lparams(width = wrapContent, height = wrapContent) {
                        sameTop(_id.manga)
                        gravity = Gravity.CENTER_VERTICAL
                        leftOf(_id.action) // Слева от кнопок действий
                    }

                    textView {
                        id = _id.manga
                        topPadding = dip(4)
                        bottomPadding = dip(4)
                        leftPadding = dip(3)
                        rightPadding = dip(3)
                        lines = 1
                        bind(vm.manga) { text = it }
                    }.lparams(width = wrapContent, height = wrapContent) {
                        //                        alignParentBottom()
                        alignParentLeft()
                        below(_id.name)
                        leftOf(_id.date)
                    }

                    // Допустимые действия для главы
                    imageView {
                        id = _id.action

                        isClickable = true
                        scaleType = FIT_CENTER

                        onClick {
                            // Скачать или удалить
                            when (vm.action.item) {
                                CHAPTER_STATUS.DELETE -> { // если режим удаления
                                    // показать окно для подтверждения действия
                                    alert(string.list_chapters_delete_text) {
                                        positiveButton(string.list_chapters_delete_yes) {
                                            try {
                                                val (acc, max) = delFile(vm.chapter.path) // При удалении главы
                                                if (acc != max) // проверить результат
                                                // если не удалено
                                                    toast(string.list_chapters_delete_not_delete)
                                                else {
                                                    // если удалено
                                                    toast(string.list_chapters_delete_okay_delete)
                                                    vm.updateAction()
                                                }
                                            } catch (ex: IOException) {
                                                // В случае ошибки
                                                toast(string.list_chapters_delete_error)
                                                ex.printStackTrace()
                                            }
                                        }
                                        negativeButton(string.list_chapters_delete_no) {}
                                    }.show()
                                }
                                CHAPTER_STATUS.DOWNLOADABLE -> { // Если выбрана загрузка файла
                                    toast("Пробую скачать главу")
                                    vm.isDownload.item = true // Переключить режим в загрузку
                                    vm.updateDownload(false)
                                    ChaptersDownloader.addTask(vm.task) // Добавить задачу в загрузщик
                                    ChaptersDownloader.bus.onEvent(2) {
                                        // Подписаться на его действия
                                        if (it.link == vm.task.link) { // Если глава соответствует выполняемой
                                            vm.updateAction() // Обновить кнопку действий
                                            vm.isDownload.item = false // Выключить режим загрузки
                                        }
                                    }
                                }
                                else -> { // Если вообще произошла абракадабра, вывод в лог несколько сообщений
                                    log = vm.chapter.path // Путь хранения главы
                                    log = getFullPath(vm.chapter.path).path // Полный путь до главы
                                    // Не знаю зачем, но пусть так
                                }
                            }
                        }

                        bind(vm.isDownload) {
                            // Включение видимости и ее отключение в зависимости от режима загрузки
                            visibility =
                                    if (it) View.INVISIBLE
                                    else View.VISIBLE
                        }
                        bind(vm.action) {
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
                            // Отмена загрузки
                            // Если прошло меньше 2 секунд с момента предыдущего нажатия на прогрессБар
                            if (cancel_task + 2000 > System.currentTimeMillis()) {
                                ChaptersDownloader.cancelTask(vm.task) // Отменить задачу
                                vm.updateAction() // Обновить кнопку действий
                                vm.isDownload.item = false // Выключить режим загрузки
                            } else // Иначе показать сообщение
                                toast(string.list_chapters_download_cancel)
                            cancel_task = System.currentTimeMillis() // Сохранить текущее время
                        }

                        bind(vm.isDownload) {
                            // Переключение видимости в зависимости от режима загрузки
                            visibility =
                                    if (it) View.VISIBLE
                                    else View.INVISIBLE
                        }
                    }.lparams(width = buttonSize, height = buttonSize) {
                        centerVertically()
                        alignParentRight()
                    }

                    // Текстовый прогресс загрузки главы
                    textView {
                        id = _id.progressText

                        gravity = Gravity.CENTER

                        bind(vm.isDownload) {
                            // Переключение видимости в зависимости от режима загрузки
                            visibility =
                                    if (it) View.VISIBLE
                                    else View.INVISIBLE
                        }
                        bind(vm.progressDownload) {
                            // Обновления текста
                            post {
                                // данный метод нужен для работы в нужном потоке
                                val progress = it * 100 / vm.maxDownload.item
                                text = resources.getString(string.list_chapters_download_progress,
                                                           progress)
                            }
                        }

                    }.lparams(width = buttonSize, height = buttonSize) {
                        centerVertically()
                        alignParentRight()
                    }

                }
            }

        }
    }
}
