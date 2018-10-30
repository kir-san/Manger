package com.san.kir.manger.extending.views

import android.view.Menu
import android.view.MenuItem

fun MenuItem.showAlways(): MenuItem {
    return this.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
}

fun MenuItem.showIfRoom(): MenuItem {
    return this.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
}

fun MenuItem.showNever(): MenuItem {
    return this.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
}

fun Menu.add(itemId: Int, titleRes: Int): MenuItem = add(0, itemId, 0, titleRes)
