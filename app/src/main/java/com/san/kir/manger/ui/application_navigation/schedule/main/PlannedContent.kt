package com.san.kir.manger.ui.application_navigation.schedule.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.san.kir.manger.room.entities.PlannedTask
import com.san.kir.manger.ui.application_navigation.schedule.ScheduleNavTarget
import com.san.kir.manger.ui.utils.navigate

@Composable
fun PlannedContent(
    nav: NavHostController,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = rememberInsetsPaddingValues(
            LocalWindowInsets.current.systemBars,
            applyStart = false, applyTop = false, applyEnd = false
        )
    ) {
        items(viewModel.items.size, { index -> viewModel.items[index].id }) { index ->
            ItemContent(viewModel.items[index], viewModel) {
                nav.navigate(ScheduleNavTarget.Schedule, viewModel.items[index].id)
            }
        }
    }
}

@Composable
private fun ItemContent(item: PlannedTask, viewModel: ScheduleViewModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                viewModel.itemName(item),
                maxLines = 1
            )

            Text(
                viewModel.itemInfo(item),
                maxLines = 1,
                style = MaterialTheme.typography.subtitle2
            )
        }

        var checed by remember(item) { mutableStateOf(item.isEnabled) }
        Switch(checked = checed, onCheckedChange = {
            checed = it
            item.isEnabled = it
            viewModel.update(item)
        })
    }
}

