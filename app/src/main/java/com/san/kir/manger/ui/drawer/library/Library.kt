package com.san.kir.manger.ui.drawer.library

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.work.WorkManager
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.CategoryWithMangas
import com.san.kir.manger.ui.Drawer
import com.san.kir.manger.ui.drawer.CategoriesNavScreen
import com.san.kir.manger.view_models.TitleViewModel
import com.san.kir.manger.workmanager.MangaDeleteWorker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

var currentCategoryWithMangas = CategoryWithMangas()

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
@Composable
fun LibraryScreen(
    nav: NavController,
    mainNav: NavHostController,
    contentPadding: PaddingValues,
    viewModel: LibraryViewModel = hiltViewModel(),
    vm: TitleViewModel = hiltViewModel(mainNav.getBackStackEntry(Drawer.route))
) {
    vm.setTitle(stringResource(id = R.string.main_menu_library))

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val viewState by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding())
    ) {
        if (viewState.isAction) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

        if (viewState.categories.isNotEmpty()) {
            val pagerState = rememberPagerState(
                pageCount = viewState.categories.size
            )
            currentCategoryWithMangas = viewState.categories[pagerState.currentPage]

            vm.setTitle(
                stringResource(
                    id = R.string.main_menu_library_count,
                    currentCategoryWithMangas.mangas.size
                )
            )

            // Название вкладок
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(MaterialTheme.colors.primarySurface)) {
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
                    viewState.categories.forEachIndexed { index, item ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            text = { Text(text = item.category.name) },
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } }
                        )
                    }
                }
            }
            // Перелистываемые вкладки
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f, true),
            ) { index ->
                LibraryPage(index, viewState, nav, mainNav)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.align(Alignment.Center)) {
                    Text(text = stringResource(id = R.string.library_no_categories))
                    Button(onClick = { nav.navigate(CategoriesNavScreen.route) }) {
                        Text(text = stringResource(id = R.string.library_to_categories))
                    }
                }
            }
        }
    }

    LaunchedEffect(viewState) {
        WorkManager
            .getInstance(context)
            .getWorkInfosByTagLiveData(MangaDeleteWorker.tag)
            .asFlow()
            .collect { works ->
                if (works.isNotEmpty()) {
                    viewModel.setActionState(works.all { it.state.isFinished }.not())
                }
            }
    }
}


