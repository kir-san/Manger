package com.san.kir.schedule.ui.task

import android.app.TimePickerDialog
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.LabelText
import com.san.kir.core.compose.MultiChoiceList
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.RadioGroup
import com.san.kir.core.compose.ScreenContent
import com.san.kir.core.compose.SingleChoiceList
import com.san.kir.core.compose.animation.FromEndToEndAnimContent
import com.san.kir.core.compose.animation.FromStartToStartAnimContent
import com.san.kir.core.compose.animation.FromTopToTopAnimContent
import com.san.kir.core.compose.animation.TopAnimatedVisibility
import com.san.kir.core.compose.horizontalInsetsPadding
import com.san.kir.core.compose.topBar
import com.san.kir.core.support.PlannedPeriod
import com.san.kir.core.support.PlannedType
import com.san.kir.core.support.PlannedWeek
import com.san.kir.schedule.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import java.util.Locale

@Composable
fun TaskScreen(
    navigateUp: () -> Unit,
    itemId: Long,
) {
    val viewModel: TaskViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.sendEvent(TaskEvent.Set(itemId)) }

    val sendEvent = remember { { event: TaskEvent -> viewModel.sendEvent(event) } }

    ScreenContent(
        topBar = topBar(
            navigationButton = NavigationButton.Back(navigateUp),
            title = if (itemId == -1L)
                stringResource(R.string.planned_task_title_create)
            else
                stringResource(R.string.planned_task_title_change),
            actions = {
                FromEndToEndAnimContent(targetState = state.hasChanges) {
                    if (it)
                        MenuIcon(Icons.Default.Save) { sendEvent(TaskEvent.Save) }
                }
            }
        )
    ) {
        Column(
            modifier = Modifier
                .horizontalInsetsPadding()
                .fillMaxWidth()
        ) {
            Content(state) { sendEvent(TaskEvent.Change(it)) }
        }
    }
}

@Composable
private fun ColumnScope.Content(state: TaskState, sendEvent: (ChangeType) -> Unit) {
    TypeChanger(type = state.item.type, sendEvent = sendEvent)

    Divider(modifier = Modifier.padding(vertical = Dimensions.half))

    TypeConfig(state, sendEvent)

    Divider(modifier = Modifier.padding(vertical = Dimensions.half))

    PeriodChanger(period = state.item.period, sendEvent = sendEvent)

    Divider(modifier = Modifier.padding(vertical = Dimensions.half))

    TopAnimatedVisibility(visible = state.item.period == PlannedPeriod.WEEK) {
        PeriodConfig(dayOfWeek = state.item.dayOfWeek, sendEvent = sendEvent)
    }

    TimeConfig(hour = state.item.hour, minute = state.item.minute, sendEvent = sendEvent)
}

@Composable
private fun TypeChanger(type: PlannedType, sendEvent: (ChangeType) -> Unit) {
    LabelText(idRes = R.string.planned_task_type_of_update)

    RadioGroup(
        state = type,
        onSelected = { sendEvent(ChangeType.Type(it)) },
        stateList = TaskState.types,
        textList = TaskState.types.map { stringResource(it.text) }.toImmutableList()
    )
}

@Composable
private fun TypeConfig(state: TaskState, sendEvent: (ChangeType) -> Unit) {
    FromTopToTopAnimContent(targetState = state.item.type) { currentState ->
        when (currentState) {
            PlannedType.MANGA ->
                TypedItem(
                    label = R.string.planned_task_change_manga,
                    value = state.mangaName,
                    nothingValue = R.string.planned_task_manga_unknown,
                ) { dismiss ->
                    SingleChoiceList(
                        initialValue = state.item.mangaId,
                        stateList = state.mangaIds,
                        textList = state.mangaNames,
                        onDismiss = dismiss
                    ) { sendEvent(ChangeType.Manga(it)) }
                }

            PlannedType.GROUP ->
                TypedItemList(
                    label = R.string.planned_task_name_of_group,
                    value = state.item.groupName,
                    onValueChange = { sendEvent(ChangeType.Group(it)) },
                    label2 = R.string.planned_task_option_of_group,
                    items = state.groupNames
                ) { dismiss ->
                    MultiChoiceList(
                        items = state.item.mangas.toPersistentList(),
                        textList = state.mangaNames,
                        stateList = state.mangaIds,
                        onDismiss = dismiss,
                    ) { sendEvent(ChangeType.Mangas(it)) }
                }

            PlannedType.CATEGORY ->
                TypedItem(
                    label = R.string.planned_task_change_category,
                    value = state.categoryName,
                    nothingValue = R.string.planned_task_category_unknown,
                ) { dismiss ->
                    SingleChoiceList(
                        initialValue = state.item.categoryId,
                        stateList = state.categoryIds,
                        textList = state.categoryNames,
                        onDismiss = dismiss
                    ) { sendEvent(ChangeType.Category(it)) }
                }

            PlannedType.CATALOG ->
                TypedItem(
                    label = R.string.planned_task_change_catalog,
                    value = state.item.catalog,
                    nothingValue = R.string.planned_task_catalog_unknown,
                ) { dismiss ->
                    SingleChoiceList(
                        initialValue = state.item.catalog,
                        stateList = state.catalogNames,
                        textList = state.catalogNames,
                        onDismiss = dismiss,
                        onSelect = { sendEvent(ChangeType.Catalog(it)) }
                    )
                }

            else -> {}
        }
    }
}

