
package com.san.kir.ankofork.appcompat

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.ViewManager
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.CheckedTextView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.MultiAutoCompleteTextView
import android.widget.RadioButton
import android.widget.RatingBar
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import com.san.kir.ankofork.AnkoViewDslMarker
import com.san.kir.ankofork.ankoView

@PublishedApi
internal object AnkoFactoriesAppcompatV7View {
    val TINTED_AUTO_COMPLETE_TEXT_VIEW = { ctx: Context ->
        if (Build.VERSION.SDK_INT < 21) androidx.appcompat.widget.AppCompatAutoCompleteTextView(ctx) else AutoCompleteTextView(
            ctx
        )
    }
    val TINTED_BUTTON = { ctx: Context ->
        if (Build.VERSION.SDK_INT < 21) androidx.appcompat.widget.AppCompatButton(ctx) else Button(
            ctx
        )
    }
    val TINTED_CHECK_BOX = { ctx: Context ->
        if (Build.VERSION.SDK_INT < 21) androidx.appcompat.widget.AppCompatCheckBox(ctx) else CheckBox(
            ctx
        )
    }
    val TINTED_CHECKED_TEXT_VIEW = { ctx: Context ->
        if (Build.VERSION.SDK_INT < 21) androidx.appcompat.widget.AppCompatCheckedTextView(ctx) else CheckedTextView(
            ctx
        )
    }
    val TINTED_EDIT_TEXT = { ctx: Context ->
        if (Build.VERSION.SDK_INT < 21) androidx.appcompat.widget.AppCompatEditText(ctx) else EditText(
            ctx
        )
    }
    val TINTED_IMAGE_BUTTON = { ctx: Context ->
        if (Build.VERSION.SDK_INT < 21) androidx.appcompat.widget.AppCompatImageButton(ctx) else ImageButton(
            ctx
        )
    }
    val TINTED_IMAGE_VIEW = { ctx: Context ->
        if (Build.VERSION.SDK_INT < 21) androidx.appcompat.widget.AppCompatImageView(ctx) else ImageView(
            ctx
        )
    }
    val TINTED_MULTI_AUTO_COMPLETE_TEXT_VIEW = { ctx: Context ->
        if (Build.VERSION.SDK_INT < 21) androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView(
            ctx
        ) else MultiAutoCompleteTextView(ctx)
    }
    val TINTED_RADIO_BUTTON = { ctx: Context ->
        if (Build.VERSION.SDK_INT < 21) androidx.appcompat.widget.AppCompatRadioButton(ctx) else RadioButton(
            ctx
        )
    }
    val TINTED_RATING_BAR = { ctx: Context ->
        if (Build.VERSION.SDK_INT < 21) androidx.appcompat.widget.AppCompatRatingBar(ctx) else RatingBar(
            ctx
        )
    }
    val TINTED_SEEK_BAR = { ctx: Context ->
        if (Build.VERSION.SDK_INT < 21) androidx.appcompat.widget.AppCompatSeekBar(ctx) else SeekBar(
            ctx
        )
    }
    val TINTED_SPINNER = { ctx: Context ->
        if (Build.VERSION.SDK_INT < 21) androidx.appcompat.widget.AppCompatSpinner(ctx) else Spinner(
            ctx
        )
    }
    val TINTED_TEXT_VIEW = { ctx: Context ->
        if (Build.VERSION.SDK_INT < 21) androidx.appcompat.widget.AppCompatTextView(ctx) else TextView(
            ctx
        )
    }
    val SEARCH_VIEW = { ctx: Context -> androidx.appcompat.widget.SearchView(ctx) }
    val SWITCH_COMPAT = { ctx: Context -> androidx.appcompat.widget.SwitchCompat(ctx) }
}

fun ViewManager.tintedAutoCompleteTextView(): AutoCompleteTextView =
    tintedAutoCompleteTextView {}

inline fun ViewManager.tintedAutoCompleteTextView(init: (@AnkoViewDslMarker AutoCompleteTextView).() -> Unit): AutoCompleteTextView {
    return ankoView(
        AnkoFactoriesAppcompatV7View.TINTED_AUTO_COMPLETE_TEXT_VIEW,
        theme = 0
    ) { init() }
}

fun ViewManager.themedTintedAutoCompleteTextView(theme: Int = 0): AutoCompleteTextView =
    themedTintedAutoCompleteTextView(theme) {}

