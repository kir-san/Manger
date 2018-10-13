package com.san.kir.manger.components.viewer


import com.san.kir.manger.components.main.Main
import com.san.kir.manger.components.viewer.ChaptersList.Helper.chapterDao
import com.san.kir.manger.components.viewer.ChaptersList.Helper.positionStat
import com.san.kir.manger.room.dao.updateAsync
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.MangaStatistic
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.imageExtensions
import com.san.kir.manger.utils.isEmptyDirectory
import java.io.File
import java.util.*

// Свой класс для управления страницами и главами
class ChaptersList(
    mangaName: String,
    chapter: String
//    val act: ViewerActivity
) {
    var page = PageObject
    var chapter = ChapterObject

    init {
        Helper.list_chapter.clear() // Очистить список
        Helper.list_chapter.addAll(chapterDao.loadChapters(mangaName)) // Получение глав
        Helper.position_chapter = findChapterPosition(chapter) // Установка текущей главы
        PageObject.updateList() // Получение списка страниц для главы
        PageObject.position = when { // Установка текущей страницы
            ChapterObject.current.progress <= 0 -> 0 // Если не больше 0, то ноль
            else -> ChapterObject.current.progress // Иначе как есть
        }
        positionStat = PageObject.position
        Helper.stats = Helper.statisticDao.loadItem(mangaName)
        Helper.stats.lastChapters = 0
        Helper.stats.lastPages = 0
        Helper.statisticDao.updateAsync(Helper.stats)
    }

    private fun findChapterPosition(chapter: String): Int { // Позиции главы, по названию главы
        val lastIndex = Helper.list_chapter.size - 1 // Последняя позиция
        // Проверка всех названий глав на соответствие, если ничего нет, то позиция равна 0
        return (0..lastIndex).firstOrNull { Helper.list_chapter[it].name == chapter } ?: 0
    }

    object Helper { // маленький объект
        val chapterDao = Main.db.chapterDao
        val statisticDao = Main.db.statisticDao
        val reg = Regex("\\d+") // шаблон
        val list_chapter: MutableList<Chapter> = mutableListOf() // Список глав
        var position_chapter = 0 // текущая глава
        var position_page = 0 // текущая страница
        var stats = MangaStatistic()
        var list_page = mutableListOf<File>() // Список страниц
        var positionStat = 0
    }

    object ChapterObject { // группа для глав
        val position: Int // текущая глава
            get() = Helper.position_chapter + 1

        val max: Int // Общее количество глав
            get() = Helper.list_chapter.size

        val current: Chapter // Текущая глава
            get() = Helper.list_chapter[Helper.position_chapter]

        fun next() { // переключение на следующую главу
            if (hasNext()) {
                Helper.position_chapter++
                PageObject.updateList()
                Helper.stats.lastChapters++
                Helper.stats.allChapters++
                Helper.statisticDao.updateAsync(Helper.stats)
            }
        }

        fun hasNext(): Boolean {
            var hasNext = false
            if (Helper.position_chapter < Helper.list_chapter.size - 1) {
                Helper.position_chapter++
                hasNext = PageObject.hasUpdateList()
                Helper.position_chapter--
            }
            return hasNext
        }

        fun prev() { // переключение на предыдущию главу
            if (hasPrev()) {
                Helper.position_chapter--
                PageObject.updateList()
            }
        }

        fun hasPrev(): Boolean {
            var hasPrev = false
            if (Helper.position_chapter > 0) {
                Helper.position_chapter--
                hasPrev = PageObject.hasUpdateList()
                Helper.position_chapter++
            }
            return hasPrev
        }
    }

    object PageObject { // группа для страниц
        var position: Int // текущая страница
            get() = Helper.position_page
            set(value) {
                Helper.position_page = value
                saveProgress(value) // Сохранить позицию в бд
            }

        val max: Int // Количество глав
            get() = Helper.list_page.size

        var list: List<File> = listOf()
            private set

        private fun saveProgress(pos: Int = position) { // Сохранение позиции текущей главы
            var p = pos // скопировать позицию
            when {
                pos < 1 -> p = 1 // если меньше единицы значение, то приравнять к еденице
                pos == max -> { // если текущая позиция последняя
                    p = max
                    // Сделать главу прочитанной
                    ChapterObject.current.isRead = true
                    Helper.chapterDao.updateAsync(ChapterObject.current)
                }
                pos > max -> return // Если больше максимального значения, ничего не делать
            }
            // Обновить позицию
            ChapterObject.current.progress = p
            Helper.chapterDao.updateAsync(ChapterObject.current)

            if (pos > positionStat) {
                val diff = pos - positionStat
                Helper.stats.lastPages += diff
                Helper.stats.allPages += diff
                Helper.statisticDao.updateAsync(Helper.stats)
                positionStat = pos
            }
        }

        fun hasUpdateList(): Boolean {
            val fullPath = getFullPath(ChapterObject.current.path)

            if (fullPath.isEmptyDirectory) {
                return false
            }
            return fullPath // Получить все файлы из папки
                .listFiles { file, s ->
                    // фильтруем список
                    val fin = File(file, s) // получаем каждый файл отдельно
                    // если это файл и он является картинкой, то пропустить в список
                    fin.isFile && (fin.extension in imageExtensions)
                }.isNotEmpty()
        }

        fun updateList() { // Обновить список страниц
            Helper.list_page =
                    getFullPath(ChapterObject.current.path) // Получить все файлы из папки
                        .listFiles { file, s ->
                            // фильтруем список
                            val fin = File(file, s) // получаем каждый файл отдельно
                            // если это файл и он является картинкой, то пропустить в список
                            fin.isFile && (fin.extension in imageExtensions)
                        }.toMutableList()
            try {
                // Сортируем список, своим способом
                Helper.list_page.sortWith(Comparator { file1, file2 ->
                    // Сравниваем числовые значения у двух файлов
                    val find1: MatchResult? =
                        Helper.reg.find(file1.nameWithoutExtension)
                    val find2 = Helper.reg.find(file2.nameWithoutExtension)
                    if (find1 == null || find2 == null) {
                        return@Comparator 1_000
                    }
                    find1.value.toInt() - find2.value.toInt()
                })
            } catch (ex: Exception) {
                ex.printStackTrace()
                Helper.list_page.sort() // используем стандартную сортировку
            }

            val tempList = Helper.list_page.toMutableList() // копируем имеющийся список
            if (ChapterObject.hasPrev()) { // Если есть главы до этой
                tempList.add(0, File("prev")) // Добавить в начало специальный файл указатель
            } else  // если нет
                tempList.add(0, File("none")) // Добавить в начало другой файл указатель

            if (ChapterObject.hasNext()) { // Если есть главы после этой
                tempList.add(File("next")) // Добавить в конец специальный файл указатель
            } else // если нет
                tempList.add(File("none")) // Добавить в конец другой файл указатель

            positionStat = 1

            list = tempList
        }
    }
}
