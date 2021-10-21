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

package com.san.kir.ankofork

import android.content.Context
import android.view.ViewManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.san.kir.ankofork.sdk28._LinearLayout

@PublishedApi
internal object AnkoFactoriesCustomViews {
    val VERTICAL_LAYOUT_FACTORY = { ctx: Context ->
        val view = _LinearLayout(ctx)
        view.orientation = LinearLayout.VERTICAL
        view
    }

    val HORIZONTAL_PROGRESS_BAR_FACTORY = { ctx: Context ->
        ProgressBar(ctx, null, android.R.attr.progressBarStyleHorizontal)
    }
}

inline fun Context.verticalLayout(theme: Int = 0, init: (@AnkoViewDslMarker _LinearLayout).() -> Unit): LinearLayout {
    return ankoView(AnkoFactoriesCustomViews.VERTICAL_LAYOUT_FACTORY, theme, init)
}

inline fun ViewManager.horizontalProgressBar(theme: Int = 0, init: (@AnkoViewDslMarker ProgressBar).() -> Unit): ProgressBar {
    return ankoView(AnkoFactoriesCustomViews.HORIZONTAL_PROGRESS_BAR_FACTORY, theme, init)
}