inline fun ViewManager.themedTintedAutoCompleteTextView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker AutoCompleteTextView).() -> Unit
): AutoCompleteTextView {
    return ankoView(
        AnkoFactoriesAppcompatV7View.TINTED_AUTO_COMPLETE_TEXT_VIEW,
        theme
    ) { init() }
}

inline fun ViewManager.tintedButton(): Button = tintedButton {}
inline fun ViewManager.tintedButton(init: (@AnkoViewDslMarker Button).() -> Unit): Button {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_BUTTON, theme = 0) { init() }
}

inline fun ViewManager.themedTintedButton(theme: Int = 0): Button = themedTintedButton(theme) {}
inline fun ViewManager.themedTintedButton(
    theme: Int = 0,
    init: (@AnkoViewDslMarker Button).() -> Unit
): Button {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_BUTTON, theme) { init() }
}

inline fun ViewManager.tintedButton(text: CharSequence?): Button {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_BUTTON, theme = 0) {
        setText(text)
    }
}

inline fun ViewManager.tintedButton(
    text: CharSequence?,
    init: (@AnkoViewDslMarker Button).() -> Unit
): Button {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_BUTTON, theme = 0) {
        init()
        setText(text)
    }
}

inline fun ViewManager.themedTintedButton(text: CharSequence?, theme: Int): Button {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_BUTTON, theme) {
        setText(text)
    }
}

inline fun ViewManager.themedTintedButton(
    text: CharSequence?,
    theme: Int,
    init: (@AnkoViewDslMarker Button).() -> Unit
): Button {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_BUTTON, theme) {
        init()
        setText(text)
    }
}

inline fun ViewManager.tintedButton(text: Int): Button {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_BUTTON, theme = 0) {
        setText(text)
    }
}

inline fun ViewManager.tintedButton(
    text: Int,
    init: (@AnkoViewDslMarker Button).() -> Unit
): Button {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_BUTTON, theme = 0) {
        init()
        setText(text)
    }
}

inline fun ViewManager.themedTintedButton(text: Int, theme: Int): Button {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_BUTTON, theme) {
        setText(text)
    }
}

inline fun ViewManager.themedTintedButton(
    text: Int,
    theme: Int,
    init: (@AnkoViewDslMarker Button).() -> Unit
): Button {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_BUTTON, theme) {
        init()
        setText(text)
    }
}

inline fun ViewManager.tintedCheckBox(): CheckBox = tintedCheckBox {}
inline fun ViewManager.tintedCheckBox(init: (@AnkoViewDslMarker CheckBox).() -> Unit): CheckBox {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_CHECK_BOX, theme = 0) { init() }
}

inline fun ViewManager.themedTintedCheckBox(theme: Int = 0): CheckBox =
    themedTintedCheckBox(theme) {}

inline fun ViewManager.themedTintedCheckBox(
    theme: Int = 0,
    init: (@AnkoViewDslMarker CheckBox).() -> Unit
): CheckBox {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_CHECK_BOX, theme) { init() }
}

inline fun ViewManager.tintedCheckBox(text: CharSequence?): CheckBox {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_CHECK_BOX, theme = 0) {
        setText(text)
    }
}

inline fun ViewManager.tintedCheckBox(
    text: CharSequence?,
    init: (@AnkoViewDslMarker CheckBox).() -> Unit
): CheckBox {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_CHECK_BOX, theme = 0) {
        init()
        setText(text)
    }
}

inline fun ViewManager.themedTintedCheckBox(text: CharSequence?, theme: Int): CheckBox {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_CHECK_BOX, theme) {
        setText(text)
    }
}

inline fun ViewManager.themedTintedCheckBox(
    text: CharSequence?,
    theme: Int,
    init: (@AnkoViewDslMarker CheckBox).() -> Unit
): CheckBox {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_CHECK_BOX, theme) {
        init()
        setText(text)
    }
}

inline fun ViewManager.tintedCheckBox(text: Int): CheckBox {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_CHECK_BOX, theme = 0) {
        setText(text)
    }
}

inline fun ViewManager.tintedCheckBox(
    text: Int,
    init: (@AnkoViewDslMarker CheckBox).() -> Unit
): CheckBox {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_CHECK_BOX, theme = 0) {
        init()
        setText(text)
    }
}

inline fun ViewManager.themedTintedCheckBox(text: Int, theme: Int): CheckBox {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_CHECK_BOX, theme) {
        setText(text)
    }
}

