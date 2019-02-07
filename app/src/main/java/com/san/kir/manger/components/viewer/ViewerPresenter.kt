package com.san.kir.manger.components.viewer

import com.san.kir.manger.eventBus.Binder
import com.san.kir.manger.extending.launchCtx
import com.san.kir.manger.extending.views.SpecialViewPager
import com.san.kir.manger.room.models.Chapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ViewerPresenter(private val act: ViewerActivity) {

    private var adapter = ViewerAdapter(act.supportFragmentManager)
    private var manager = ChaptersList(act)// Менеджер глав и страниц
    private lateinit var viewPager: SpecialViewPager

    val progressChapters = Binder(-1)
    val progressPages = Binder(0) // Текущая страница, которую в данный момент читают
    val max = Binder(0)
    var maxChapters = -1

    val isNext = Binder(true) // Есть ли следующая глава
    val isPrev = Binder(true) // Есть ли предыдущая глава

    val isBottomBar = Binder(true) // Отображение нижнего бара

    val isLoad = Binder(true)

    var isSwipeControl = Binder(true) // Свайпы

    fun into(viewPager: SpecialViewPager) {
        viewPager.adapter = adapter
        this.viewPager = viewPager
    }

    fun configManager(
        chapter: Chapter,
        isAlternative: Boolean
    ) = act.launchCtx {

        manager.init(chapter, isAlternative)

        maxChapters = manager.chaptersSize
        max.item = manager.pagesSize
        progressPages.unicItem =
            if (manager.pagePosition <= 0) 1 // Если полученная позиция не больше нуля, то присвоить значение 1
            else manager.pagePosition // Иначе то что есть

        // При изменении прогресса, отдать новое значение в менеджер
        progressPages.bind { pos -> manager.pagePosition = pos }

        progressChapters.unicItem = manager.chapterPosition // Установка значения

        checkButton()

        withContext(Dispatchers.Main) {
            adapter.setList(manager.pagesList)
            viewPager.currentItem = progressPages.item
        }
    }

    fun nextPage() {
        progressPages.unicItem += 1
    }

    fun prevPage() {
        progressPages.unicItem -= 1
    }

    // Предыдущая глава
    fun prevChapter() = act.launch {
        manager.prevChapter() // Переключение главы
        initChapter()
    }

    // Следующая глава
    fun nextChapter() = act.launch(act.coroutineContext) {
        manager.nextChapter() // Переключение главы
        initChapter()
    }

    private suspend fun initChapter() {
        progressPages.unicItem = 1
        max.unicItem = manager.pagesSize
        progressChapters.unicItem = manager.chapterPosition

        checkButton()

        withContext(Dispatchers.Main) {
            adapter.setList(manager.pagesList)
            viewPager.currentItem = progressPages.item
            act.chapter = manager.chapter() // Сохранение данных
            act.title = act.chapter.name // Смена заголовка
        }
    }

    // Проверка видимости кнопок переключения глав
    private fun checkButton() {
        isPrev.item = manager.pagesList.first().link == "prev"
        isNext.item = manager.pagesList.last().link == "next"
    }
}
