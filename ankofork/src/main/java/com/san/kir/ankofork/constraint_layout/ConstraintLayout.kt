/*
 * Copyright 2017 JetBrains s.r.o.
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
package com.san.kir.ankofork.constraint_layout

import android.view.View
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.san.kir.ankofork.AnkoInternals
import com.san.kir.ankofork.AnkoInternals.noGetter
import com.san.kir.ankofork.constraint_layout.ConstraintSetBuilder.Connection.BasicConnection
import com.san.kir.ankofork.constraint_layout.ConstraintSetBuilder.Side

val ConstraintLayout.matchConstraint
    get() = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT

fun ConstraintLayout.applyConstraintSet(init: ConstraintSetBuilder.() -> Unit): ConstraintSet =
        constraintSet(init).also { it.applyTo(this) }

fun ConstraintLayout.constraintSet(init: ConstraintSetBuilder.() -> Unit): ConstraintSet =
        ConstraintSetBuilder().also { it.clone(this) }.apply(init)

class ViewConstraintBuilder(
    @IdRes private val viewId: Int,
    private val constraintSetBuilder: ConstraintSetBuilder
) {

    infix fun Pair<Side, Side>.of(@IdRes targetViewId: Int): BasicConnection =
            constraintSetBuilder.run { (first of viewId) to (second of targetViewId) }

    infix fun Pair<Side, Side>.of(targetView: View): BasicConnection = this of targetView.id

    fun clear() {
        constraintSetBuilder.clear(viewId)
    }

    fun clear(sideId: Int) {
        constraintSetBuilder.clear(viewId, sideId)
    }

    var height: Int
        @Deprecated(AnkoInternals.NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
        set(value) {
            constraintSetBuilder.constrainHeight(viewId, value)
        }

    var width: Int
        @Deprecated(AnkoInternals.NO_GETTER, level = DeprecationLevel.ERROR) get() = noGetter()
        set(value) {
            constraintSetBuilder.constrainWidth(viewId, value)
        }

}

class ConstraintSetBuilder : ConstraintSet() {
    operator fun Int.invoke(init: ViewConstraintBuilder.() -> Unit) {
        ViewConstraintBuilder(this, this@ConstraintSetBuilder).apply(init)
    }

    operator fun View.invoke(init: ViewConstraintBuilder.() -> Unit) = id.invoke(init)

    infix fun Side.of(@IdRes viewId: Int) = when (this) {
        Side.LEFT -> ViewSide.Left
        Side.RIGHT -> ViewSide.Right
        Side.TOP -> ViewSide.Top
        Side.BOTTOM -> ViewSide.Bottom
        Side.BASELINE -> ViewSide.Baseline
        Side.START -> ViewSide.Start
        Side.END -> ViewSide.End
    }

    infix fun Side.of(view: View) = this of view.id

    infix fun Pair<ViewSide, Side>.of(@IdRes viewId: Int) = first to (second of viewId)

    infix fun Pair<ViewSide, Side>.of(view: View) = first to (second of view.id)

    infix fun ViewSide.to(targetSide: ViewSide) = BasicConnection(this, targetSide)

    enum class Side {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
        BASELINE,
        START,
        END,
    }

    sealed class ViewSide {
        object Left : ViewSide()
        object Right : ViewSide()
        object Top : ViewSide()
        object Bottom : ViewSide()
        object Baseline : ViewSide()
        object Start : ViewSide()
        object End : ViewSide()

        val sideId: Int
            get() = when(this) {
                is Left -> LEFT
                is Right -> RIGHT
                is Top -> TOP
                is Bottom -> BOTTOM
                is Baseline -> BASELINE
                is Start -> START
                is End -> END
            }
    }

    sealed class Connection(val from: ViewSide, val to: ViewSide) {
        class BasicConnection(from: ViewSide, to: ViewSide) : Connection(from, to)
    }
}
