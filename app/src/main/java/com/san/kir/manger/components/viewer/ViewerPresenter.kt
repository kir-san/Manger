package com.san.kir.manger.components.viewer

import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.Binder
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.utils.extensions.SpecialViewPager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewerPresenter(private val act: ViewerActivity) {

    private var adapter = ViewerAdapter(act.supportFragmentManager)
    private var manager = ChaptersList(act)// Менеджер глав и страниц

    val progressChapters = Binder(-1)
    val progressPages = Binder(0) // Текущая страница, которую в данный момент читают

    val max = Binder(0)
    var maxChapters = -1

    val isNext = Binder(true) // Есть ли следующая глава
    val isPrev = Binder(true) // Есть ли предыдущая глава

    val isBottomBar = Binder(false) // Отображение нижнего бара

    val isLoad = Binder(true)

    var isSwipeControl = Binder(true) // Свайпы

    fun into(viewPager: SpecialViewPager) {
        viewPager.adapter = adapter
    }

    fun configManager(chapter: Chapter, isAlternative: Boolean) = act.lifecycleScope.launch(Dispatchers.Default) {

        manager.init(chapter, isAlternative)

        withContext(Dispatchers.Main) {
            adapter.setList(manager.pagesList)
//            viewPager.currentItem = progressPages.item
        }

        maxChapters = manager.chaptersSize
        max.item = manager.pagesSize

        withContext(Dispatchers.Main) {
            act.mView.viewPager.currentItem =
                if (manager.pagePosition <= 0) 1 // Если полученная позиция не больше нуля, то присвоить значение 1
                else manager.pagePosition // Иначе то что есть
        }

        // При изменении прогресса, отдать новое значение в менеджер
        progressPages.bind { pos ->
            manager.pagePosition = pos
        }

        progressChapters.unicItem = manager.chapterPosition // Установка значения

        checkButton()


    }

    fun nextPage() {
        act.mView.viewPager.currentItem += 1
    }

    fun prevPage() {
        act.mView.viewPager.currentItem -= 1
    }

    // Предыдущая глава
    fun prevChapter() = act.lifecycleScope.launch(Dispatchers.Default) {
        manager.prevChapter() // Переключение главы
        initChapter()
    }

    // Следующая глава
    fun nextChapter() = act.lifecycleScope.launch(Dispatchers.Default) {
        manager.nextChapter() // Переключение главы
        initChapter()
    }

    private suspend fun initChapter() {
        max.unicItem = manager.pagesSize
        progressChapters.unicItem = manager.chapterPosition

        checkButton()

        withContext(Dispatchers.Main) {
            adapter.setList(manager.pagesList)
            act.chapter = manager.chapter() // Сохранение данных
            act.title = act.chapter.name // Смена заголовка
            act.mView.viewPager.currentItem = 1
        }
    }

    // Проверка видимости кнопок переключения глав
    private fun checkButton() {
        isPrev.item = manager.pagesList.first().link == "prev"
        isNext.item = manager.pagesList.last().link == "next"
    }

    fun invalidateFragmentMenus(position: Int) {
        adapter.items.forEachIndexed { index, fragment ->
            fragment.setHasOptionsMenu(index == position)
        }
        act.invalidateOptionsMenu()
    }
}
