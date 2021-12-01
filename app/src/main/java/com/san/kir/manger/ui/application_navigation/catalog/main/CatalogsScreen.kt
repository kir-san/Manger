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
import androidx.navigation.NavHostController
import com.san.kir.manger.R
import com.san.kir.manger.data.room.entities.Site
import com.san.kir.manger.ui.application_navigation.catalog.CatalogsNavTarget
import com.san.kir.manger.utils.compose.MenuIcon
import com.san.kir.manger.utils.compose.MenuText
import com.san.kir.manger.utils.compose.TopBarScreenList
import com.san.kir.manger.utils.compose.navigate
import com.san.kir.manger.utils.compose.rememberImage
import com.san.kir.manger.utils.coroutines.withDefaultContext

@Composable
fun CatalogsScreen(
    navHostController: NavHostController,
    viewModel: CatalogsViewModel = hiltViewModel()
) {
    val siteList by viewModel.siteList.collectAsState(emptyList())

    TopBarScreenList(
        navHostController = navHostController,
        title = stringResource(R.string.main_menu_catalogs),
        actions = { CatalogsActions(navHostController, viewModel) }
    ) {
        items(items = siteList, key = { site -> site.id }) { item ->
            ItemView(item, viewModel) {
                navHostController.navigate(CatalogsNavTarget.Catalog, item.name)
            }
        }

    }
}

@Composable
fun ItemView(item: Site, viewModel: CatalogsViewModel, onClick: () -> Unit) {
    var isError by remember { mutableStateOf(false) }
    var isInit by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Image(
            rememberImage("http://www.google.com/s2/favicons?domain=${item.host}"), "",
            modifier = Modifier
                .padding(vertical = 10.dp)
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
fun CatalogsActions(nav: NavHostController, viewModel: CatalogsViewModel) {
    var expanded by remember { mutableStateOf(false) }

    MenuIcon(
        icon = Icons.Default.Search,
        onClick = { nav.navigate(CatalogsNavTarget.GlobalSearch) })

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