inline fun ViewManager.themedTintedCheckBox(
    text: Int,
    theme: Int,
    init: (@AnkoViewDslMarker CheckBox).() -> Unit
): CheckBox {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_CHECK_BOX, theme) {
        init()
        setText(text)
    }
}

inline fun ViewManager.tintedCheckBox(text: CharSequence?, checked: Boolean): CheckBox {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_CHECK_BOX, theme = 0) {
        setText(text)
        isChecked = checked
    }
}

inline fun ViewManager.tintedCheckBox(
    text: CharSequence?,
    checked: Boolean,
    init: (@AnkoViewDslMarker CheckBox).() -> Unit
): CheckBox {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_CHECK_BOX, theme = 0) {
        init()
        setText(text)
        isChecked = checked
    }
}

inline fun ViewManager.themedTintedCheckBox(
    text: CharSequence?,
    checked: Boolean,
    theme: Int
): CheckBox {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_CHECK_BOX, theme) {
        setText(text)
        isChecked = checked
    }
}

inline fun ViewManager.themedTintedCheckBox(
    text: CharSequence?,
    checked: Boolean,
    theme: Int,
    init: (@AnkoViewDslMarker CheckBox).() -> Unit
): CheckBox {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_CHECK_BOX, theme) {
        init()
        setText(text)
        isChecked = checked
    }
}

inline fun ViewManager.tintedCheckBox(text: Int, checked: Boolean): CheckBox {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_CHECK_BOX, theme = 0) {
        setText(text)
        isChecked = checked
    }
}

inline fun ViewManager.tintedCheckBox(
    text: Int,
    checked: Boolean,
    init: (@AnkoViewDslMarker CheckBox).() -> Unit
): CheckBox {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_CHECK_BOX, theme = 0) {
        init()
        setText(text)
        isChecked = checked
    }
}

inline fun ViewManager.themedTintedCheckBox(text: Int, checked: Boolean, theme: Int): CheckBox {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_CHECK_BOX, theme) {
        setText(text)
        isChecked = checked
    }
}

inline fun ViewManager.themedTintedCheckBox(
    text: Int,
    checked: Boolean,
    theme: Int,
    init: (@AnkoViewDslMarker CheckBox).() -> Unit
): CheckBox {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_CHECK_BOX, theme) {
        init()
        setText(text)
        isChecked = checked
    }
}

inline fun ViewManager.tintedCheckedTextView(): CheckedTextView = tintedCheckedTextView {}
inline fun ViewManager.tintedCheckedTextView(init: (@AnkoViewDslMarker CheckedTextView).() -> Unit): CheckedTextView {
    return ankoView(
        AnkoFactoriesAppcompatV7View.TINTED_CHECKED_TEXT_VIEW,
        theme = 0
    ) { init() }
}

inline fun ViewManager.themedTintedCheckedTextView(theme: Int = 0): CheckedTextView =
    themedTintedCheckedTextView(theme) {}

inline fun ViewManager.themedTintedCheckedTextView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker CheckedTextView).() -> Unit
): CheckedTextView {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_CHECKED_TEXT_VIEW, theme) { init() }
}

inline fun ViewManager.tintedEditText(): EditText = tintedEditText {}
inline fun ViewManager.tintedEditText(init: (@AnkoViewDslMarker EditText).() -> Unit): EditText {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_EDIT_TEXT, theme = 0) { init() }
}

inline fun ViewManager.themedTintedEditText(theme: Int = 0): EditText =
    themedTintedEditText(theme) {}

inline fun ViewManager.themedTintedEditText(
    theme: Int = 0,
    init: (@AnkoViewDslMarker EditText).() -> Unit
): EditText {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_EDIT_TEXT, theme) { init() }
}

inline fun ViewManager.tintedEditText(text: CharSequence?): EditText {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_EDIT_TEXT, theme = 0) {
        setText(text)
    }
}

inline fun ViewManager.tintedEditText(
    text: CharSequence?,
    init: (@AnkoViewDslMarker EditText).() -> Unit
): EditText {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_EDIT_TEXT, theme = 0) {
        init()
        setText(text)
    }
}

inline fun ViewManager.themedTintedEditText(text: CharSequence?, theme: Int): EditText {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_EDIT_TEXT, theme) {
        setText(text)
    }
}

inline fun ViewManager.themedTintedEditText(
    text: CharSequence?,
    theme: Int,
    init: (@AnkoViewDslMarker EditText).() -> Unit
): EditText {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_EDIT_TEXT, theme) {
        init()
        setText(text)
    }
}

