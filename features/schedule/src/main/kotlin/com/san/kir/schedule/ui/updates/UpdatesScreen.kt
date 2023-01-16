package com.san.kir.schedule.ui.updates

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.core.compose.bottomInsetsPadding
import com.san.kir.schedule.R
import com.san.kir.schedule.utils.ItemContent

@Composable
fun UpdatesScreen() {
    val viewModel: UpdatesViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    val sendEvent = remember { { event: UpdatesEvent -> viewModel.sendEvent(event) } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = bottomInsetsPadding(),
    ) {
        items(state.items.size, { index -> state.items[index].id }) { index ->
            val item = state.items[index]

            ItemContent(
                title = item.name,
                subTitle = stringResource(R.string.available_update_category_name, item.category),
                checked = item.update,
                onCheckedChange = { sendEvent(UpdatesEvent.Update(item.id, it)) }
            )
        }
    }
}
