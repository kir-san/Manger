package com.san.kir.manger.ui.application_navigation.library.chapters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsPadding
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.utils.enums.ChapterFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalAnimationApi::class, ExperimentalCoroutinesApi::class)
@Composable
fun ListPageContent(
    manga: Manga,
    chapterFilter: ChapterFilter,
    changeChapterFilter: (ChapterFilter) -> Unit,
    chapters: List<Chapter>,
    selectedItems: List<Boolean>,
    selectionMode: Boolean,
    selectItem: (Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {

        Box(modifier = Modifier.weight(1f)) {
            if (chapters.isNotEmpty() && selectedItems.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(
                        items = chapters,
                        key = { _, ch -> ch.id },
                    ) { index, chapter ->
                        ChaptersItemContent(
                            manga,
                            chapter,
                            selectedItems[index],
                            selectionMode,
                            { selectItem(index) }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(selectionMode.not()) {
            BottomOrderBar(chapterFilter, changeChapterFilter)
        }
    }
}

@Preview
@Composable
fun PreviewListPageContent() {
    val (filter, filterSetter) = remember {
        mutableStateOf(ChapterFilter.ALL_READ_ASC)
    }

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

@Composable
private fun BottomOrderBar(
    currentFilter: ChapterFilter,
    changeFilter: (ChapterFilter) -> Unit,
) {
    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(start = false, end = false)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Смена порядка сортировки

        IconButton(onClick = { changeFilter(currentFilter.inverse()) }) {
            Icon(
                Icons.Default.Sort, contentDescription = "reverse sort",
                modifier = Modifier.rotate(
                    animateFloatAsState(if (currentFilter.isAsc) 0f else 180f).value
                )
            )
        }


        Spacer(modifier = Modifier.weight(1f))

        // Кнопка включения отображения всех глав
        IconButton(
            onClick = { changeFilter(currentFilter.toAll()) },
            modifier = Modifier.padding(horizontal = 5.dp)
        ) {
            Icon(
                Icons.Default.SelectAll, contentDescription = null,
                tint = animatedColor(currentFilter.isAll)
            )
        }

        // Кнопка включения отображения только прочитанных глав
        IconButton(
            onClick = { changeFilter(currentFilter.toRead()) },
            modifier = Modifier.padding(horizontal = 5.dp)
        ) {
            Icon(
                Icons.Default.Visibility, contentDescription = null,
                tint = animatedColor(currentFilter.isRead)
            )
        }


        // Кнопка включения отображения только не прочитанных глав
        IconButton(
            onClick = { changeFilter(currentFilter.toNot()) },
            modifier = Modifier.padding(horizontal = 5.dp)
        ) {
            Icon(
                Icons.Default.VisibilityOff, contentDescription = null,
                tint = animatedColor(currentFilter.isNot)
            )
        }

        Spacer(modifier = Modifier.weight(1f))
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

@Composable
private fun animatedColor(state: Boolean): Color {
    val defaultIconColor = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
    val selectedIconColor = Color(0xff36a0da)
    return animateColorAsState(targetValue = if (state) selectedIconColor else defaultIconColor)
        .value
}
