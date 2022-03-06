package com.san.kir.manger.ui.application_navigation.catalog.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.MenuIcon
import com.san.kir.core.compose_utils.MenuText
import com.san.kir.core.compose_utils.TopBarScreenList
import com.san.kir.core.compose_utils.rememberImage
import com.san.kir.core.compose_utils.systemBarsHorizontalPadding
import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.core.utils.findInGoogle
import com.san.kir.data.models.base.Site
import com.san.kir.manger.R

@Composable
fun CatalogsScreen(
    navigateUp: () -> Unit,
    navigateToItem: (String) -> Unit,
    navigateToSearch: () -> Unit,
    viewModel: CatalogsViewModel = hiltViewModel(),
) {
    val siteList by viewModel.siteList.collectAsState(emptyList())

    TopBarScreenList(
        navigateUp = navigateUp,
        title = stringResource(R.string.main_menu_catalogs),
        actions = { CatalogsActions(navigateToSearch, viewModel) },
        additionalPadding = Dimensions.small
    ) {
        items(items = siteList, key = { site -> site.id }) { item ->
            ItemView(item, viewModel, navigateToItem)
        }

    }
}

@Composable
fun ItemView(item: Site, viewModel: CatalogsViewModel, onClick: (String) -> Unit) {
    var isError by remember { mutableStateOf(false) }
    var isInit by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item.name) }
            .padding(vertical = Dimensions.smaller, horizontal = Dimensions.default)
            .padding(systemBarsHorizontalPadding())
    ) {
        Image(
            rememberImage(findInGoogle(item.host)),
            contentDescription = "",
            modifier = Modifier
                .padding(end = 8.dp)
                .size(50.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f, true)
                .align(Alignment.CenterVertically)
        ) {
            Text(item.name, style = MaterialTheme.typography.body1)
            Text(item.host, style = MaterialTheme.typography.body2)
        }

        if (isError) Icon(
            Icons.Default.DeviceUnknown, "",
            tint = MaterialTheme.colors.error,
            modifier = Modifier
                .padding(end = 8.dp)
                .size(15.dp)
                .align(Alignment.CenterVertically)
        )

        if (isInit) CircularProgressIndicator(
            strokeWidth = 2.dp,
            modifier = Modifier
                .padding(end = 8.dp)
                .size(15.dp)
                .align(Alignment.CenterVertically)
        )

        Text(
            text = stringResource(
                R.string.site_volume, item.oldVolume, item.volume - item.oldVolume
            ),
            style = MaterialTheme.typography.caption,
            modifier = Modifier
                .align(Alignment.CenterVertically)
        )
    }

    LaunchedEffect(true) {
        val site = withDefaultContext { viewModel.site(item) }
        if (site.isInit.not()) {
            isError = false
            isInit = true

            viewModel.updateSiteInfo(site)
                .onFailure {
                    isError = true
                }

            isInit = false
        }
    }

}

@Composable
fun CatalogsActions(navigateToSearch: () -> Unit, viewModel: CatalogsViewModel) {
    var expanded by remember { mutableStateOf(false) }

    MenuIcon(
        icon = Icons.Default.Search,
        onClick = navigateToSearch)

    MenuIcon(icon = Icons.Default.MoreVert, onClick = { expanded = true })

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {

        MenuText(id = R.string.catalog_for_one_site_update_all, onClick = {
            expanded = false
            viewModel.update()
        })

        MenuText(id = R.string.catalog_for_one_site_update_catalog_contain, onClick = {
            expanded = false
            viewModel.updateCatalogs()
        })
    }

}
