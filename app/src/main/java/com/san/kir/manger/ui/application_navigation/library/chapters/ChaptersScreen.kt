package com.san.kir.manger.ui.application_navigation.library.chapters

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.imePadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.san.kir.ankofork.dialogs.longToast
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.services.MangaUpdaterService
import com.san.kir.manger.ui.LocalBaseViewModel
import com.san.kir.manger.ui.MainViewModel
import com.san.kir.manger.utils.extensions.longToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Composable
fun ChaptersScreen(nav: NavHostController, viewModel: ChaptersViewModel) {

    val selectionMode by viewModel.selectionMode.collectAsState()
    val manga by viewModel.manga.collectAsState()

    var action by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier,
        topBar = {
            if (selectionMode)
                SelectionModeChaptersTopBar(viewModel) { state -> action = state }
            else
                ChaptersTopBar(nav, viewModel) { state -> action = state }

        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    rememberInsetsPaddingValues(
                        insets = LocalWindowInsets.current.systemBars,
                        applyStart = true, applyEnd = true,
                        applyBottom = false, applyTop = false,
                        additionalTop = contentPadding.calculateTopPadding(),
                    )
                )
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            ChaptersContent(nav, action, viewModel)
        }
    }

    if (MangaUpdaterService.contains(manga))
        action = true

    ReceiverHandler(manga) { state -> action = state }
}

@Composable
private fun ReceiverHandler(
    manga: Manga,
    ctx: Context = LocalContext.current,
    vm: MainViewModel = LocalBaseViewModel.current,
    changeAction: (Boolean) -> Unit,
) {
    LaunchedEffect("receiver") {
        vm.chaptersReceiver.onEach { message ->
            if (manga.unic == message.mangaName) {
                if (message.countNew == -1) {
                    ctx.longToast(R.string.list_chapters_message_error)
                } else {
                    if (message.isFoundNew.not()) {
                        ctx.longToast(R.string.list_chapters_message_no_found)
                    } else {
                        ctx.longToast(R.string.list_chapters_message_count_new, message.countNew)
                    }
                }
                changeAction(false)
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class, ExperimentalCoroutinesApi::class)
@Composable
fun ColumnScope.ChaptersContent(
    nav: NavHostController,
    action: Boolean,
    viewModel: ChaptersViewModel,
    scope: CoroutineScope = rememberCoroutineScope(),
) {
    val chapters by viewModel.chapters.collectAsState(emptyList())
    val isTitle by viewModel.isTitle.collectAsState(true)
    val manga by viewModel.manga.collectAsState()

    val pagerState =
        if (manga.isAlternativeSite) rememberPagerState(pageCount = 1)
        else rememberPagerState(pageCount = 2)

    // ПрогрессБар для отображения фоновых операций
    if (action || chapters.isEmpty()) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

    if (isTitle)
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                )
            },
            modifier = Modifier
                .padding(
                    rememberInsetsPaddingValues(
                        insets = LocalWindowInsets.current.systemBars,
                        applyBottom = false, applyTop = false
                    )
                )
        ) {
            repeat(pagerState.pageCount) { index ->
                val item = CHAPTER_PAGES[index]
                Tab(
                    selected = pagerState.currentPage == index,
                    text = { Text(text = stringResource(id = item.nameId)) },
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } }
                )
            }
        }

    HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
        CHAPTER_PAGES[index].content(nav, viewModel)
    }

}

