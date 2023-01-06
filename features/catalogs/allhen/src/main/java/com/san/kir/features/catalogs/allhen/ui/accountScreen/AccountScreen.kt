package com.san.kir.features.catalogs.allhen.ui.accountScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.ScreenContent
import com.san.kir.core.compose.topBar
import com.san.kir.data.parsing.sites.Allhentai

@Composable
fun AccountScreen(
    navigateUp: () -> Boolean,
) {
    val viewModel: AccountScreenViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    ScreenContent(
        topBar = topBar(
            navigationButton = NavigationButton.Back(navigateUp),
            title = Allhentai.SITE_NAME
        )
    ) {
        WebView(
            state = rememberWebViewState(Allhentai.AUTH_URL),
            modifier = Modifier.fillMaxSize(),
            onCreated = { view ->
                view.settings.javaScriptEnabled = true
            },
        )
    }
}
