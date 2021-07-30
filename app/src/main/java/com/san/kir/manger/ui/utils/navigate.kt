package com.san.kir.manger.ui.utils

import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.san.kir.manger.ui.application_navigation.ApplicationNavigationDestination

fun NavController.navigate(screen: ApplicationNavigationDestination, value: Parcelable? = null) {
    value?.let {
        currentBackStackEntry?.replaceArguments(
            bundleOf(screen.element to it)
        )
    }
    navigate(screen.route)
}

fun <T : Parcelable> NavController.getElement(screen: ApplicationNavigationDestination): T? {
    return previousBackStackEntry?.arguments?.getParcelable(screen.element)
}