inline fun ViewManager.tintedEditText(text: Int): EditText {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_EDIT_TEXT, theme = 0) {
        setText(text)
    }
}

inline fun ViewManager.tintedEditText(
    text: Int,
    init: (@AnkoViewDslMarker EditText).() -> Unit
): EditText {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_EDIT_TEXT, theme = 0) {
        init()
        setText(text)
    }
}

inline fun ViewManager.themedTintedEditText(text: Int, theme: Int): EditText {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_EDIT_TEXT, theme) {
        setText(text)
    }
}

inline fun ViewManager.themedTintedEditText(
    text: Int,
    theme: Int,
    init: (@AnkoViewDslMarker EditText).() -> Unit
): EditText {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_EDIT_TEXT, theme) {
        init()
        setText(text)
    }
}

inline fun ViewManager.tintedImageButton(): ImageButton = tintedImageButton {}
inline fun ViewManager.tintedImageButton(init: (@AnkoViewDslMarker ImageButton).() -> Unit): ImageButton {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_IMAGE_BUTTON, theme = 0) { init() }
}

inline fun ViewManager.themedTintedImageButton(theme: Int = 0): ImageButton =
    themedTintedImageButton(theme) {}

inline fun ViewManager.themedTintedImageButton(
    theme: Int = 0,
    init: (@AnkoViewDslMarker ImageButton).() -> Unit
): ImageButton {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_IMAGE_BUTTON, theme) { init() }
}

inline fun ViewManager.tintedImageButton(imageDrawable: android.graphics.drawable.Drawable?): ImageButton {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_IMAGE_BUTTON, theme = 0) {
        setImageDrawable(imageDrawable)
    }
}

inline fun ViewManager.tintedImageButton(
    imageDrawable: android.graphics.drawable.Drawable?,
    init: (@AnkoViewDslMarker ImageButton).() -> Unit
): ImageButton {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_IMAGE_BUTTON, theme = 0) {
        init()
        setImageDrawable(imageDrawable)
    }
}

inline fun ViewManager.themedTintedImageButton(
    imageDrawable: android.graphics.drawable.Drawable?,
    theme: Int
): ImageButton {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_IMAGE_BUTTON, theme) {
        setImageDrawable(imageDrawable)
    }
}

inline fun ViewManager.themedTintedImageButton(
    imageDrawable: android.graphics.drawable.Drawable?,
    theme: Int,
    init: (@AnkoViewDslMarker ImageButton).() -> Unit
): ImageButton {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_IMAGE_BUTTON, theme) {
        init()
        setImageDrawable(imageDrawable)
    }
}

inline fun ViewManager.tintedImageButton(imageResource: Int): ImageButton {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_IMAGE_BUTTON, theme = 0) {
        setImageResource(imageResource)
    }
}

inline fun ViewManager.tintedImageButton(
    imageResource: Int,
    init: (@AnkoViewDslMarker ImageButton).() -> Unit
): ImageButton {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_IMAGE_BUTTON, theme = 0) {
        init()
        setImageResource(imageResource)
    }
}

inline fun ViewManager.themedTintedImageButton(imageResource: Int, theme: Int): ImageButton {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_IMAGE_BUTTON, theme) {
        setImageResource(imageResource)
    }
}

inline fun ViewManager.themedTintedImageButton(
    imageResource: Int,
    theme: Int,
    init: (@AnkoViewDslMarker ImageButton).() -> Unit
): ImageButton {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_IMAGE_BUTTON, theme) {
        init()
        setImageResource(imageResource)
    }
}

inline fun ViewManager.tintedImageView(): ImageView = tintedImageView {}
inline fun ViewManager.tintedImageView(init: (@AnkoViewDslMarker ImageView).() -> Unit): ImageView {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_IMAGE_VIEW, theme = 0) { init() }
}

inline fun ViewManager.themedTintedImageView(theme: Int = 0): ImageView =
    themedTintedImageView(theme) {}

inline fun ViewManager.themedTintedImageView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker ImageView).() -> Unit
): ImageView {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_IMAGE_VIEW, theme) { init() }
}

inline fun ViewManager.tintedImageView(imageDrawable: android.graphics.drawable.Drawable?): ImageView {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_IMAGE_VIEW, theme = 0) {
        setImageDrawable(imageDrawable)
    }
}

