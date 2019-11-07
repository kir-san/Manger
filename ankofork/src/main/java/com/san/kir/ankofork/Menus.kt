package com.san.kir.ankofork

import android.view.Menu
import android.view.MenuItem


/**
 * Create a plain menu item
 */
inline fun Menu.item(title: CharSequence, /*@DrawableRes*/ icon: Int = 0, checkable: Boolean = false): MenuItem =
        add(title).apply {
            setIcon(icon)
            isCheckable = checkable
        }

/**
 * Create a menu item and configure it
 */
inline fun Menu.item(title: CharSequence, /*@DrawableRes*/ icon: Int = 0, checkable: Boolean = false, configure: MenuItem.() -> Unit): MenuItem =
        add(title).apply {
            setIcon(icon)
            isCheckable = checkable
            configure()
        }

/**
 * Create a menu item with title from resources
 */
inline fun Menu.item(/*@StringRes*/ titleRes: Int, /*@DrawableRes*/ icon: Int = 0, checkable: Boolean = false): MenuItem =
        add(titleRes).apply {
            setIcon(icon)
            isCheckable = checkable
        }

/**
 * Create a menu item with title from resources and configure it
 */
inline fun Menu.item(/*@StringRes*/ titleRes: Int, /*@DrawableRes*/ icon: Int = 0, checkable: Boolean = false, configure: MenuItem.() -> Unit): MenuItem =
        add(titleRes).apply {
            setIcon(icon)
            isCheckable = checkable
            configure()
        }


/**
 * Set OnClickListener on a menu item
 */
inline fun MenuItem.onClick(consume: Boolean = true, crossinline action: () -> Unit) {
    setOnMenuItemClickListener { action(); consume }
}
