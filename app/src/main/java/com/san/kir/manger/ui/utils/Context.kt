package com.san.kir.manger.ui.utils

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.san.kir.manger.utils.extensions.log

@Composable
inline fun <reified VM : ViewModel> NavBackStackEntry.viewModel(nav: NavHostController): VM {
    val backStackEntry = nav.getBackStackEntry(nav.currentBackStackEntry!!.destination.id)
    log("${backStackEntry.destination.id}")
    log("${destination.parent?.id}")
    return ViewModelProvider(backStackEntry).get()
}