inline fun ViewManager.tintedImageView(
    imageDrawable: android.graphics.drawable.Drawable?,
    init: (@AnkoViewDslMarker ImageView).() -> Unit
): ImageView {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_IMAGE_VIEW, theme = 0) {
        init()
        setImageDrawable(imageDrawable)
    }
}

inline fun ViewManager.themedTintedImageView(
    imageDrawable: android.graphics.drawable.Drawable?,
    theme: Int
): ImageView {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_IMAGE_VIEW, theme) {
        setImageDrawable(imageDrawable)
    }
}

inline fun ViewManager.themedTintedImageView(
    imageDrawable: android.graphics.drawable.Drawable?,
    theme: Int,
    init: (@AnkoViewDslMarker ImageView).() -> Unit
): ImageView {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_IMAGE_VIEW, theme) {
        init()
        setImageDrawable(imageDrawable)
    }
}

inline fun ViewManager.tintedImageView(imageResource: Int): ImageView {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_IMAGE_VIEW, theme = 0) {
        setImageResource(imageResource)
    }
}

inline fun ViewManager.tintedImageView(
    imageResource: Int,
    init: (@AnkoViewDslMarker ImageView).() -> Unit
): ImageView {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_IMAGE_VIEW, theme = 0) {
        init()
        setImageResource(imageResource)
    }
}

inline fun ViewManager.themedTintedImageView(imageResource: Int, theme: Int): ImageView {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_IMAGE_VIEW, theme) {
        setImageResource(imageResource)
    }
}

inline fun ViewManager.themedTintedImageView(
    imageResource: Int,
    theme: Int,
    init: (@AnkoViewDslMarker ImageView).() -> Unit
): ImageView {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_IMAGE_VIEW, theme) {
        init()
        setImageResource(imageResource)
    }
}

inline fun ViewManager.tintedMultiAutoCompleteTextView(): MultiAutoCompleteTextView =
    tintedMultiAutoCompleteTextView {}

inline fun ViewManager.tintedMultiAutoCompleteTextView(init: (@AnkoViewDslMarker MultiAutoCompleteTextView).() -> Unit): MultiAutoCompleteTextView {
    return ankoView(
        AnkoFactoriesAppcompatV7View.TINTED_MULTI_AUTO_COMPLETE_TEXT_VIEW,
        theme = 0
    ) { init() }
}

inline fun ViewManager.themedTintedMultiAutoCompleteTextView(theme: Int = 0): MultiAutoCompleteTextView =
    themedTintedMultiAutoCompleteTextView(theme) {}

inline fun ViewManager.themedTintedMultiAutoCompleteTextView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker MultiAutoCompleteTextView).() -> Unit
): MultiAutoCompleteTextView {
    return ankoView(
        AnkoFactoriesAppcompatV7View.TINTED_MULTI_AUTO_COMPLETE_TEXT_VIEW,
        theme
    ) { init() }
}

inline fun ViewManager.tintedRadioButton(): RadioButton = tintedRadioButton {}
inline fun ViewManager.tintedRadioButton(init: (@AnkoViewDslMarker RadioButton).() -> Unit): RadioButton {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_RADIO_BUTTON, theme = 0) { init() }
}

inline fun ViewManager.themedTintedRadioButton(theme: Int = 0): RadioButton =
    themedTintedRadioButton(theme) {}

inline fun ViewManager.themedTintedRadioButton(
    theme: Int = 0,
    init: (@AnkoViewDslMarker RadioButton).() -> Unit
): RadioButton {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_RADIO_BUTTON, theme) { init() }
}

inline fun ViewManager.tintedRatingBar(): RatingBar = tintedRatingBar {}
inline fun ViewManager.tintedRatingBar(init: (@AnkoViewDslMarker RatingBar).() -> Unit): RatingBar {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_RATING_BAR, theme = 0) { init() }
}

inline fun ViewManager.themedTintedRatingBar(theme: Int = 0): RatingBar =
    themedTintedRatingBar(theme) {}

inline fun ViewManager.themedTintedRatingBar(
    theme: Int = 0,
    init: (@AnkoViewDslMarker RatingBar).() -> Unit
): RatingBar {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_RATING_BAR, theme) { init() }
}

inline fun ViewManager.tintedSeekBar(): SeekBar = tintedSeekBar {}
inline fun ViewManager.tintedSeekBar(init: (@AnkoViewDslMarker SeekBar).() -> Unit): SeekBar {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_SEEK_BAR, theme = 0) { init() }
}

