package com.san.kir.manger.components.viewer

import com.san.kir.manger.eventBus.Binder
import com.san.kir.manger.extending.views.SpecialViewPager


class ViewerPresenter(private val act: ViewerActivity) {

    private var adapter = ViewerAdapter(act.supportFragmentManager)
    private lateinit var manager: ChaptersList // Менеджер глав и страниц
    private lateinit var viewPager: SpecialViewPager


    val progressChapters = Binder(-1)
    val progressPages = Binder(0) // Текущая страница, которую в данный момент читают
    val max = Binder(0)
    var maxChapters = -1

    val isNext = Binder(true) // Есть ли следующая глава
    val isPrev = Binder(true) // Есть ли предыдущая глава

    val isBottomBar = Binder(true) // Отображение нижнего бара

    var isSwipeControl = Binder(true) // Свайпы

    fun into(viewPager: SpecialViewPager) {
        viewPager.adapter = adapter
        this.viewPager = viewPager
    }

    fun configManager(mangaName: String, chapterName: String) {
        manager = ChaptersList(mangaName, chapterName)
        adapter.setList(manager.page.list)
        viewPager.currentItem = progressPages.item
        max.item = manager.page.max
        maxChapters = manager.chapter.max
        progressPages.item =
                if (manager.page.position <= 0) 1 // Если полученная позиция не больше нуля, то присвоить значение 1
                else manager.page.position // Иначе то что есть

        // При изменении прогресса, отдать новое значение в менеджер
        progressPages.bind { pos -> manager.page.position = pos }

        progressChapters.item = manager.chapter.position // Установка значения

        checkButton()
    }

    fun nextPage() {
        progressPages.item += 1
    }

    fun prevPage() {
        progressPages.item -= 1
    }

    // Предыдущая глава
    fun prevChapter() {
        manager.chapter.prev() // Переключение главы
        initChapter()
    }

    // Следующая глава
    fun nextChapter() {
        manager.chapter.next() // Переключение главы
        initChapter()
    }

    private fun initChapter() {
        adapter.setList(manager.page.list)
        viewPager.currentItem = progressPages.item
        progressPages.item = 1
        max.item = manager.page.max
        progressChapters.item = manager.chapter.position
        checkButton()
        act.chapterName = manager.chapter.current.name // Сохранение данных
        act.title = manager.chapter.current.name // Смена заголовка
    }

    // Проверка видимости кнопок переключения глав
    private fun checkButton() {
        isPrev.item = manager.page.list.first().name == "prev"
        isNext.item = manager.page.list.last().name == "next"
    }
}
