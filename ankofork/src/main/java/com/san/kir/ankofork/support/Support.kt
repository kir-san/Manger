/*
 * Copyright 2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused")
package com.san.kir.ankofork.support

import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.AnkoInternals
import com.san.kir.ankofork.AnkoInternals.createAnkoContext
import com.san.kir.ankofork.Orientation
import com.san.kir.ankofork.ScreenSize
import com.san.kir.ankofork.UiMode

inline fun <reified T : View> Fragment.find(id: Int): T = view?.findViewById(id) as T
inline fun <reified T : View> Fragment.findOptional(id: Int): T? = view?.findViewById(id) as? T

fun Fragment.ui(init: AnkoContext<Fragment>.() -> Unit) = createAnkoContext(requireActivity(), init)

inline fun <T: Any> Fragment.configuration(
    screenSize: ScreenSize? = null,
    density: ClosedRange<Int>? = null,
    language: String? = null,
    orientation: Orientation? = null,
    long: Boolean? = null,
    fromSdk: Int? = null,
    sdk: Int? = null,
    uiMode: UiMode? = null,
    nightMode: Boolean? = null,
    rightToLeft: Boolean? = null,
    smallestWidth: Int? = null,
    init: () -> T
): T? {
    val act = activity
    return if (act != null) {
        if (AnkoInternals.testConfiguration(act, screenSize, density, language, orientation, long,
                                            fromSdk, sdk, uiMode, nightMode, rightToLeft, smallestWidth)) init() else null
    }
    else null
}

fun <T: Fragment> T.withArguments(vararg params: Pair<String, Any?>): T {
    arguments = bundleOf(*params)
    return this
}

