@file:Suppress("KDocUnresolvedReference")

package com.san.kir.ankofork

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

open class AnkoException(message: String = "") : RuntimeException(message)

/**
 * Execute [f] only if the current Android SDK version is [version] or newer.
 * Do nothing otherwise.
 */
@ChecksSdkIntAtLeast(parameter = 0, lambda = 1)
inline fun doFromSdk(version: Int, f: () -> Unit) {
    if (Build.VERSION.SDK_INT >= version) f()
}