inline fun ViewManager.themedTintedSeekBar(theme: Int = 0): SeekBar = themedTintedSeekBar(theme) {}
inline fun ViewManager.themedTintedSeekBar(
    theme: Int = 0,
    init: (@AnkoViewDslMarker SeekBar).() -> Unit
): SeekBar {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_SEEK_BAR, theme) { init() }
}

inline fun ViewManager.tintedSpinner(): Spinner = tintedSpinner {}
inline fun ViewManager.tintedSpinner(init: (@AnkoViewDslMarker Spinner).() -> Unit): Spinner {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_SPINNER, theme = 0) { init() }
}

inline fun ViewManager.themedTintedSpinner(theme: Int = 0): Spinner = themedTintedSpinner(theme) {}
inline fun ViewManager.themedTintedSpinner(
    theme: Int = 0,
    init: (@AnkoViewDslMarker Spinner).() -> Unit
): Spinner {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_SPINNER, theme) { init() }
}

inline fun Context.tintedSpinner(): Spinner = tintedSpinner {}
inline fun Context.tintedSpinner(init: (@AnkoViewDslMarker Spinner).() -> Unit): Spinner {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_SPINNER, theme = 0) { init() }
}

inline fun Context.themedTintedSpinner(theme: Int = 0): Spinner = themedTintedSpinner(theme) {}
inline fun Context.themedTintedSpinner(
    theme: Int = 0,
    init: (@AnkoViewDslMarker Spinner).() -> Unit
): Spinner {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_SPINNER, theme) { init() }
}

inline fun Activity.tintedSpinner(): Spinner = tintedSpinner {}
inline fun Activity.tintedSpinner(init: (@AnkoViewDslMarker Spinner).() -> Unit): Spinner {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_SPINNER, theme = 0) { init() }
}

inline fun Activity.themedTintedSpinner(theme: Int = 0): Spinner = themedTintedSpinner(theme) {}
inline fun Activity.themedTintedSpinner(
    theme: Int = 0,
    init: (@AnkoViewDslMarker Spinner).() -> Unit
): Spinner {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_SPINNER, theme) { init() }
}

inline fun ViewManager.tintedTextView(): TextView = tintedTextView {}
inline fun ViewManager.tintedTextView(init: (@AnkoViewDslMarker TextView).() -> Unit): TextView {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_TEXT_VIEW, theme = 0) { init() }
}

inline fun ViewManager.themedTintedTextView(theme: Int = 0): TextView =
    themedTintedTextView(theme) {}

inline fun ViewManager.themedTintedTextView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker TextView).() -> Unit
): TextView {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_TEXT_VIEW, theme) { init() }
}

inline fun ViewManager.tintedTextView(text: CharSequence?): TextView {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_TEXT_VIEW, theme = 0) {
        setText(text)
    }
}

inline fun ViewManager.tintedTextView(
    text: CharSequence?,
    init: (@AnkoViewDslMarker TextView).() -> Unit
): TextView {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_TEXT_VIEW, theme = 0) {
        init()
        setText(text)
    }
}

inline fun ViewManager.themedTintedTextView(text: CharSequence?, theme: Int): TextView {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_TEXT_VIEW, theme) {
        setText(text)
    }
}

inline fun ViewManager.themedTintedTextView(
    text: CharSequence?,
    theme: Int,
    init: (@AnkoViewDslMarker TextView).() -> Unit
): TextView {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_TEXT_VIEW, theme) {
        init()
        setText(text)
    }
}

inline fun ViewManager.tintedTextView(text: Int): TextView {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_TEXT_VIEW, theme = 0) {
        setText(text)
    }
}

inline fun ViewManager.tintedTextView(
    text: Int,
    init: (@AnkoViewDslMarker TextView).() -> Unit
): TextView {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_TEXT_VIEW, theme = 0) {
        init()
        setText(text)
    }
}

inline fun ViewManager.themedTintedTextView(text: Int, theme: Int): TextView {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_TEXT_VIEW, theme) {
        setText(text)
    }
}

inline fun ViewManager.themedTintedTextView(
    text: Int,
    theme: Int,
    init: (@AnkoViewDslMarker TextView).() -> Unit
): TextView {
    return ankoView(AnkoFactoriesAppcompatV7View.TINTED_TEXT_VIEW, theme) {
        init()
        setText(text)
    }
}


