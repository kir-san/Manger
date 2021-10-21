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

import android.view.View
import com.san.kir.ankofork.AnkoInternals.NO_GETTER
import com.san.kir.ankofork.AnkoInternals.noGetter
import kotlin.DeprecationLevel.ERROR

var View.backgroundColorResource: Int
    @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
    set(colorId) = setBackgroundColor(context.resources.getColor(colorId))

var View.horizontalPadding: Int
    @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
    set(value) = setPadding(value, paddingTop, value, paddingBottom)

var View.padding: Int
    @Deprecated(NO_GETTER, level = ERROR) get() = noGetter()
    inline set(value) = setPadding(value, value, value, value)

