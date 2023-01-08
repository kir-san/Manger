package com.san.kir.catalog.ui.catalogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.catalog.R
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.ScreenList
import com.san.kir.core.compose.animation.FromEndToEndAnimContent
import com.san.kir.core.compose.endInsetsPadding
import com.san.kir.core.compose.rememberImage
import com.san.kir.core.compose.startInsetsPadding
import com.san.kir.core.compose.topBar
import com.san.kir.core.utils.findInGoogle

@Composable
fun CatalogsScreen(
    navigateUp: () -> Boolean,
    navigateToItem: (String) -> Unit,
    navigateToSearch: () -> Unit,
) {
    val viewModel: CatalogsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    val sendEvent = remember { { event: CatalogsEvent -> viewModel.sendEvent(event) } }

    ScreenList(
        topBar = topBar(
            navigationButton = NavigationButton.Back(navigateUp),
            title = stringResource(R.string.catalogs),
            actions = {
                MenuIcon(
                    icon = Icons.Default.Search,
                    onClick = navigateToSearch,
                )

                ExpandedMenu {
                    MenuText(
                        id = R.string.catalog_for_one_site_update_catalog_contain,
                        onClick = { sendEvent(CatalogsEvent.UpdateContent) },
                    )
                }
            },
            hasAction = state.background
        ),
        additionalPadding = Dimensions.half,
        onRefresh = { sendEvent(CatalogsEvent.UpdateData) }
    ) {
        items(items = state.items, key = { it.hashCode() }) { item ->
            ItemView(item) { navigateToItem(item.name) }
        }

    }
}

@Composable
private fun ItemView(item: CheckableSite, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Dimensions.quarter, horizontal = Dimensions.default)
    ) {
        Image(
            rememberImage(findInGoogle(item.host)),
            contentDescription = "",
            modifier = Modifier
                .padding(end = Dimensions.half)
                .size(Dimensions.Image.default)
                .startInsetsPadding()
        )

        Column(
            modifier = Modifier
                .weight(1f, true)
                .align(Alignment.CenterVertically)
        ) {
            Text(item.name, style = MaterialTheme.typography.body1)
            Text(item.host, style = MaterialTheme.typography.body2)
        }

        FromEndToEndAnimContent(
            targetState = item.state,
            modifier = Modifier
                .padding(end = Dimensions.half)
                .size(Dimensions.default)
                .align(Alignment.CenterVertically)
        ) {
            when (it) {
                SiteState.Error ->
                    Icon(
                        Icons.Default.DeviceUnknown, "",
                        tint = MaterialTheme.colors.error,
                    )

                SiteState.Load ->
                    CircularProgressIndicator(strokeWidth = Dimensions.smallest)

                SiteState.Ok -> {}
            }
        }

        FromEndToEndAnimContent(
            targetState = item.volume,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .endInsetsPadding()
        ) {
            when (it) {
                VolumeState.Error -> {
                    Text(
                        text = stringResource(R.string.site_volume_error),
                        style = MaterialTheme.typography.caption,
                    )
                }

                VolumeState.Load -> {
                    Text(
                        text = stringResource(R.string.site_volume_load),
                        style = MaterialTheme.typography.caption,
                    )
                }

                is VolumeState.Ok -> {
                    if (it.diff == 0) {
                        Text(
                            text = stringResource(R.string.site_volume_with_number, it.volume),
                            style = MaterialTheme.typography.caption,
                        )
                    } else {
                        Text(
                            text = stringResource(
                                R.string.site_volume_with_numbers, it.diff, it.volume
                            ),
                            style = MaterialTheme.typography.caption,
                        )
                    }
                }
            }
        }
    }
}
