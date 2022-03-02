package com.san.kir.manger.ui.application_navigation.library.chapters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.imePadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.san.kir.core.compose_utils.TopBarScreenPadding
import com.san.kir.data.models.base.Manga
import com.san.kir.manger.R
import com.san.kir.manger.foreground_work.services.MangaUpdaterService
import com.san.kir.core.utils.longToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@Composable
fun ChaptersScreen(viewModel: ChaptersViewModel, navigateUp: () -> Unit) {

    var (action, actionSetter) = rememberSaveable { mutableStateOf(false) }

    TopBarScreenPadding(
        topBar = {
            if (viewModel.selectionMode)
                SelectionModeChaptersTopBar(viewModel, actionSetter)
            else
                ChaptersTopBar(navigateUp, viewModel, actionSetter)
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
        ) {
            ChaptersContent(action, viewModel)
        }
    }

    if (MangaUpdaterService.contains(viewModel.manga))
        action = true

    ReceiverHandler(viewModel.manga) { state -> action = state }
}

@Composable
private fun ReceiverHandler(
    manga: Manga,
    ctx: Context = LocalContext.current,
    changeAction: (Boolean) -> Unit,
) {
    val context = LocalContext.current

    val currentOnSystemEvent by rememberUpdatedState(changeAction)
    val currentManga by rememberUpdatedState(manga)

    DisposableEffect(context, changeAction) {

        val intentFilter = IntentFilter(MangaUpdaterService.actionGet)

        val broadcast = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    val mangaName = intent.getStringExtra(MangaUpdaterService.ITEM_NAME)
                    if (intent.action == MangaUpdaterService.actionGet) {
                        if (mangaName != null && mangaName == currentManga.name) {
                            val isFoundNew =
                                intent.getBooleanExtra(MangaUpdaterService.IS_FOUND_NEW, false)
                            val countNew = intent.getIntExtra(MangaUpdaterService.COUNT_NEW, 0)
                            if (countNew == -1) {
                                ctx.longToast(R.string.list_chapters_message_error)
                            } else {
                                if (isFoundNew.not()) {
                                    ctx.longToast(R.string.list_chapters_message_no_found)
                                } else {
                                    ctx.longToast(R.string.list_chapters_message_count_new,
                                        countNew)
                                }
                            }
                        }
                        currentOnSystemEvent(false)
                    }
                }
            }
        }

        context.registerReceiver(broadcast, intentFilter)

        // When the effect leaves the Composition, remove the callback
        onDispose {
            context.unregisterReceiver(broadcast)
        }
    }
}

@OptIn(ExperimentalPagerApi::class, ExperimentalCoroutinesApi::class)
@Composable
fun ColumnScope.ChaptersContent(
    action: Boolean,
    viewModel: ChaptersViewModel,
    scope: CoroutineScope = rememberCoroutineScope(),
) {
    val isTitle by viewModel.isTitle.collectAsState(true)

    val pagerState = rememberPagerState()
    val pages = chapterPages(viewModel.manga.isAlternativeSite)

    // ПрогрессБар для отображения фоновых операций
    if (action || viewModel.chapters.isEmpty())
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

    if (isTitle)
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                )
            },
            modifier = Modifier.systemBarsPadding(bottom = false, top = false)
        ) {
            pages.forEachIndexed { index, item ->
                Tab(
                    selected = pagerState.currentPage == index,
                    text = { Text(text = stringResource(id = item.nameId)) },
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } }
                )
            }
        }

    HorizontalPager(
        count = pages.size,
        state = pagerState,
        modifier = Modifier.weight(1f)
    ) { index ->
        pages[index].content(viewModel)
    }

}

