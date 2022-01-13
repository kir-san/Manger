package com.san.kir.features.shikimori.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.ComponentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.features.shikimori.ui.catalog.ShikimoriScreen
import com.san.kir.features.shikimori.ui.catalog.ShikimoriViewModel
import com.san.kir.features.shikimori.ui.catalog_item.ShikiItemScreen
import com.san.kir.features.shikimori.ui.catalog_item.ShikiItemViewModel
import com.san.kir.features.shikimori.ui.listItem.ShikimoriItem

fun ComponentActivity.setContent() {
    setContentView(
        ComposeView(this).apply {
            setContent {
                ShikimoriContent()
            }
        }
    )
}


@Composable
internal fun ShikimoriContent() {
    val viewModel = hiltViewModel<ShikimoriViewModel>()
    val viewModelItem = hiltViewModel<ShikiItemViewModel>()
    var nav: ShikiNavTarget by remember { mutableStateOf(ShikiNavTarget.Catalog) }


    Crossfade(targetState = nav) { target ->
        when (target) {
            ShikiNavTarget.Start ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    ShikimoriItem(
                        viewModel,
                        navigateToManager = { nav = ShikiNavTarget.Catalog })
                }
            ShikiNavTarget.Catalog ->
                ShikimoriScreen(
                    viewModel,
                    navigateUp = { nav = ShikiNavTarget.Start },
                    navigateToShikiItem = { nav = ShikiNavTarget.ShikiItem(it) },
                    navigateToLocalItem = {}
                )
            is ShikiNavTarget.ShikiItem -> {
                viewModelItem.update(target.id)
                ShikiItemScreen(
                    viewModel = viewModelItem,
                    navigateUp = { nav = ShikiNavTarget.Catalog })
            }
        }
    }
}

sealed class ShikiNavTarget(val id: Long = 0) {
    object Start : ShikiNavTarget()
    object Catalog : ShikiNavTarget()
    class ShikiItem(id: Long) : ShikiNavTarget(id)
}