inline fun ViewManager.searchView(): androidx.appcompat.widget.SearchView = searchView {}
inline fun ViewManager.searchView(init: (@AnkoViewDslMarker androidx.appcompat.widget.SearchView).() -> Unit): androidx.appcompat.widget.SearchView {
    return ankoView(AnkoFactoriesAppcompatV7View.SEARCH_VIEW, theme = 0) { init() }
}

inline fun ViewManager.themedSearchView(theme: Int = 0): androidx.appcompat.widget.SearchView =
    themedSearchView(theme) {}

inline fun ViewManager.themedSearchView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker androidx.appcompat.widget.SearchView).() -> Unit
): androidx.appcompat.widget.SearchView {
    return ankoView(AnkoFactoriesAppcompatV7View.SEARCH_VIEW, theme) { init() }
}

inline fun Context.searchView(): androidx.appcompat.widget.SearchView = searchView {}
inline fun Context.searchView(init: (@AnkoViewDslMarker androidx.appcompat.widget.SearchView).() -> Unit): androidx.appcompat.widget.SearchView {
    return ankoView(AnkoFactoriesAppcompatV7View.SEARCH_VIEW, theme = 0) { init() }
}

inline fun Context.themedSearchView(theme: Int = 0): androidx.appcompat.widget.SearchView =
    themedSearchView(theme) {}

inline fun Context.themedSearchView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker androidx.appcompat.widget.SearchView).() -> Unit
): androidx.appcompat.widget.SearchView {
    return ankoView(AnkoFactoriesAppcompatV7View.SEARCH_VIEW, theme) { init() }
}

inline fun Activity.searchView(): androidx.appcompat.widget.SearchView = searchView {}
inline fun Activity.searchView(init: (@AnkoViewDslMarker androidx.appcompat.widget.SearchView).() -> Unit): androidx.appcompat.widget.SearchView {
    return ankoView(AnkoFactoriesAppcompatV7View.SEARCH_VIEW, theme = 0) { init() }
}

inline fun Activity.themedSearchView(theme: Int = 0): androidx.appcompat.widget.SearchView =
    themedSearchView(theme) {}

inline fun Activity.themedSearchView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker androidx.appcompat.widget.SearchView).() -> Unit
): androidx.appcompat.widget.SearchView {
    return ankoView(AnkoFactoriesAppcompatV7View.SEARCH_VIEW, theme) { init() }
}

inline fun ViewManager.switchCompat(): androidx.appcompat.widget.SwitchCompat = switchCompat {}
inline fun ViewManager.switchCompat(init: (@AnkoViewDslMarker androidx.appcompat.widget.SwitchCompat).() -> Unit): androidx.appcompat.widget.SwitchCompat {
    return ankoView(AnkoFactoriesAppcompatV7View.SWITCH_COMPAT, theme = 0) { init() }
}

inline fun ViewManager.themedSwitchCompat(theme: Int = 0): androidx.appcompat.widget.SwitchCompat =
    themedSwitchCompat(theme) {}

inline fun ViewManager.themedSwitchCompat(
    theme: Int = 0,
    init: (@AnkoViewDslMarker androidx.appcompat.widget.SwitchCompat).() -> Unit
): androidx.appcompat.widget.SwitchCompat {
    return ankoView(AnkoFactoriesAppcompatV7View.SWITCH_COMPAT, theme) { init() }
}

@PublishedApi
internal object AnkoFactoriesAppcompatV7ViewGroup {
    val LINEAR_LAYOUT_COMPAT = { ctx: Context -> _LinearLayoutCompat(ctx) }
    val TOOLBAR = { ctx: Context -> _Toolbar(ctx) }
}

inline fun ViewManager.linearLayoutCompat(): androidx.appcompat.widget.LinearLayoutCompat =
    linearLayoutCompat {}

inline fun ViewManager.linearLayoutCompat(init: (@AnkoViewDslMarker _LinearLayoutCompat).() -> Unit): androidx.appcompat.widget.LinearLayoutCompat {
    return ankoView(
        AnkoFactoriesAppcompatV7ViewGroup.LINEAR_LAYOUT_COMPAT,
        theme = 0
    ) { init() }
}

inline fun ViewManager.themedLinearLayoutCompat(theme: Int = 0): androidx.appcompat.widget.LinearLayoutCompat =
    themedLinearLayoutCompat(theme) {}

inline fun ViewManager.themedLinearLayoutCompat(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _LinearLayoutCompat).() -> Unit
): androidx.appcompat.widget.LinearLayoutCompat {
    return ankoView(AnkoFactoriesAppcompatV7ViewGroup.LINEAR_LAYOUT_COMPAT, theme) { init() }
}

