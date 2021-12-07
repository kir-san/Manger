package com.san.kir.manger.utils.extensions

import androidx.annotation.StringRes
import android.view.Menu
import android.view.MenuItem

fun MenuItem.showAlways(): MenuItem {
    return this.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
}

fun Menu.add(itemId: Int, @StringRes titleRes: Int): MenuItem = add(0, itemId, 0, titleRes)

