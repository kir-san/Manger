package com.san.kir.manger.ui.utils

import android.view.View
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * https://github.com/chrisbanes/accompanist
 */

@Stable
interface Insets {
    /**
     * The left dimension of these insets in pixels.
     */
    @get:IntRange(from = 0)
    val left: Int

    /**
     * The top dimension of these insets in pixels.
     */
    @get:IntRange(from = 0)
    val top: Int

    /**
     * The right dimension of these insets in pixels.
     */
    @get:IntRange(from = 0)
    val right: Int

    /**
     * The bottom dimension of these insets in pixels.
     */
    @get:IntRange(from = 0)
    val bottom: Int

    fun copy(
        left: Int = this.left,
        top: Int = this.top,
        right: Int = this.right,
        bottom: Int = this.bottom,
    ): Insets = MutableInsets(left, top, right, bottom)

    operator fun minus(other: Insets): Insets = copy(
        left = this.left - other.left,
        top = this.top - other.top,
        right = this.right - other.right,
        bottom = this.bottom - other.bottom,
    )

    operator fun plus(other: Insets): Insets = copy(
        left = this.left + other.left,
        top = this.top + other.top,
        right = this.right + other.right,
        bottom = this.bottom + other.bottom,
    )
}

internal class MutableInsets(
    left: Int = 0,
    top: Int = 0,
    right: Int = 0,
    bottom: Int = 0,
) : Insets {
    override var left by mutableStateOf(left)
        internal set

    override var top by mutableStateOf(top)
        internal set

    override var right by mutableStateOf(right)
        internal set

    override var bottom by mutableStateOf(bottom)
        internal set

    fun reset() {
        left = 0
        top = 0
        right = 0
        bottom = 0
    }
}

/**
 * Represents the values for a type of insets, and stores information about the layout insets,
 * animating insets, and visibility of the insets.
 *
 * [InsetsType] instances are commonly stored in a [WindowInsets] instance.
 */
@Stable
@Suppress("MemberVisibilityCanBePrivate")
class InsetsType : Insets {
    private var ongoingAnimationsCount by mutableStateOf(0)
    internal val _layoutInsets = MutableInsets()
    internal val _animatedInsets = MutableInsets()

    /**
     * The layout insets for this [InsetsType]. These are the insets which are defined from the
     * current window layout.
     *
     * You should not normally need to use this directly, and instead use [left], [top],
     * [right], and [bottom] to return the correct value for the current state.
     */
    val layoutInsets: Insets
        get() = _layoutInsets

    /**
     * The animated insets for this [InsetsType]. These are the insets which are updated from
     * any on-going animations. If there are no animations in progress, the returned [Insets] will
     * be empty.
     *
     * You should not normally need to use this directly, and instead use [left], [top],
     * [right], and [bottom] to return the correct value for the current state.
     */
    val animatedInsets: Insets
        get() = _animatedInsets

    /**
     * The left dimension of the insets in pixels.
     */
    override val left: Int
        get() = (if (animationInProgress) animatedInsets else layoutInsets).left

    /**
     * The top dimension of the insets in pixels.
     */
    override val top: Int
        get() = (if (animationInProgress) animatedInsets else layoutInsets).top

    /**
     * The right dimension of the insets in pixels.
     */
    override val right: Int
        get() = (if (animationInProgress) animatedInsets else layoutInsets).right

    /**
     * The bottom dimension of the insets in pixels.
     */
    override val bottom: Int
        get() = (if (animationInProgress) animatedInsets else layoutInsets).bottom

    /**
     * Whether the insets are currently visible.
     */
    var isVisible by mutableStateOf(true)
        internal set

    /**
     * Whether this insets type is being animated at this moment.
     */
    val animationInProgress: Boolean
        get() = ongoingAnimationsCount > 0

    /**
     * The progress of any ongoing animations, in the range of 0 to 1.
     * If there is no animation in progress, this will return 0.
     */
    @get:FloatRange(from = 0.0, to = 1.0)
    var animationFraction by mutableStateOf(0f)
        internal set

