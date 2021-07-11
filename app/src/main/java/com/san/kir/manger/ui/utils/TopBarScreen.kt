package com.san.kir.manger.ui.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController

@Composable
fun TopBarScreen(
    modifier: Modifier = Modifier,
    nav: NavController? = null,
    title: String = "",
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {

    Scaffold(modifier = modifier,
             topBar = {
                 TopAppBar(
                     title = { Text(text = title) },
                     navigationIcon = {
                         IconButton(onClick = { nav?.popBackStack() }) {
                             Icon(Icons.Default.ArrowBack, "")
                         }
                     },
                     modifier = Modifier
                         .statusBarsPadding()
                         .fillMaxWidth()
                         .padding(0.dp),
                     actions = actions
                 )

             }) {
        content(it)
    }
}
