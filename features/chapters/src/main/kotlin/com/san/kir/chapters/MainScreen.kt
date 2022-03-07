package com.san.kir.chapters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.san.kir.background.services.MangaUpdaterService
import com.san.kir.core.compose_utils.TopBarScreenPadding
import com.san.kir.data.models.base.Manga
import com.san.kir.core.utils.longToast

@Composable
fun ChaptersScreen(
    viewModel: MainViewModel,
    mangaUnic: String,
    navigateUp: () -> Unit,
) {
    // Инициация данных во vm
    viewModel.setMangaUnic(mangaUnic)

    val selectionMode by viewModel.selection.isEnable.collectAsState()
    val manga by viewModel.manga.collectAsState()

    // Индикатор выполнения каких-либо действий
    var (action, actionSetter) = rememberSaveable { mutableStateOf(false) }

    TopBarScreenPadding(
        topBar = {
            // В зависимости от состояния активации меняется AppBar
            if (selectionMode) {
                SelectionModeTopBar(viewModel, actionSetter)
            } else {
                DefaultTopBar(navigateUp, viewModel, actionSetter)
            }
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
        ) {
            Content(action, viewModel)
        }
    }


    if (MangaUpdaterService.contains(manga))
        action = true

    ReceiverHandler(manga, changeAction = actionSetter)
}

// Подписка на сообщения от сервиса MangaUpdateService
@Composable
private fun ReceiverHandler(
    manga: Manga,
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
                        // Реагирование только если текущее название соотвествует полученному
                        if (mangaName != null && mangaName == currentManga.name) {
                            // Получаем результаты работы
                            // Были ли найдены новые главы TODO избавиться от лишнего флага в сервисе
                            val isFoundNew =
                                intent.getBooleanExtra(MangaUpdaterService.IS_FOUND_NEW, false)
                            // Сколько найдено
                            val countNew = intent.getIntExtra(MangaUpdaterService.COUNT_NEW, 0)

                            // Отображение сообщения в зависимости от результата
                            if (countNew == -1) {
                                context?.longToast(R.string.list_chapters_message_error)
                            } else {
                                if (isFoundNew.not()) {
                                    context?.longToast(R.string.list_chapters_message_no_found)
                                } else {
                                    context?.longToast(R.string.list_chapters_message_count_new,
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

