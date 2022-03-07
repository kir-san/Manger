package com.san.kir.chapters.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.window.DialogProperties
import com.san.kir.background.util.collectWorkInfoByTag
import com.san.kir.background.works.ChapterDeleteWorker
import com.san.kir.background.works.ReadChapterDelete
import com.san.kir.chapters.R
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.data.models.base.Manga

private fun dismissButton(onClick: () -> Unit) = @Composable {
    TextButton(onClick) {
        Text(
            stringResource(R.string.list_chapters_remove_no)
                .toUpperCase(Locale.current)
        )
    }
}

private fun confirmButton(onClick: () -> Unit, onClose: () -> Unit) = @Composable {
    TextButton(
        onClick = {
            onClick()
            onClose()
        }
    ) {
        Text(
            stringResource(R.string.list_chapters_remove_yes)
                .toUpperCase(Locale.current)
        )
    }
}

private fun text(idRes: Int?): @Composable (() -> Unit)? =
    if (idRes != null) {
        @Composable {
            Text(text = stringResource(id = idRes))
        }
    } else null

// Подготовленный шаблон диалога
@Composable
private fun PrepareAlertDialog(
    visible: Boolean,
    title: Int? = null,
    text: Int? = null,
    onClose: () -> Unit,
    onClick: () -> Unit,
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onClose,
            title = text(title),
            text = text(text),
            confirmButton = confirmButton(onClick, onClose),
            dismissButton = dismissButton(onClose)
        )
    }
}

// Диалог подтвержедения выделенных глав из БД
@Composable
internal fun FullDeleteChaptersAlertDialog(
    visible: Boolean,
    onClose: () -> Unit,
    onClick: () -> Unit,
) {
    PrepareAlertDialog(
        visible,
        title = R.string.action_full_delete_title,
        text = R.string.action_full_delete_message,
        onClose = onClose,
        onClick = onClick
    )
}

// Диалог подтверждения удаления выделенных глав
@Composable
internal fun DeleteSelectedChaptersAlertDialog(
    visible: Boolean,
    onClose: () -> Unit,
    onClick: () -> Unit,
) {
    PrepareAlertDialog(
        visible,
        title = R.string.list_chapters_remove_text,
        onClose = onClose,
        onClick = onClick
    )
}

// Диалог очистки памяти ото всех прочитанных глав
@Composable
internal fun DeleteChaptersDialog(
    visible: Boolean,
    onClose: () -> Unit,
    onClick: () -> Unit,
) {
    PrepareAlertDialog(
        visible,
        text = R.string.library_popupmenu_delete_read_chapters_message,
        onClose = onClose,
        onClick = onClick
    )
}

// Диалог оповещения о процессе очистки
@Composable
internal fun ProgressDeletingChaptersDialog(
    visible: Boolean,
    manga: Manga,
    onClose: () -> Unit,
) {
    if (visible) {
        val ctx = LocalContext.current
        // Индикатор выполнения операции
        var action by remember { mutableStateOf(true) }
        // Отображаемое сообщение в зависимости от статуса действия
        val message =
            if (action) R.string.library_popupmenu_delete_read_chapters_delete
            else R.string.library_popupmenu_delete_read_chapters_ready


        AlertDialog(
            onDismissRequest = onClose,
            properties = DialogProperties(
                // Откючение возможности отменить диалог
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
            buttons = {
                // Кнопка доступна после выполнения действия
                if (action.not())
                    TextButton(onClick = onClose) {
                        Text(stringResource(R.string.library_popupmenu_delete_read_chapters_btn_close))
                    }
            },
            text = {
                Row(
                    modifier = Modifier.padding(Dimensions.default),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (action) CircularProgressIndicator()
                    Text(
                        stringResource(message),
                        modifier = Modifier.padding(horizontal = Dimensions.small)
                    )
                }
            }
        )

        LaunchedEffect(true) {
            // Запуск работы по удалению глав
            ChapterDeleteWorker.addTask<ReadChapterDelete>(ctx, manga)

            // Подписка на выполняемую работу для изменения индикатора действия
            ctx.collectWorkInfoByTag(ChapterDeleteWorker.tag) { works ->
                // Индикатор убирается если нет задач ни одной задачи
                action = works.any { it.state.isFinished.not() }
            }
        }
    }
}
