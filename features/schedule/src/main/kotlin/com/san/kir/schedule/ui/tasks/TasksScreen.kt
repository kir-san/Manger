package com.san.kir.schedule.ui.tasks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.core.compose.bottomInsetsPadding
import com.san.kir.schedule.utils.ItemContent

@Composable
internal fun TasksScreen(
    navigateToItem: (Long) -> Unit,
) {
    val viewModel: TasksViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    val sendEvent = remember { { event: TasksEvent -> viewModel.sendEvent(event) } }


    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = bottomInsetsPadding()
    ) {
        items(state.items.size, { index -> state.items[index].id }) { index ->
            val item = state.items[index]

            ItemContent(
                title = item.name,
                subTitle = item.info,
                checked = item.isEnabled,
                onCheckedChange = { sendEvent(TasksEvent.Update(item.id, item.isEnabled.not())) },
                onClick = { navigateToItem(item.id) }
            )
        }
    }
}
