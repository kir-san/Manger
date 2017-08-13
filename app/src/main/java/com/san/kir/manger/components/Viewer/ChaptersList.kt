package com.san.kir.manger.components.Viewer


import com.san.kir.manger.dbflow.models.Chapter
import com.san.kir.manger.dbflow.wrapers.ChapterWrapper
import com.san.kir.manger.utils.extensions
import com.san.kir.manger.utils.getFullPath
import java.io.File
import java.util.*

// Свой класс для управления страницами и главами
class ChaptersList(mangaName: String, chapter: String, position_page: Int) {
    var page = PageObject
    var chapter = ChapterObject

    init {
        m.list_chapter.clear() // Очистить список
        m.list_chapter.addAll(ChapterWrapper.getChapters(mangaName)) // Получение глав
        m.position_chapter = findChapterPosition(chapter) // Установка текущей главы
        PageObject.updateList() // Получение списка страниц для главы
        PageObject.position = when { // Установка текущей страницы
            position_page <= 0 -> 0 // Если не больше 0, то ноль
            else -> position_page // Иначе как есть
        }
    }

    private fun findChapterPosition(chapter: String): Int { // Позиции главы, по названию главы
        val lastIndex = m.list_chapter.size - 1 // Последняя позиция
        // Проверка всех названий глав на соответствие, если ничего нет, то позиция равна 0
        return (0..lastIndex).firstOrNull { m.list_chapter[it].name == chapter } ?: 0
    }

    private object m { // маленький объект
        val reg = Regex("(^\\d+$)") // шаблон
        val list_chapter: MutableList<Chapter> = mutableListOf() // Список глав
        var position_chapter = 0 // текущая глава
        var position_page = 0 // текущая страница
        var list_page = mutableListOf<File>() // Список страниц
    }

    object ChapterObject { // группа для глав
        val position: Int // текущая глава
            get() = m.position_chapter + 1

        val max: Int // Общее количество глав
            get() = m.list_chapter.size

        val current: Chapter // Текущая глава (не позиция)
            get() = m.list_chapter[m.position_chapter]

        val next: Boolean // переключение на следующую главу
            get () {
                if (m.position_chapter < m.list_chapter.size) { // если глава не последняя
                    m.position_chapter++ // увеличить
                    if (!PageObject.updateList()) { // Обновить список страниц, если не получилось
                        m.position_chapter-- // уменьшить
                        PageObject.updateList() // Обновить список страниц
                        return false
                    }
                    return true
                }
                return false
            }

        val prev: Boolean // переключение на предыдущию главу
            get () {
                if (m.position_chapter > 0) { // если глава не первая
                    m.position_chapter-- // уменьшить
                    if (!PageObject.updateList()) { // Обновить список страниц, если не получилось
                        m.position_chapter++ // увеличить
                        PageObject.updateList() // Обновить список страниц
                        return false
                    }
                    return true
                }
                return false
            }
    }

    object PageObject { // группа для страниц
        var position: Int // текущая страница
            get() = m.position_page
            set(value) {
                m.position_page = value
                saveProgress(value) // Сохранить позицию в бд
            }

        val max: Int // Количество глав
            get() = m.list_page.size

        val list: List<File> // формирование списка страниц
            get() {
                val list = m.list_page // копируем имеющийся список
                if (ChapterObject.prev) { // Если есть главы до этой
                    list.add(0, File("prev")) // Добавить в начало специальный файл указатель
                    ChapterObject.next // вернуть главу обратно
                } else  // если нет
                    list.add(0, File("none")) // Добавить в начало другой файл указатель

                if (ChapterObject.next) { // Если есть главы после этой
                    list.add(File("next")) // Добавить в конец специальный файл указатель
                    ChapterObject.prev // вернуть главу обратно
                } else // если нет
                    list.add(File("none")) // Добавить в конец другой файл указатель

                return list // вернуть сформированный список
            }

        fun saveProgress(pos: Int = position) { // Сохранение позиции текущей главы
            var p = pos // скопировать позицию
//            log = "1, pos = $pos, max = $max"
            when {
                pos < 1 -> p = 1 // если меньше единицы значение, то приравнять к еденице
                pos == max -> { // если текущая позиция последняя
                    p = max
                    ChapterObject.current.updateStatus(true) // Сделать главу прочитанной
                }
                pos > max -> return // Если больше максимального значения, ничего не делать
            }
            ChapterObject.current.updateProgress(p) // Обновить позицию
        }

        fun updateList(): Boolean { // Обновить список страниц
            try { // пробуем сделать следущее
                m.list_page = getFullPath(ChapterObject.current.path) // Получить все файлы из папки
                        .listFiles { file, s ->
                            // фильтруем список
                            val fin = File(file, s) // получаем каждый файл отдельно
                            // если это файл и он является картинкой, то пропустить в список
                            fin.isFile && (fin.extension in extensions)
                        }.toMutableList()
            } catch (ex: Exception) { // если не получилось
                return false
            }
            try { // пробуем сделать следущее
                // Сортируем список, своим способом
                m.list_page.sortWith(Comparator { file1, file2 ->
                    // Сравниваем числовые значения у двух файлов
                    m.reg.find(file1.nameWithoutExtension)?.value?.toInt() as Int -
                            m.reg.find(file2.nameWithoutExtension)?.value?.toInt() as Int
                })
            } catch(ex: Exception) { // если не получилось
                m.list_page.sort() // используем стандартную сортировку
            }
            return true
        }


    }
}
