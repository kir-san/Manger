package com.san.kir.manger.ui.drawer.catalogs

import android.graphics.BitmapFactory
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.github.kittinunf.result.coroutines.failure
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.pager.ExperimentalPagerApi
import com.san.kir.ankofork.startService
import com.san.kir.manger.R
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.room.entities.Site
import com.san.kir.manger.services.CatalogForOneSiteUpdaterService
import com.san.kir.manger.ui.CatalogNavigationDestination
import com.san.kir.manger.ui.CatalogsSearchNavigationDestination
import com.san.kir.manger.ui.DrawerNavigationDestination
import com.san.kir.manger.ui.utils.MenuIcon
import com.san.kir.manger.ui.utils.MenuText
import com.san.kir.manger.utils.loadImage
import com.san.kir.manger.view_models.TitleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext

@ExperimentalPagerApi
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
@Composable
fun CatalogsScreen(
    mainNav: NavHostController,
    contentPadding: PaddingValues,
    vm: TitleViewModel = hiltViewModel(mainNav.getBackStackEntry(DrawerNavigationDestination.route)),
    viewModel: CatalogsViewModel = hiltViewModel()
) {
    vm.setTitle(stringResource(id = R.string.main_menu_catalogs))

    val siteList by viewModel.siteList.collectAsState(emptyList())

    LazyColumn(
        contentPadding = rememberInsetsPaddingValues(
            insets = LocalWindowInsets.current.systemBars,
            applyTop = false,
        ),
        modifier = Modifier.padding(top = contentPadding.calculateTopPadding())
    ) {
        items(items = siteList, key = { site -> site.id }) { item ->
            ItemView(item) {
                mainNav.navigate("${CatalogNavigationDestination().base}/${item.name}")
            }
        }
    }
}

@Composable
fun ItemView(item: Site, viewModel: CatalogsViewModel = hiltViewModel(), onClick: () -> Unit) {
    val context = LocalContext.current
    var isError by remember { mutableStateOf(false) }
    var isInit by remember { mutableStateOf(false) }
    var icon by remember { mutableStateOf(ImageBitmap(60, 60)) }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Image(
            icon, "", modifier = Modifier
                .size(60.dp)
                .padding(10.dp)
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
                .size(15.dp)
                .align(Alignment.CenterVertically)
        )

        if (isInit) CircularProgressIndicator(
            strokeWidth = 2.dp,
            modifier = Modifier
                .size(15.dp)
                .align(Alignment.CenterVertically)
        )

        Text(
            text = stringResource(
                R.string.site_volume, item.oldVolume, item.volume - item.oldVolume
            ),
            style = MaterialTheme.typography.caption,
            modifier = Modifier
                .padding(10.dp)
                .align(Alignment.CenterVertically)
        )
    }

    LaunchedEffect(key1 = true) {
        if (item.host.isNotEmpty()) {
            loadImage("http://www.google.com/s2/favicons?domain=${item.host}") {
                onSuccess { icon = it }
                onError {
                    val errorImage =
                        BitmapFactory.decodeResource(context.resources, R.drawable.ic_error)
                    if (errorImage != null) icon = errorImage.asImageBitmap()
                }
                start()
            }
        }

        val site = withContext(Dispatchers.IO) { viewModel.site(item) }
        if (site.isInit.not()) {
            isError = false
            isInit = true

            viewModel.updateSiteInfo(site)
                .failure {
                    isError = true
                }

            isInit = false
        }
    }

}

@ExperimentalAnimationApi
@Composable
fun CatalogsActions(mainNav: NavHostController, viewModel: CatalogsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    MenuIcon(
        icon = Icons.Default.Search,
        onClick = { mainNav.navigate(CatalogsSearchNavigationDestination.route) })

    MenuIcon(icon = Icons.Default.MoreVert, onClick = { expanded = true })

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {

        MenuText(id = R.string.catalog_for_one_site_update_all, onClick = {
            expanded = false
            viewModel.update()
        })

        MenuText(id = R.string.catalog_for_one_site_update_catalog_contain, onClick = {
            expanded = false
            ManageSites.CATALOG_SITES.forEach {
                if (!CatalogForOneSiteUpdaterService.isContain(it.catalogName))
                    context.startService<CatalogForOneSiteUpdaterService>("catalogName" to it.catalogName)
            }
        })
    }

}
