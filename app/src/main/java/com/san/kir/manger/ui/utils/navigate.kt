package com.san.kir.manger.ui.utils

import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.san.kir.manger.ui.MainAppScreen
import com.san.kir.manger.utils.extensions.log

fun NavController.navigate(screen: MainAppScreen, value: Parcelable? = null) {
    value?.let {
        currentBackStackEntry?.replaceArguments(
            bundleOf(screen.element to it)
        )
    }
    navigate(screen.route)
}

fun <T : Parcelable> NavController.getElement(screen: MainAppScreen): T? {
    return previousBackStackEntry?.arguments?.getParcelable(screen.element)
}