@Composable
private fun PeriodChanger(period: PlannedPeriod, sendEvent: (ChangeType) -> Unit) {
    LabelText(R.string.planned_task_repeat)

    RadioGroup(
        state = period,
        onSelected = { sendEvent(ChangeType.Period(it)) },
        stateList = TaskState.periods,
        textList = TaskState.periods.map { stringResource(it.text) }.toImmutableList()
    )
}

@Composable
private fun PeriodConfig(dayOfWeek: PlannedWeek, sendEvent: (ChangeType) -> Unit) {
    Column {
        LabelText(R.string.planned_task_change_day)

        RadioGroup(
            state = dayOfWeek,
            onSelected = { sendEvent(ChangeType.Day(it)) },
            stateList = TaskState.weeks,
            textList = TaskState.weeks.map { stringResource(it.text) }.toImmutableList()
        )

        Divider(modifier = Modifier.padding(vertical = Dimensions.half))
    }
}

@Composable
private fun TimeConfig(hour: Int, minute: Int, sendEvent: (ChangeType) -> Unit) {
    LabelText(R.string.planned_task_change_time)

    val activity = LocalContext.current as ComponentActivity
    TextButton(onClick = {
        showTimePicker(activity, hour, minute) { hour, minute ->
            sendEvent(ChangeType.Time(hour, minute))
        }
    }) {
        Text(
            "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}",
            style = MaterialTheme.typography.h3
        )
    }
}

@Composable
private fun TypedItem(
    label: Int,
    nothingValue: Int,
    value: String,
    dialogContent: @Composable (dismiss: () -> Unit) -> Unit,
) {
    var dialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        LabelText(label)

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                value.ifEmpty { stringResource(nothingValue) },
                modifier = Modifier
                    .padding(vertical = Dimensions.half)
                    .weight(1f)
            )

            TextButton(onClick = { dialog = true }) {
                Text(stringResource(R.string.planned_task_change).uppercase(Locale.getDefault()))
            }
        }

        if (dialog) {
            dialogContent { dialog = false }
        }
    }
}

@Composable
private fun TypedItemList(
    label: Int,
    value: String,
    onValueChange: (String) -> Unit,
    label2: Int,
    items: ImmutableList<String>,
    dialogContent: @Composable (dismiss: () -> Unit) -> Unit,
) {
    var dialog by remember { mutableStateOf(false) }

    Column {
        LabelText(label)

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimensions.half)
        )

        LabelText(label2)

        Row(modifier = Modifier.fillMaxWidth()) {
            FromStartToStartAnimContent(
                targetState = items.isEmpty(),
                modifier = Modifier
                    .padding(Dimensions.half)
                    .weight(1f)
            ) {
                if (it) {
                    Text(stringResource(R.string.planned_task_group_unknown))
                } else
                    Column { items.forEach { item -> Text(item) } }
            }

            TextButton(onClick = { dialog = true }) {
                Text(stringResource(R.string.planned_task_change).uppercase(Locale.getDefault()))
            }
        }

        if (dialog) {
            dialogContent { dialog = false }
        }
    }
}

private fun showTimePicker(
    activity: ComponentActivity,
    hour: Int,
    minute: Int,
    updateTime: (hour: Int, minute: Int) -> Unit,
) {
    val dialog = TimePickerDialog(activity, { _, h, m -> updateTime(h, m) }, hour, minute, true)
    dialog.show()
}