inline fun Context.linearLayoutCompat(): androidx.appcompat.widget.LinearLayoutCompat =
    linearLayoutCompat {}

inline fun Context.linearLayoutCompat(init: (@AnkoViewDslMarker _LinearLayoutCompat).() -> Unit): androidx.appcompat.widget.LinearLayoutCompat {
    return ankoView(
        AnkoFactoriesAppcompatV7ViewGroup.LINEAR_LAYOUT_COMPAT,
        theme = 0
    ) { init() }
}

inline fun Context.themedLinearLayoutCompat(theme: Int = 0): androidx.appcompat.widget.LinearLayoutCompat =
    themedLinearLayoutCompat(theme) {}

inline fun Context.themedLinearLayoutCompat(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _LinearLayoutCompat).() -> Unit
): androidx.appcompat.widget.LinearLayoutCompat {
    return ankoView(AnkoFactoriesAppcompatV7ViewGroup.LINEAR_LAYOUT_COMPAT, theme) { init() }
}

inline fun Activity.linearLayoutCompat(): androidx.appcompat.widget.LinearLayoutCompat =
    linearLayoutCompat {}

inline fun Activity.linearLayoutCompat(init: (@AnkoViewDslMarker _LinearLayoutCompat).() -> Unit): androidx.appcompat.widget.LinearLayoutCompat {
    return ankoView(
        AnkoFactoriesAppcompatV7ViewGroup.LINEAR_LAYOUT_COMPAT,
        theme = 0
    ) { init() }
}

inline fun Activity.themedLinearLayoutCompat(theme: Int = 0): androidx.appcompat.widget.LinearLayoutCompat =
    themedLinearLayoutCompat(theme) {}

inline fun Activity.themedLinearLayoutCompat(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _LinearLayoutCompat).() -> Unit
): androidx.appcompat.widget.LinearLayoutCompat {
    return ankoView(AnkoFactoriesAppcompatV7ViewGroup.LINEAR_LAYOUT_COMPAT, theme) { init() }
}

inline fun ViewManager.toolbar(): androidx.appcompat.widget.Toolbar = toolbar {}
inline fun ViewManager.toolbar(init: (@AnkoViewDslMarker _Toolbar).() -> Unit): androidx.appcompat.widget.Toolbar {
    return ankoView(AnkoFactoriesAppcompatV7ViewGroup.TOOLBAR, theme = 0) { init() }
}

inline fun ViewManager.themedToolbar(theme: Int = 0): androidx.appcompat.widget.Toolbar =
    themedToolbar(theme) {}

inline fun ViewManager.themedToolbar(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _Toolbar).() -> Unit
): androidx.appcompat.widget.Toolbar {
    return ankoView(AnkoFactoriesAppcompatV7ViewGroup.TOOLBAR, theme) { init() }
}

inline fun Context.toolbar(): androidx.appcompat.widget.Toolbar = toolbar {}
inline fun Context.toolbar(init: (@AnkoViewDslMarker _Toolbar).() -> Unit): androidx.appcompat.widget.Toolbar {
    return ankoView(AnkoFactoriesAppcompatV7ViewGroup.TOOLBAR, theme = 0) { init() }
}

inline fun Context.themedToolbar(theme: Int = 0): androidx.appcompat.widget.Toolbar =
    themedToolbar(theme) {}

inline fun Context.themedToolbar(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _Toolbar).() -> Unit
): androidx.appcompat.widget.Toolbar {
    return ankoView(AnkoFactoriesAppcompatV7ViewGroup.TOOLBAR, theme) { init() }
}

inline fun Activity.toolbar(): androidx.appcompat.widget.Toolbar = toolbar {}
inline fun Activity.toolbar(init: (@AnkoViewDslMarker _Toolbar).() -> Unit): androidx.appcompat.widget.Toolbar {
    return ankoView(AnkoFactoriesAppcompatV7ViewGroup.TOOLBAR, theme = 0) { init() }
}

inline fun Activity.themedToolbar(theme: Int = 0): androidx.appcompat.widget.Toolbar =
    themedToolbar(theme) {}

inline fun Activity.themedToolbar(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _Toolbar).() -> Unit
): androidx.appcompat.widget.Toolbar {
    return ankoView(AnkoFactoriesAppcompatV7ViewGroup.TOOLBAR, theme) { init() }
}