    internal fun onAnimationStart() {
        ongoingAnimationsCount++
    }

    internal fun onAnimationEnd() {
        ongoingAnimationsCount--

        if (ongoingAnimationsCount == 0) {
            // If there are no on-going animations, clear out the animated insets
            _animatedInsets.reset()
            animationFraction = 0f
        }
    }
}


class WindowInsets {
    /**
     * Inset values which match [WindowInsetsCompat.Type.systemBars]
     */
    val systemBars = InsetsType()

    /**
     * Inset values which match [WindowInsetsCompat.Type.systemGestures]
     */
    val systemGestures = InsetsType()

    /**
     * Inset values which match [WindowInsetsCompat.Type.navigationBars]
     */
    val navigationBars = InsetsType()

    /**
     * Inset values which match [WindowInsetsCompat.Type.statusBars]
     */
    val statusBars = InsetsType()

    /**
     * Inset values which match [WindowInsetsCompat.Type.ime]
     */
    val ime = InsetsType()

    /**
     * Inset values wich match [WindowInsetsCompat.Type.displayCutout]
     */
    val displayCutout = InsetsType()
}

val LocalWindowInsets = staticCompositionLocalOf { WindowInsets() }

/**
 * This class sets up the necessary listeners on the given [view] to be able to observe
 * [WindowInsetsCompat] instances dispatched by the system.
 *
 * This class is useful for when you prefer to handle the ownership of the [WindowInsets]
 * yourself. One example of this is if you find yourself using [ProvideWindowInsets] in fragments.
 *
 * It is convenient to use [ProvideWindowInsets] in fragments, but that can result in a
 * delay in the initial inset update, which results in a visual flicker.
 * See [this issue](https://github.com/chrisbanes/accompanist/issues/155) for more information.
 *
 * The alternative is for fragments to manage the [WindowInsets] themselves, like so:
 *
 * ```
 * override fun onCreateView(
 *     inflater: LayoutInflater,
 *     container: ViewGroup?,
 *     savedInstanceState: Bundle?
 * ): View = ComposeView(requireContext()).apply {
 *     layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
 *
 *     // Create an ViewWindowInsetObserver using this view
 *     val observer = ViewWindowInsetObserver(this)
 *
 *     // Call start() to start listening now.
 *     // The WindowInsets instance is returned to us.
 *     val windowInsets = observer.start()
 *
 *     setContent {
 *         // Instead of calling ProvideWindowInsets, we use Providers to provide
 *         // the WindowInsets instance from above to AmbientWindowInsets
 *         Providers(AmbientWindowInsets provides windowInsets) {
 *             /* Content */
 *         }
 *     }
 * }
 * ```
 *
 * @param view The view to observe [WindowInsetsCompat]s from.
 */
class ViewWindowInsetObserver(private val view: View) {
    private val attachListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) = v.requestApplyInsets()
        override fun onViewDetachedFromWindow(v: View) = Unit
    }

    /**
     * Whether this [ViewWindowInsetObserver] is currently observing.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var isObserving: Boolean = false
        private set

    /**
     * Start observing window insets from [view]. Make sure to call [stop] if required.
     *
     * @param consumeWindowInsets Whether to consume any [WindowInsetsCompat]s which are
     * dispatched to the host view. Defaults to `true`.
     */
    fun start(
        consumeWindowInsets: Boolean = true
    ): WindowInsets {
        return WindowInsets().apply {
            observeInto(
                windowInsets = this,
                consumeWindowInsets = consumeWindowInsets,
                windowInsetsAnimationsEnabled = false
            )
        }
    }

    /**
     * Start observing window insets from [view]. Make sure to call [stop] if required.
     *
     * @param windowInsetsAnimationsEnabled Whether to listen for [WindowInsetsAnimation]s, such as
     * IME animations.
     * @param consumeWindowInsets Whether to consume any [WindowInsetsCompat]s which are
     * dispatched to the host view. Defaults to `true`.
     */
    fun start(
        windowInsetsAnimationsEnabled: Boolean,
        consumeWindowInsets: Boolean = true,
    ): WindowInsets {
        return WindowInsets().apply {
            observeInto(
                windowInsets = this,
                consumeWindowInsets = consumeWindowInsets,
                windowInsetsAnimationsEnabled = windowInsetsAnimationsEnabled
            )
        }
    }

    internal fun observeInto(
        windowInsets: WindowInsets,
        consumeWindowInsets: Boolean,
        windowInsetsAnimationsEnabled: Boolean,
    ) {
        require(!isObserving) {
            "start() called, but this ViewWindowInsetObserver is already observing"
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, wic ->
            // Go through each inset type and update its layoutInsets from the
            // WindowInsetsCompat values
            windowInsets.statusBars.run {
                _layoutInsets.updateFrom(wic.getInsets(WindowInsetsCompat.Type.statusBars()))
                isVisible = wic.isVisible(WindowInsetsCompat.Type.statusBars())
            }
            windowInsets.navigationBars.run {
                _layoutInsets.updateFrom(wic.getInsets(WindowInsetsCompat.Type.navigationBars()))
                isVisible = wic.isVisible(WindowInsetsCompat.Type.navigationBars())
            }
            windowInsets.systemBars.run {
                _layoutInsets.updateFrom(wic.getInsets(WindowInsetsCompat.Type.systemBars()))
                isVisible = wic.isVisible(WindowInsetsCompat.Type.systemBars())
            }
            windowInsets.systemGestures.run {
                _layoutInsets.updateFrom(wic.getInsets(WindowInsetsCompat.Type.systemGestures()))
                isVisible = wic.isVisible(WindowInsetsCompat.Type.systemGestures())
            }
            windowInsets.ime.run {
                _layoutInsets.updateFrom(wic.getInsets(WindowInsetsCompat.Type.ime()))
                isVisible = wic.isVisible(WindowInsetsCompat.Type.ime())
            }
            windowInsets.displayCutout.run {
                _layoutInsets.updateFrom(wic.getInsets(WindowInsetsCompat.Type.displayCutout()))
                isVisible = wic.isVisible(WindowInsetsCompat.Type.displayCutout())
            }

            if (consumeWindowInsets) WindowInsetsCompat.CONSUMED else wic
        }

        // Add an OnAttachStateChangeListener to request an inset pass each time we're attached
        // to the window
        val attachListener = object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) = v.requestApplyInsets()
            override fun onViewDetachedFromWindow(v: View) = Unit
        }
        view.addOnAttachStateChangeListener(attachListener)

        if (view.isAttachedToWindow) {
            // If the view is already attached, we can request an inset pass now
            view.requestApplyInsets()
        }

        isObserving = true
    }

    /**
     * Removes any listeners from the [view] so that we no longer observe inset changes.
     *
     * This is only required to be called from hosts which have a shorter lifetime than the [view].
     * For example, if you're using [ViewWindowInsetObserver] from a `@Composable` function,
     * you should call [stop] from an `onDispose` block, like so:
     *
     * ```
     * DisposableEffect(view) {
     *     val observer = ViewWindowInsetObserver(view)
     *     // ...
     *     onDispose {
     *         observer.stop()
     *     }
     * }
     * ```
     *
     * Whereas if you're using this class from a fragment (or similar), it is not required to
     * call this function since it will live as least as longer as the view.
     */
    fun stop() {
        require(isObserving) {
            "stop() called, but this ViewWindowInsetObserver is not currently observing"
        }
        view.removeOnAttachStateChangeListener(attachListener)
        ViewCompat.setOnApplyWindowInsetsListener(view, null)
        isObserving = false
    }
}


/**
 * Applies any [WindowInsetsCompat] values to [AmbientWindowInsets], which are then available
 * within [content].
 *
 * If you're using this in fragments, you may wish to take a look at
 * [ViewWindowInsetObserver] for a more optimal solution.
 *
 * @param consumeWindowInsets Whether to consume any [WindowInsetsCompat]s which are dispatched to
 * the host view. Defaults to `true`.
 */
@Composable
fun ProvideWindowInsets(
    consumeWindowInsets: Boolean = true,
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val windowInsets = LocalWindowInsets.current

    DisposableEffect(view) {
        val observer = ViewWindowInsetObserver(view)
        observer.observeInto(
            windowInsets = windowInsets,
            consumeWindowInsets = consumeWindowInsets,
            windowInsetsAnimationsEnabled = false
        )
        onDispose {
            observer.stop()
        }
    }

    CompositionLocalProvider(LocalWindowInsets provides windowInsets) {
        content()
    }
}


/**
 * Applies any [WindowInsetsCompat] values to [LocalWindowInsets], which are then available
 * within [content].
 *
 * If you're using this in fragments, you may wish to take a look at
 * [ViewWindowInsetObserver] for a more optimal solution.
 *
 * @param windowInsetsAnimationsEnabled Whether to listen for [WindowInsetsAnimation]s, such as
 * IME animations.
 * @param consumeWindowInsets Whether to consume any [WindowInsetsCompat]s which are dispatched to
 * the host view. Defaults to `true`.
 */
@Composable
fun ProvideWindowInsets(
    windowInsetsAnimationsEnabled: Boolean,
    consumeWindowInsets: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val windowInsets = remember { WindowInsets() }

    DisposableEffect(view) {
        val observer = ViewWindowInsetObserver(view)
        observer.observeInto(
            windowInsets = windowInsets,
            consumeWindowInsets = consumeWindowInsets,
            windowInsetsAnimationsEnabled = windowInsetsAnimationsEnabled
        )
        onDispose {
            observer.stop()
        }
    }

    CompositionLocalProvider(LocalWindowInsets provides windowInsets) {
        content()
    }
}

/**
 * Updates our mutable state backed [InsetsType] from an Android system insets.
 */
private fun MutableInsets.updateFrom(insets: androidx.core.graphics.Insets) {
    left = insets.left
    top = insets.top
    right = insets.right
    bottom = insets.bottom
}

internal fun Insets.coerceEachDimensionAtLeast(minimumValue: InsetsType): Insets {
    // Fast path, no need to copy if `this` >= `other`
    if (left >= minimumValue.left && top >= minimumValue.top &&
        right >= minimumValue.right && bottom >= minimumValue.bottom) {
        return this
    }
    return copy(
        left = left.coerceAtLeast(minimumValue.left),
        top = top.coerceAtLeast(minimumValue.top),
        right = right.coerceAtLeast(minimumValue.right),
        bottom = bottom.coerceAtLeast(minimumValue.bottom),
    )
}

enum class HorizontalSide { Left, Right }
enum class VerticalSide { Top, Bottom }

/**
 * Apply additional space which matches the height of the status bars height along the top edge
 * of the content.
 */
fun Modifier.statusBarsPadding(): Modifier = composed {
    InsetsPaddingModifier(
        insetsType = LocalWindowInsets.current.statusBars,
        applyTop = true
    )
}

fun Modifier.displayCutoutPadding(): Modifier = composed {
    InsetsPaddingModifier(
        insetsType = LocalWindowInsets.current.displayCutout,
        applyLeft = true,
        applyRight = true
    )
}

fun Modifier.bottomBarPadding(): Modifier = composed {
    InsetsPaddingModifier(
        insetsType = LocalWindowInsets.current.systemBars,
        applyBottom = true
    )
}

fun Modifier.systemBarsPadding(
    enabled: Boolean = true
): Modifier = composed {
    InsetsPaddingModifier(
        insetsType = LocalWindowInsets.current.systemBars,
        applyLeft = enabled,
        applyTop = enabled,
        applyRight = enabled,
        applyBottom = enabled
    )
}

fun Modifier.navigationBarsPadding(
    bottom: Boolean = true,
    left: Boolean = true,
    right: Boolean = true
): Modifier = composed {
    InsetsPaddingModifier(
        insetsType = LocalWindowInsets.current.navigationBars,
        applyLeft = left,
        applyRight = right,
        applyBottom = bottom
    )
}

fun Modifier.imePadding(): Modifier = composed {
    InsetsPaddingModifier(
        insetsType = LocalWindowInsets.current.ime,
        applyLeft = true,
        applyRight = true,
        applyBottom = true,
    )
}

fun Modifier.navigationBarsWithImePadding(): Modifier = composed {
    InsetsPaddingModifier(
        insetsType = LocalWindowInsets.current.ime,
        minimumInsetsType = LocalWindowInsets.current.navigationBars,
        applyLeft = true,
        applyRight = true,
        applyBottom = true,
    )
}

private data class InsetsPaddingModifier(
    private val insetsType: InsetsType,
    private val minimumInsetsType: InsetsType? = null,
    private val applyLeft: Boolean = false,
    private val applyTop: Boolean = false,
    private val applyRight: Boolean = false,
    private val applyBottom: Boolean = false,
) : LayoutModifier {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val transformedInsets = if (minimumInsetsType != null) {
            // If we have a minimum insets, coerce each dimensions
            insetsType.coerceEachDimensionAtLeast(minimumInsetsType)
        } else insetsType

        val left = if (applyLeft) transformedInsets.left else 0
        val top = if (applyTop) transformedInsets.top else 0
        val right = if (applyRight) transformedInsets.right else 0
        val bottom = if (applyBottom) transformedInsets.bottom else 0
        val horizontal = left + right
        val vertical = top + bottom

        val placeable = measurable.measure(constraints.offset(-horizontal, -vertical))

        val width = (placeable.width + horizontal)
            .coerceIn(constraints.minWidth, constraints.maxWidth)
        val height = (placeable.height + vertical)
            .coerceIn(constraints.minHeight, constraints.maxHeight)
        return layout(width, height) {
            placeable.place(left, top)
        }
    }
}


/**
 * Returns the current insets converted into a [PaddingValues].
 *
 * @param start Whether to apply the inset on the start dimension.
 * @param top Whether to apply the inset on the top dimension.
 * @param end Whether to apply the inset on the end dimension.
 * @param bottom Whether to apply the inset on the bottom dimension.
 * @param additionalHorizontal Value to add to the start and end dimensions.
 * @param additionalVertical Value to add to the top and bottom dimensions.
 */
@Composable
inline fun InsetsType.toPaddingValues(
    start: Boolean = true,
    top: Boolean = true,
    end: Boolean = true,
    bottom: Boolean = true,
    additionalHorizontal: Dp = 0.dp,
    additionalVertical: Dp = 0.dp,
) = toPaddingValues(
    start = start,
    top = top,
    end = end,
    bottom = bottom,
    additionalStart = additionalHorizontal,
    additionalTop = additionalVertical,
    additionalEnd = additionalHorizontal,
    additionalBottom = additionalVertical
)


/**
 * Returns the current insets converted into a [PaddingValues].
 *
 * @param start Whether to apply the inset on the start dimension.
 * @param top Whether to apply the inset on the top dimension.
 * @param end Whether to apply the inset on the end dimension.
 * @param bottom Whether to apply the inset on the bottom dimension.
 * @param additionalStart Value to add to the start dimension.
 * @param additionalTop Value to add to the top dimension.
 * @param additionalEnd Value to add to the end dimension.
 * @param additionalBottom Value to add to the bottom dimension.
 */
@Composable
fun InsetsType.toPaddingValues(
    start: Boolean = true,
    top: Boolean = true,
    end: Boolean = true,
    bottom: Boolean = true,
    additionalStart: Dp = 0.dp,
    additionalTop: Dp = 0.dp,
    additionalEnd: Dp = 0.dp,
    additionalBottom: Dp = 0.dp,
): PaddingValues = with(LocalDensity.current) {
    val layoutDirection = LocalLayoutDirection.current
    PaddingValues(
        start = additionalStart + when {
            start && layoutDirection == LayoutDirection.Ltr -> this@toPaddingValues.left.toDp()
            start && layoutDirection == LayoutDirection.Rtl -> this@toPaddingValues.right.toDp()
            else -> 0.dp
        },
        top = additionalTop + when {
            top -> this@toPaddingValues.top.toDp()
            else -> 0.dp
        },
        end = additionalEnd + when {
            end && layoutDirection == LayoutDirection.Ltr -> this@toPaddingValues.right.toDp()
            end && layoutDirection == LayoutDirection.Rtl -> this@toPaddingValues.left.toDp()
            else -> 0.dp
        },
        bottom = additionalBottom + when {
            bottom -> this@toPaddingValues.bottom.toDp()
            else -> 0.dp
        }
    )
}


/**
 * Declare the height of the content to match the height of the status bars exactly.
 *
 * This is very handy when used with `Spacer` to push content below the status bars:
 * ```
 * Column {
 *     Spacer(Modifier.statusBarHeight())
 *
 *     // Content to be drawn below status bars (y-axis)
 * }
 * ```
 *
 * It's also useful when used to draw a scrim which matches the status bars:
 * ```
 * Spacer(
 *     Modifier.statusBarHeight()
 *         .fillMaxWidth()
 *         .drawBackground(MaterialTheme.colors.background.copy(alpha = 0.3f)
 * )
 * ```
 *
 * Internally this matches the behavior of the [Modifier.height] modifier.
 *
 * @param additional Any additional height to add to the status bars size.
 */
fun Modifier.statusBarsHeight(
    additional: Dp = 0.dp
): Modifier = composed {
    InsetsSizeModifier(
        insetsType = LocalWindowInsets.current.statusBars,
        heightSide = VerticalSide.Top,
        additionalHeight = additional
    )
}


/**
 * Declare the preferred height of the content to match the height of the navigation bars when
 * present at the bottom of the screen.
 *
 * This is very handy when used with `Spacer` to push content below the navigation bars:
 * ```
 * Column {
 *     // Content to be drawn above status bars (y-axis)
 *     Spacer(Modifier.navigationBarHeight())
 * }
 * ```
 *
 * It's also useful when used to draw a scrim which matches the navigation bars:
 * ```
 * Spacer(
 *     Modifier.navigationBarHeight()
 *         .fillMaxWidth()
 *         .drawBackground(MaterialTheme.colors.background.copy(alpha = 0.3f)
 * )
 * ```
 *
 * Internally this matches the behavior of the [Modifier.height] modifier.
 *
 * @param additional Any additional height to add to the status bars size.
 */
fun Modifier.navigationBarsHeight(
    additional: Dp = 0.dp
): Modifier = composed {
    InsetsSizeModifier(
        insetsType = LocalWindowInsets.current.navigationBars,
        heightSide = VerticalSide.Bottom,
        additionalHeight = additional
    )
}

/**
 * Declare the preferred width of the content to match the width of the navigation bars,
 * on the given [side].
 *
 * This is very handy when used with `Spacer` to push content inside from any vertical
 * navigation bars (typically when the device is in landscape):
 * ```
 * Row {
 *     Spacer(Modifier.navigationBarWidth(HorizontalSide.Left))
 *
 *     // Content to be inside the navigation bars (x-axis)
 *
 *     Spacer(Modifier.navigationBarWidth(HorizontalSide.Right))
 * }
 * ```
 *
 * It's also useful when used to draw a scrim which matches the navigation bars:
 * ```
 * Spacer(
 *     Modifier.navigationBarWidth(HorizontalSide.Left)
 *         .fillMaxHeight()
 *         .drawBackground(MaterialTheme.colors.background.copy(alpha = 0.3f)
 * )
 * ```
 *
 * Internally this matches the behavior of the [Modifier.height] modifier.
 *
 * @param side The navigation bar side to use as the source for the width.
 * @param additional Any additional width to add to the status bars size.
 */
fun Modifier.navigationBarsWidth(
    side: HorizontalSide,
    additional: Dp = 0.dp
): Modifier = composed {
    InsetsSizeModifier(
        insetsType = LocalWindowInsets.current.navigationBars,
        widthSide = side,
        additionalWidth = additional
    )
}

/**
 * [Modifier] class which powers the modifiers above. This is the lower level modifier which
 * supports the functionality through a number of parameters.
 *
 * We may make this public at some point. If you need this, please let us know via the
 * issue tracker.
 */
private data class InsetsSizeModifier(
    private val insetsType: InsetsType,
    private val widthSide: HorizontalSide? = null,
    private val additionalWidth: Dp = 0.dp,
    private val heightSide: VerticalSide? = null,
    private val additionalHeight: Dp = 0.dp
) : LayoutModifier {
    private val Density.targetConstraints: Constraints
        get() {
            val additionalWidthPx = additionalWidth.roundToPx()
            val additionalHeightPx = additionalHeight.roundToPx()
            return Constraints(
                minWidth = additionalWidthPx + when (widthSide) {
                    HorizontalSide.Left -> insetsType.left
                    HorizontalSide.Right -> insetsType.right
                    null -> 0
                },
                minHeight = additionalHeightPx + when (heightSide) {
                    VerticalSide.Top -> insetsType.top
                    VerticalSide.Bottom -> insetsType.bottom
                    null -> 0
                },
                maxWidth = when (widthSide) {
                    HorizontalSide.Left -> insetsType.left + additionalWidthPx
                    HorizontalSide.Right -> insetsType.right + additionalWidthPx
                    null -> Constraints.Infinity
                },
                maxHeight = when (heightSide) {
                    VerticalSide.Top -> insetsType.top + additionalHeightPx
                    VerticalSide.Bottom -> insetsType.bottom + additionalHeightPx
                    null -> Constraints.Infinity
                }
            )
        }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val wrappedConstraints = targetConstraints.let { targetConstraints ->
            val resolvedMinWidth = if (widthSide != null) {
                targetConstraints.minWidth
            } else {
                constraints.minWidth.coerceAtMost(targetConstraints.maxWidth)
            }
            val resolvedMaxWidth = if (widthSide != null) {
                targetConstraints.maxWidth
            } else {
                constraints.maxWidth.coerceAtLeast(targetConstraints.minWidth)
            }
            val resolvedMinHeight = if (heightSide != null) {
                targetConstraints.minHeight
            } else {
                constraints.minHeight.coerceAtMost(targetConstraints.maxHeight)
            }
            val resolvedMaxHeight = if (heightSide != null) {
                targetConstraints.maxHeight
            } else {
                constraints.maxHeight.coerceAtLeast(targetConstraints.minHeight)
            }
            Constraints(
                resolvedMinWidth,
                resolvedMaxWidth,
                resolvedMinHeight,
                resolvedMaxHeight
            )
        }
        val placeable = measurable.measure(wrappedConstraints)
        return layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ) = measurable.minIntrinsicWidth(height).let {
        val constraints = targetConstraints
        it.coerceIn(constraints.minWidth, constraints.maxWidth)
    }

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ) = measurable.maxIntrinsicWidth(height).let {
        val constraints = targetConstraints
        it.coerceIn(constraints.minWidth, constraints.maxWidth)
    }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ) = measurable.minIntrinsicHeight(width).let {
        val constraints = targetConstraints
        it.coerceIn(constraints.minHeight, constraints.maxHeight)
    }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ) = measurable.maxIntrinsicHeight(width).let {
        val constraints = targetConstraints
        it.coerceIn(constraints.minHeight, constraints.maxHeight)
    }
}
