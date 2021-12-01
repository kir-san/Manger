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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import java.io.Serializable

object AnkoInternals {
    const val NO_GETTER: String = "Property does not have a getter"

    fun noGetter(): Nothing = throw AnkoException("Property does not have a getter")

    private class AnkoContextThemeWrapper(base: Context?, val theme: Int) : ContextThemeWrapper(base, theme)

    fun <T : View> addView(manager: ViewManager, view: T) = when (manager) {
        is ViewGroup -> manager.addView(view)
        is AnkoContext<*> -> manager.addView(view, null)
        else -> throw AnkoException("$manager is the wrong parent")
    }

    fun <T : View> addView(ctx: Context, view: T) {
        ctx.ui { addView(this, view) }
    }

    fun <T : View> addView(activity: Activity, view: T) {
        createAnkoContext(activity, { addView(this, view) }, true)
    }

    fun wrapContextIfNeeded(ctx: Context, theme: Int): Context {
        return if (theme != 0 && (ctx !is AnkoContextThemeWrapper || ctx.theme != theme)) {
            // If the context isn't a ContextThemeWrapper, or it is but does not have
            // the same theme as we need, wrap it in a new wrapper
            AnkoContextThemeWrapper(ctx, theme)
        } else {
            ctx
        }
    }

    fun getContext(manager: ViewManager): Context = when (manager) {
        is ViewGroup -> manager.context
        is AnkoContext<*> -> manager.ctx
        else -> throw AnkoException("$manager is the wrong parent")
    }

    inline fun <T> T.createAnkoContext(
        ctx: Context,
        init: AnkoContext<T>.() -> Unit,
        setContentView: Boolean = false
    ): AnkoContext<T> {
        val dsl = AnkoContextImpl(ctx, this, setContentView)
        dsl.init()
        return dsl
    }

}
