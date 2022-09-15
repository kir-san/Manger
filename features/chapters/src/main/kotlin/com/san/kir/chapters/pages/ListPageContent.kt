package com.san.kir.chapters.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.FullWeightSpacer
import com.san.kir.core.support.ChapterFilter
import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.countPages
import kotlinx.coroutines.ExperimentalCoroutinesApi

// Страница со списком и инструментами для манипуляции с ним
@Composable
internal fun ListPageContent(
    manga: Manga,
    chapterFilter: ChapterFilter,
    changeChapterFilter: (ChapterFilter) -> Unit,
    chapters: List<Chapter>,
    selectedItems: List<Boolean>,
    selectionMode: Boolean,
    selectItem: (Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {

        // Обертка для корректного отображения элементов если список пустой
        Box(modifier = Modifier.weight(1f)) {

            // Список отображается только если он не пустой
            if (chapters.isNotEmpty() && selectedItems.isNotEmpty()) {

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(
                        items = chapters,
                        key = { _, ch -> ch.id },
                    ) { index, chapter ->
                        // Количество страниц в локальной памяти
                        val countPagesInMemory by produceState(0, chapter, manga) {
                            withDefaultContext {
                                value = chapter.countPages
                            }
                        }

                        ItemContent(
                            manga = manga,
                            chapter = chapter,
                            isSelected = selectedItems[index],
                            selectionMode = selectionMode,
                            onSelectItem = { selectItem(index) },
                            localCountPages = countPagesInMemory,
                        )
                    }
                }
            }
        }

        // Нижний бар, скрывается если включен режим выделения
        AnimatedVisibility(selectionMode.not()) {
            BottomOrderBar(chapterFilter, changeChapterFilter)
        }
    }
}

@Preview
@Composable
fun PreviewListPageContent() {
    val (filter, filterSetter) = remember { mutableStateOf(ChapterFilter.ALL_READ_ASC) }

    val chapters = listOf(
        Chapter(id = 1L, name = "First chapter Item"),
        Chapter(id = 2L, name = "Second chapter Item"),
        Chapter(id = 3L, name = "Third chapter Item"),
    )

    val selectedItems = chapters.map { false }

    val selectionMode = false

    MaterialTheme {
        ListPageContent(manga = Manga(),
                        chapterFilter = filter,
                        changeChapterFilter = filterSetter,
                        chapters = chapters,
                        selectedItems = selectedItems,
                        selectionMode = selectionMode,
                        selectItem = {})
    }
}

// Нижний бар управления сортировкой и фильтрацией списка
@Composable
private fun BottomOrderBar(
    currentFilter: ChapterFilter,
    changeFilter: (ChapterFilter) -> Unit,
) {
    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Vertical))
    ) {
        FullWeightSpacer()

        // Смена порядка сортировки
        IconButton(onClick = { changeFilter(currentFilter.inverse()) }) {
            Icon(
                Icons.Default.Sort,
                contentDescription = "reverse sort",
                modifier = Modifier.rotate(
                    animateFloatAsState(if (currentFilter.isAsc) 0f else 180f).value
                )
            )
        }

        FullWeightSpacer()

        // Кнопка включения отображения всех глав
        IconButton(
            onClick = { changeFilter(currentFilter.toAll()) },
            modifier = Modifier.padding(horizontal = Dimensions.small)
        ) {
            Icon(
                Icons.Default.SelectAll,
                contentDescription = null,
                tint = animatedColor(currentFilter.isAll)
            )
        }

        // Кнопка включения отображения только прочитанных глав
        IconButton(
            onClick = { changeFilter(currentFilter.toRead()) },
            modifier = Modifier.padding(horizontal = Dimensions.small)
        ) {
            Icon(
                Icons.Default.Visibility,
                contentDescription = null,
                tint = animatedColor(currentFilter.isRead)
            )
        }


        // Кнопка включения отображения только не прочитанных глав
        IconButton(
            onClick = { changeFilter(currentFilter.toNot()) },
            modifier = Modifier.padding(horizontal = Dimensions.small)
        ) {
            Icon(
                Icons.Default.VisibilityOff,
                contentDescription = null,
                tint = animatedColor(currentFilter.isNot)
            )
        }

        FullWeightSpacer()
    }
}

@Preview
@Composable
fun PreviewBottomOrderBar() {
    val (filter, filterSetter) = remember {
        mutableStateOf(ChapterFilter.ALL_READ_ASC)
    }
    BottomOrderBar(filter, filterSetter)
}

// Анимированая цветовая индикация нажатой кнопки
@Composable
private fun animatedColor(state: Boolean): Color {
    val defaultIconColor = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
    val selectedIconColor = Color(0xff36a0da)
    return animateColorAsState(targetValue = if (state) selectedIconColor else defaultIconColor)
        .value
}
