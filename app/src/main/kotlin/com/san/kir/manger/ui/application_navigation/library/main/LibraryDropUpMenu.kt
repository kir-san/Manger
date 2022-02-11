package com.san.kir.manger.ui.application_navigation.library.main

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MenuDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.insets.systemBarsPadding
import com.san.kir.manger.R
import com.san.kir.manger.ui.application_navigation.library.LibraryNavTarget
import com.san.kir.core.compose_utils.MenuText
import com.san.kir.manger.utils.compose.navigate
import com.san.kir.manger.foreground_work.workmanager.MangaDeleteWorker

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LibraryDropUpMenu(nav: NavHostController, viewModel: LibraryViewModel) {
    var deleteDialog by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .systemBarsPadding(top = false)
    ) {

        DropdownMenuItem(onClick = {
            viewModel.changeSelectedManga(false)
        }, modifier = Modifier.background(color = MaterialTheme.colors.primary)) {
            Text(
                stringResource(
                    R.string.library_popupmenu_title,
                    viewModel.selectedManga.manga.name
                ),
                maxLines = 1, fontWeight = FontWeight.Bold
            )
        }

        MenuText(id = R.string.library_popupmenu_about, onClick = {
            viewModel.changeSelectedManga(false)
            nav.navigate(LibraryNavTarget.About, viewModel.selectedManga.manga.name)
        })

        DropdownMenuItem(onClick = {
            expandedCategory = !expandedCategory
            deleteDialog = false
        }) {
            Text(
                stringResource(id = R.string.library_popupmenu_set_category),
                modifier = Modifier.weight(1f)
            )

            val categoryName by viewModel.selectedMangaCategory.collectAsState()
            Text(
                text = categoryName,
                style = MaterialTheme.typography.subtitle2
            )
        }

        val categories by viewModel.categories.collectAsState(emptyMap())

        ExpandedCategories(
            expandedCategory,
            categories - viewModel.selectedManga.manga.categoryId
        ) { categoryId ->
            viewModel.updateCategory(categoryId)
            expandedCategory = false
            viewModel.changeSelectedManga(false)
        }

        MenuText(id = R.string.library_popupmenu_storage, onClick = {
            viewModel.changeSelectedManga(false)
            nav.navigate(LibraryNavTarget.Storage, viewModel.selectedManga.manga.name)
        })

        MenuText(id = R.string.library_popupmenu_statistic, onClick = {
            viewModel.changeSelectedManga(false)
            nav.navigate(LibraryNavTarget.Statistic, viewModel.selectedManga.manga.name)
        })

        MenuText(id = R.string.library_popupmenu_delete, onClick = {
            deleteDialog = !deleteDialog
            expandedCategory = false
        })

        CustomAnimatedItem(deleteDialog) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(minHeight = 48.dp)
                    .padding(MenuDefaults.DropdownMenuItemContentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.library_popupmenu_delete_message),
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                val pad = PaddingValues(4.dp, 4.dp)
                val context = LocalContext.current

                OutlinedButton(onClick = {
                    deleteDialog = false
                    viewModel.changeSelectedManga(false)
                    MangaDeleteWorker.addTask(context, viewModel.selectedManga.manga.id, true)
                }, modifier = Modifier.padding(pad)) {
                    Text(text = stringResource(id = R.string.library_popupmenu_delete_ok_with_files))
                }

                OutlinedButton(onClick = {
                    deleteDialog = false
                    viewModel.changeSelectedManga(false)
                    MangaDeleteWorker.addTask(context, viewModel.selectedManga.manga.id)
                }, modifier = Modifier.padding(pad)) {
                    Text(text = stringResource(id = R.string.library_popupmenu_delete_ok))
                }

                OutlinedButton(
                    onClick = { deleteDialog = false },
                    modifier = Modifier.padding(pad)
                ) {
                    Text(text = stringResource(id = R.string.library_popupmenu_delete_no))
                }
            }
        }
    }
}

@Composable
private fun ExpandedCategories(
    visibility: Boolean,
    categories: Map<Long, String>,
    onItemChanged: (Long) -> Unit,
) {
    CustomAnimatedItem(visibility) {
        categories.forEach { (key, value) -> MenuText(value) { onItemChanged(key) } }
    }
}

@Composable
private fun CustomAnimatedItem(
    visibility: Boolean,
    content: @Composable ColumnScope.() -> Unit,
) {
    CustomAnimated(visibility) { state ->
        val transformOriginState = remember { mutableStateOf(TransformOrigin(1f, 1f)) }
        // Menu open/close animation.
        val transition = updateTransition(state, "expandedItem")

        val scale by transition.scaleAnimate()
        val alpha by transition.alphaAnimate()

        Surface(
            modifier = Modifier.graphicsLayer {
                this.alpha = alpha
                scaleY = scale
                transformOrigin = transformOriginState.value
            },
            color = MaterialTheme.colors.primarySurface,
            elevation = AppBarDefaults.TopAppBarElevation + 10.dp,
        ) {
            Column {
                content()
            }
        }
    }
}

private const val transitionDuration = 180

@Composable
private fun Transition<Boolean>.alphaAnimate(): State<Float> {
    return animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                tween(
                    durationMillis = transitionDuration,
                    easing = FastOutSlowInEasing
                )
            } else {
                // Expanded to dismissed.
                tween(
                    durationMillis = transitionDuration,
                )
            }
        }, label = ""
    ) {
        if (it) {
            // Menu is expanded.
            1f
        } else {
            // Menu is dismissed.
            0f
        }
    }
}

@Composable
private fun Transition<Boolean>.scaleAnimate(): State<Float> {
    return animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                tween(
                    durationMillis = transitionDuration,
                    easing = FastOutSlowInEasing
                )
            } else {
                // Expanded to dismissed.
                tween(
                    durationMillis = transitionDuration,
                )
            }
        }, label = ""
    ) {
        if (it) {
            // Menu is expanded.
            1f
        } else {
            // Menu is dismissed.
            0f
        }
    }
}

@Composable
fun CustomAnimated(
    targetState: Boolean,
    content: @Composable (MutableTransitionState<Boolean>) -> Unit,
) {
    val expandedStates = remember { MutableTransitionState(false) }
    expandedStates.targetState = targetState

    if (expandedStates.currentState || expandedStates.targetState) {
        content(expandedStates)
    }
}
