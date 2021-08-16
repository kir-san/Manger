package com.san.kir.manger.ui.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.imePadding
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar


@Composable
fun TopBarScreenWithInsets(
    modifier: Modifier = Modifier,
    nav: NavController? = null,
    title: String = "",
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {

    Scaffold(modifier = modifier,
             topBar = {
                 TopAppBar(
                     title = { Text(text = title) },
                     navigationIcon = {
                         IconButton(onClick = { nav?.navigateUp() }) {
                             Icon(Icons.Default.ArrowBack, "")
                         }
                     },
                     modifier = Modifier
                         .statusBarsPadding()
                         .fillMaxWidth()
                         .padding(0.dp),
                     actions = actions,
                     contentPadding = rememberInsetsPaddingValues(
                         insets = LocalWindowInsets.current.systemBars,
                         applyBottom = false, applyTop = false
                     )
                 )
             }) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    rememberInsetsPaddingValues(
                        insets = LocalWindowInsets.current.systemBars,
                        applyStart = true, applyEnd = true,
                        applyBottom = false, applyTop = false,
                        additionalTop = contentPadding.calculateTopPadding(),
                        additionalBottom = contentPadding.calculateBottomPadding(),
                        additionalStart = 16.dp, additionalEnd = 16.dp
                    )
                )
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            content()
            if (LocalWindowInsets.current.ime.bottom <= 0)
                Spacer(modifier = Modifier.navigationBarsHeight(16.dp))
            else
                Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
