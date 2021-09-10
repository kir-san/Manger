package com.san.kir.manger.ui.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.imePadding
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import kotlinx.coroutines.launch


@Composable
fun TopBarScreenWithInsets(
    modifier: Modifier = Modifier,
    navigationButtonListener: () -> Unit = { },
    scaffoldState: ScaffoldState? = null,
    title: String = "",
    subtitle: String = "",
    additionalPadding: Dp = 16.dp,
    actions: @Composable RowScope.() -> Unit = {},
    listContent: (LazyListScope.() -> Unit)? = null,
    drawerContent: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null,
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(modifier = modifier,
             scaffoldState = scaffoldState ?: rememberScaffoldState(),
             drawerContent = drawerContent,
             drawerGesturesEnabled = true,
             topBar = {
                 TopAppBar(
                     title = {
                         Column {
                             Text(text = title, maxLines = 1)
                             if (subtitle.isNotEmpty()) Text(
                                 text = subtitle,
                                 style = MaterialTheme.typography.subtitle2,
                                 maxLines = 1
                             )
                         }
                     },
                     navigationIcon = {
                         if (scaffoldState == null) {
                             IconButton(onClick = navigationButtonListener) {
                                 Icon(Icons.Default.ArrowBack, "")
                             }
                         } else {
                             IconButton(onClick = {
                                 coroutineScope.launch {
                                     scaffoldState.drawerState.open()
                                 }
                             }) { Icon(Icons.Default.Menu, "") }
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
        content?.let { con ->
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
                            additionalStart = additionalPadding, additionalEnd = additionalPadding
                        )
                    )
                    .imePadding()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(additionalPadding))
                con()
                if (LocalWindowInsets.current.ime.bottom <= 0 && additionalPadding > 0.dp)
                    Spacer(modifier = Modifier.navigationBarsHeight(additionalPadding))
                else
                    Spacer(modifier = Modifier.height(additionalPadding))
            }
        }

        listContent?.let { listCon ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        rememberInsetsPaddingValues(
                            insets = LocalWindowInsets.current.systemBars,
                            applyStart = true, applyEnd = true,
                            applyBottom = false, applyTop = false,
                            additionalTop = contentPadding.calculateTopPadding(),
                            additionalBottom = contentPadding.calculateBottomPadding(),
                            additionalStart = additionalPadding, additionalEnd = additionalPadding
                        )
                    )
                    .imePadding(),
                contentPadding = rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.systemBars,
                    applyTop = false,
                ),
            ) {
                listCon()
            }
        }
    }
}


@Composable
fun TopBarScreenContent(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState? = null,
    title: String = "",
    subtitle: String = "",
    actions: @Composable RowScope.() -> Unit = {},
    drawerContent: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null,
) {
    TopBarScreenWithInsets(
        modifier = modifier,
        scaffoldState = scaffoldState,
        navigationButtonListener = { navHostController.navigateUp() },
        title = title,
        subtitle = subtitle,
        actions = actions,
        content = content,
        drawerContent = drawerContent,
    )
}

@Composable
fun TopBarScreenList(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
    additionalPadding: Dp = 16.dp,
    title: String = "",
    subtitle: String = "",
    actions: @Composable RowScope.() -> Unit = {},
    listContent: (LazyListScope.() -> Unit)? = null,
) {
    TopBarScreenWithInsets(
        modifier = modifier,
        additionalPadding = additionalPadding,
        navigationButtonListener = { navHostController.navigateUp() },
        title = title,
        subtitle = subtitle,
        actions = actions,
        listContent = listContent,
    )
}
