package com.san.kir.ankofork.sdk28

import android.app.Activity
import android.content.Context
import android.view.ViewManager
import android.widget.TextView
import com.san.kir.ankofork.AnkoViewDslMarker
import com.san.kir.ankofork.Binder
import com.san.kir.ankofork.ankoView

@PublishedApi
internal object AnkoFactoriesSdk28View {
    val MEDIA_ROUTE_BUTTON = { ctx: Context -> android.app.MediaRouteButton(ctx) }
    val EXTRACT_EDIT_TEXT = { ctx: Context -> android.inputmethodservice.ExtractEditText(ctx) }
    val VIEW = { ctx: Context -> android.view.View(ctx) }
    val ADAPTER_VIEW_FLIPPER = { ctx: Context -> android.widget.AdapterViewFlipper(ctx) }
    val AUTO_COMPLETE_TEXT_VIEW = { ctx: Context -> android.widget.AutoCompleteTextView(ctx) }
    val BUTTON = { ctx: Context -> android.widget.Button(ctx) }
    val CHECK_BOX = { ctx: Context -> android.widget.CheckBox(ctx) }
    val CHECKED_TEXT_VIEW = { ctx: Context -> android.widget.CheckedTextView(ctx) }
    val EDIT_TEXT = { ctx: Context -> android.widget.EditText(ctx) }
    val IMAGE_BUTTON = { ctx: Context -> android.widget.ImageButton(ctx) }
    val IMAGE_VIEW = { ctx: Context -> android.widget.ImageView(ctx) }
    val LIST_VIEW = { ctx: Context -> android.widget.ListView(ctx) }
    val MULTI_AUTO_COMPLETE_TEXT_VIEW =
        { ctx: Context -> android.widget.MultiAutoCompleteTextView(ctx) }
    val PROGRESS_BAR = { ctx: Context -> android.widget.ProgressBar(ctx) }
    val RADIO_BUTTON = { ctx: Context -> android.widget.RadioButton(ctx) }
    val RATING_BAR = { ctx: Context -> android.widget.RatingBar(ctx) }
    val SEARCH_VIEW = { ctx: Context -> android.widget.SearchView(ctx) }
    val SEEK_BAR = { ctx: Context -> android.widget.SeekBar(ctx) }
    val SPACE = { ctx: Context -> android.widget.Space(ctx) }
    val SPINNER = { ctx: Context -> android.widget.Spinner(ctx) }
    val STACK_VIEW = { ctx: Context -> android.widget.StackView(ctx) }
    val SWITCH = { ctx: Context -> android.widget.Switch(ctx) }
    val TAB_HOST = { ctx: Context -> android.widget.TabHost(ctx) }
    val TAB_WIDGET = { ctx: Context -> android.widget.TabWidget(ctx) }
    val TEXT_CLOCK = { ctx: Context -> android.widget.TextClock(ctx) }
    val TEXT_VIEW = { ctx: Context -> TextView(ctx) }
    val TIME_PICKER = { ctx: Context -> android.widget.TimePicker(ctx) }
    val TOGGLE_BUTTON = { ctx: Context -> android.widget.ToggleButton(ctx) }
    val VIEW_FLIPPER = { ctx: Context -> android.widget.ViewFlipper(ctx) }
}

inline fun ViewManager.mediaRouteButton(): android.app.MediaRouteButton = mediaRouteButton {}
inline fun ViewManager.mediaRouteButton(init: (@AnkoViewDslMarker android.app.MediaRouteButton).() -> Unit): android.app.MediaRouteButton {
    return ankoView(AnkoFactoriesSdk28View.MEDIA_ROUTE_BUTTON, theme = 0) { init() }
}

inline fun ViewManager.themedMediaRouteButton(theme: Int = 0): android.app.MediaRouteButton =
    themedMediaRouteButton(theme) {}

inline fun ViewManager.themedMediaRouteButton(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.app.MediaRouteButton).() -> Unit
): android.app.MediaRouteButton {
    return ankoView(AnkoFactoriesSdk28View.MEDIA_ROUTE_BUTTON, theme) { init() }
}


inline fun ViewManager.extractEditText(): android.inputmethodservice.ExtractEditText =
    extractEditText {}

inline fun ViewManager.extractEditText(init: (@AnkoViewDslMarker android.inputmethodservice.ExtractEditText).() -> Unit): android.inputmethodservice.ExtractEditText {
    return ankoView(AnkoFactoriesSdk28View.EXTRACT_EDIT_TEXT, theme = 0) { init() }
}

inline fun ViewManager.themedExtractEditText(theme: Int = 0): android.inputmethodservice.ExtractEditText =
    themedExtractEditText(theme) {}

inline fun ViewManager.themedExtractEditText(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.inputmethodservice.ExtractEditText).() -> Unit
): android.inputmethodservice.ExtractEditText {
    return ankoView(AnkoFactoriesSdk28View.EXTRACT_EDIT_TEXT, theme) { init() }
}


inline fun ViewManager.view(): android.view.View = view {}
inline fun ViewManager.view(init: (@AnkoViewDslMarker android.view.View).() -> Unit): android.view.View {
    return ankoView(AnkoFactoriesSdk28View.VIEW, theme = 0) { init() }
}

inline fun ViewManager.themedView(theme: Int = 0): android.view.View = themedView(theme) {}
inline fun ViewManager.themedView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.view.View).() -> Unit
): android.view.View {
    return ankoView(AnkoFactoriesSdk28View.VIEW, theme) { init() }
}

inline fun ViewManager.adapterViewFlipper(): android.widget.AdapterViewFlipper =
    adapterViewFlipper {}

inline fun ViewManager.adapterViewFlipper(init: (@AnkoViewDslMarker android.widget.AdapterViewFlipper).() -> Unit): android.widget.AdapterViewFlipper {
    return ankoView(AnkoFactoriesSdk28View.ADAPTER_VIEW_FLIPPER, theme = 0) { init() }
}

inline fun ViewManager.themedAdapterViewFlipper(theme: Int = 0): android.widget.AdapterViewFlipper =
    themedAdapterViewFlipper(theme) {}

inline fun ViewManager.themedAdapterViewFlipper(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.AdapterViewFlipper).() -> Unit
): android.widget.AdapterViewFlipper {
    return ankoView(AnkoFactoriesSdk28View.ADAPTER_VIEW_FLIPPER, theme) { init() }
}

inline fun Context.adapterViewFlipper(): android.widget.AdapterViewFlipper = adapterViewFlipper {}
inline fun Context.adapterViewFlipper(init: (@AnkoViewDslMarker android.widget.AdapterViewFlipper).() -> Unit): android.widget.AdapterViewFlipper {
    return ankoView(AnkoFactoriesSdk28View.ADAPTER_VIEW_FLIPPER, theme = 0) { init() }
}

inline fun Context.themedAdapterViewFlipper(theme: Int = 0): android.widget.AdapterViewFlipper =
    themedAdapterViewFlipper(theme) {}

inline fun Context.themedAdapterViewFlipper(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.AdapterViewFlipper).() -> Unit
): android.widget.AdapterViewFlipper {
    return ankoView(AnkoFactoriesSdk28View.ADAPTER_VIEW_FLIPPER, theme) { init() }
}

inline fun Activity.adapterViewFlipper(): android.widget.AdapterViewFlipper = adapterViewFlipper {}
inline fun Activity.adapterViewFlipper(init: (@AnkoViewDslMarker android.widget.AdapterViewFlipper).() -> Unit): android.widget.AdapterViewFlipper {
    return ankoView(AnkoFactoriesSdk28View.ADAPTER_VIEW_FLIPPER, theme = 0) { init() }
}

inline fun Activity.themedAdapterViewFlipper(theme: Int = 0): android.widget.AdapterViewFlipper =
    themedAdapterViewFlipper(theme) {}

inline fun Activity.themedAdapterViewFlipper(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.AdapterViewFlipper).() -> Unit
): android.widget.AdapterViewFlipper {
    return ankoView(AnkoFactoriesSdk28View.ADAPTER_VIEW_FLIPPER, theme) { init() }
}

inline fun ViewManager.autoCompleteTextView(): android.widget.AutoCompleteTextView =
    autoCompleteTextView {}

inline fun ViewManager.autoCompleteTextView(init: (@AnkoViewDslMarker android.widget.AutoCompleteTextView).() -> Unit): android.widget.AutoCompleteTextView {
    return ankoView(AnkoFactoriesSdk28View.AUTO_COMPLETE_TEXT_VIEW, theme = 0) { init() }
}

inline fun ViewManager.themedAutoCompleteTextView(theme: Int = 0): android.widget.AutoCompleteTextView =
    themedAutoCompleteTextView(theme) {}

inline fun ViewManager.themedAutoCompleteTextView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.AutoCompleteTextView).() -> Unit
): android.widget.AutoCompleteTextView {
    return ankoView(AnkoFactoriesSdk28View.AUTO_COMPLETE_TEXT_VIEW, theme) { init() }
}

inline fun ViewManager.button(): android.widget.Button = button {}
inline fun ViewManager.button(init: (@AnkoViewDslMarker android.widget.Button).() -> Unit): android.widget.Button {
    return ankoView(AnkoFactoriesSdk28View.BUTTON, theme = 0) { init() }
}

inline fun ViewManager.themedButton(theme: Int = 0): android.widget.Button = themedButton(theme) {}
inline fun ViewManager.themedButton(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.Button).() -> Unit
): android.widget.Button {
    return ankoView(AnkoFactoriesSdk28View.BUTTON, theme) { init() }
}

inline fun ViewManager.button(text: CharSequence?): android.widget.Button {
    return ankoView(AnkoFactoriesSdk28View.BUTTON, theme = 0) {
        setText(text)
    }
}

inline fun ViewManager.button(
    text: CharSequence?,
    init: (@AnkoViewDslMarker android.widget.Button).() -> Unit
): android.widget.Button {
    return ankoView(AnkoFactoriesSdk28View.BUTTON, theme = 0) {
        init()
        setText(text)
    }
}

inline fun ViewManager.themedButton(text: CharSequence?, theme: Int): android.widget.Button {
    return ankoView(AnkoFactoriesSdk28View.BUTTON, theme) {
        setText(text)
    }
}

inline fun ViewManager.themedButton(
    text: CharSequence?,
    theme: Int,
    init: (@AnkoViewDslMarker android.widget.Button).() -> Unit
): android.widget.Button {
    return ankoView(AnkoFactoriesSdk28View.BUTTON, theme) {
        init()
        setText(text)
    }
}

inline fun ViewManager.button(text: Int): android.widget.Button {
    return ankoView(AnkoFactoriesSdk28View.BUTTON, theme = 0) {
        setText(text)
    }
}

inline fun ViewManager.button(
    text: Int,
    init: (@AnkoViewDslMarker android.widget.Button).() -> Unit
): android.widget.Button {
    return ankoView(AnkoFactoriesSdk28View.BUTTON, theme = 0) {
        init()
        setText(text)
    }
}

inline fun ViewManager.themedButton(text: Int, theme: Int): android.widget.Button {
    return ankoView(AnkoFactoriesSdk28View.BUTTON, theme) {
        setText(text)
    }
}

inline fun ViewManager.themedButton(
    text: Int,
    theme: Int,
    init: (@AnkoViewDslMarker android.widget.Button).() -> Unit
): android.widget.Button {
    return ankoView(AnkoFactoriesSdk28View.BUTTON, theme) {
        init()
        setText(text)
    }
}

inline fun ViewManager.checkBox(): android.widget.CheckBox = checkBox {}
inline fun ViewManager.checkBox(init: (@AnkoViewDslMarker android.widget.CheckBox).() -> Unit): android.widget.CheckBox {
    return ankoView(AnkoFactoriesSdk28View.CHECK_BOX, theme = 0) { init() }
}

inline fun ViewManager.themedCheckBox(theme: Int = 0): android.widget.CheckBox =
    themedCheckBox(theme) {}

inline fun ViewManager.themedCheckBox(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.CheckBox).() -> Unit
): android.widget.CheckBox {
    return ankoView(AnkoFactoriesSdk28View.CHECK_BOX, theme) { init() }
}

inline fun ViewManager.checkBox(text: CharSequence?): android.widget.CheckBox {
    return ankoView(AnkoFactoriesSdk28View.CHECK_BOX, theme = 0) {
        setText(text)
    }
}

inline fun ViewManager.checkBox(
    text: CharSequence?,
    init: (@AnkoViewDslMarker android.widget.CheckBox).() -> Unit
): android.widget.CheckBox {
    return ankoView(AnkoFactoriesSdk28View.CHECK_BOX, theme = 0) {
        init()
        setText(text)
    }
}

inline fun ViewManager.themedCheckBox(text: CharSequence?, theme: Int): android.widget.CheckBox {
    return ankoView(AnkoFactoriesSdk28View.CHECK_BOX, theme) {
        setText(text)
    }
}

inline fun ViewManager.themedCheckBox(
    text: CharSequence?,
    theme: Int,
    init: (@AnkoViewDslMarker android.widget.CheckBox).() -> Unit
): android.widget.CheckBox {
    return ankoView(AnkoFactoriesSdk28View.CHECK_BOX, theme) {
        init()
        setText(text)
    }
}

inline fun ViewManager.checkBox(text: Int): android.widget.CheckBox {
    return ankoView(AnkoFactoriesSdk28View.CHECK_BOX, theme = 0) {
        setText(text)
    }
}

inline fun ViewManager.checkBox(
    text: Int,
    init: (@AnkoViewDslMarker android.widget.CheckBox).() -> Unit
): android.widget.CheckBox {
    return ankoView(AnkoFactoriesSdk28View.CHECK_BOX, theme = 0) {
        init()
        setText(text)
    }
}

inline fun ViewManager.themedCheckBox(text: Int, theme: Int): android.widget.CheckBox {
    return ankoView(AnkoFactoriesSdk28View.CHECK_BOX, theme) {
        setText(text)
    }
}

inline fun ViewManager.themedCheckBox(
    text: Int,
    theme: Int,
    init: (@AnkoViewDslMarker android.widget.CheckBox).() -> Unit
): android.widget.CheckBox {
    return ankoView(AnkoFactoriesSdk28View.CHECK_BOX, theme) {
        init()
        setText(text)
    }
}

inline fun ViewManager.checkBox(text: CharSequence?, checked: Boolean): android.widget.CheckBox {
    return ankoView(AnkoFactoriesSdk28View.CHECK_BOX, theme = 0) {
        setText(text)
        isChecked = checked
    }
}

inline fun ViewManager.checkBox(
    text: CharSequence?,
    checked: Boolean,
    init: (@AnkoViewDslMarker android.widget.CheckBox).() -> Unit
): android.widget.CheckBox {
    return ankoView(AnkoFactoriesSdk28View.CHECK_BOX, theme = 0) {
        init()
        setText(text)
        isChecked = checked
    }
}

inline fun ViewManager.themedCheckBox(
    text: CharSequence?,
    checked: Boolean,
    theme: Int
): android.widget.CheckBox {
    return ankoView(AnkoFactoriesSdk28View.CHECK_BOX, theme) {
        setText(text)
        isChecked = checked
    }
}

inline fun ViewManager.themedCheckBox(
    text: CharSequence?,
    checked: Boolean,
    theme: Int,
    init: (@AnkoViewDslMarker android.widget.CheckBox).() -> Unit
): android.widget.CheckBox {
    return ankoView(AnkoFactoriesSdk28View.CHECK_BOX, theme) {
        init()
        setText(text)
        isChecked = checked
    }
}

inline fun ViewManager.checkBox(text: Int, checked: Boolean): android.widget.CheckBox {
    return ankoView(AnkoFactoriesSdk28View.CHECK_BOX, theme = 0) {
        setText(text)
        isChecked = checked
    }
}

inline fun ViewManager.checkBox(
    text: Int,
    checked: Boolean,
    init: (@AnkoViewDslMarker android.widget.CheckBox).() -> Unit
): android.widget.CheckBox {
    return ankoView(AnkoFactoriesSdk28View.CHECK_BOX, theme = 0) {
        init()
        setText(text)
        isChecked = checked
    }
}

inline fun ViewManager.themedCheckBox(
    text: Int,
    checked: Boolean,
    theme: Int
): android.widget.CheckBox {
    return ankoView(AnkoFactoriesSdk28View.CHECK_BOX, theme) {
        setText(text)
        isChecked = checked
    }
}

inline fun ViewManager.themedCheckBox(
    text: Int,
    checked: Boolean,
    theme: Int,
    init: (@AnkoViewDslMarker android.widget.CheckBox).() -> Unit
): android.widget.CheckBox {
    return ankoView(AnkoFactoriesSdk28View.CHECK_BOX, theme) {
        init()
        setText(text)
        isChecked = checked
    }
}

inline fun ViewManager.checkedTextView(): android.widget.CheckedTextView = checkedTextView {}
inline fun ViewManager.checkedTextView(init: (@AnkoViewDslMarker android.widget.CheckedTextView).() -> Unit): android.widget.CheckedTextView {
    return ankoView(AnkoFactoriesSdk28View.CHECKED_TEXT_VIEW, theme = 0) { init() }
}

inline fun ViewManager.themedCheckedTextView(theme: Int = 0): android.widget.CheckedTextView =
    themedCheckedTextView(theme) {}

inline fun ViewManager.themedCheckedTextView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.CheckedTextView).() -> Unit
): android.widget.CheckedTextView {
    return ankoView(AnkoFactoriesSdk28View.CHECKED_TEXT_VIEW, theme) { init() }
}

inline fun ViewManager.editText(): android.widget.EditText = editText {}
inline fun ViewManager.editText(init: (@AnkoViewDslMarker android.widget.EditText).() -> Unit): android.widget.EditText {
    return ankoView(AnkoFactoriesSdk28View.EDIT_TEXT, theme = 0) { init() }
}

inline fun ViewManager.themedEditText(theme: Int = 0): android.widget.EditText =
    themedEditText(theme) {}

inline fun ViewManager.themedEditText(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.EditText).() -> Unit
): android.widget.EditText {
    return ankoView(AnkoFactoriesSdk28View.EDIT_TEXT, theme) { init() }
}

inline fun ViewManager.editText(text: CharSequence?): android.widget.EditText {
    return ankoView(AnkoFactoriesSdk28View.EDIT_TEXT, theme = 0) {
        setText(text)
    }
}

inline fun ViewManager.editText(
    text: CharSequence?,
    init: (@AnkoViewDslMarker android.widget.EditText).() -> Unit
): android.widget.EditText {
    return ankoView(AnkoFactoriesSdk28View.EDIT_TEXT, theme = 0) {
        init()
        setText(text)
    }
}

inline fun ViewManager.themedEditText(text: CharSequence?, theme: Int): android.widget.EditText {
    return ankoView(AnkoFactoriesSdk28View.EDIT_TEXT, theme) {
        setText(text)
    }
}

inline fun ViewManager.themedEditText(
    text: CharSequence?,
    theme: Int,
    init: (@AnkoViewDslMarker android.widget.EditText).() -> Unit
): android.widget.EditText {
    return ankoView(AnkoFactoriesSdk28View.EDIT_TEXT, theme) {
        init()
        setText(text)
    }
}

inline fun ViewManager.editText(text: Int): android.widget.EditText {
    return ankoView(AnkoFactoriesSdk28View.EDIT_TEXT, theme = 0) {
        setText(text)
    }
}

inline fun ViewManager.editText(
    text: Int,
    init: (@AnkoViewDslMarker android.widget.EditText).() -> Unit
): android.widget.EditText {
    return ankoView(AnkoFactoriesSdk28View.EDIT_TEXT, theme = 0) {
        init()
        setText(text)
    }
}

inline fun ViewManager.themedEditText(text: Int, theme: Int): android.widget.EditText {
    return ankoView(AnkoFactoriesSdk28View.EDIT_TEXT, theme) {
        setText(text)
    }
}

inline fun ViewManager.themedEditText(
    text: Int,
    theme: Int,
    init: (@AnkoViewDslMarker android.widget.EditText).() -> Unit
): android.widget.EditText {
    return ankoView(AnkoFactoriesSdk28View.EDIT_TEXT, theme) {
        init()
        setText(text)
    }
}

inline fun ViewManager.imageButton(): android.widget.ImageButton = imageButton {}
inline fun ViewManager.imageButton(init: (@AnkoViewDslMarker android.widget.ImageButton).() -> Unit): android.widget.ImageButton {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_BUTTON, theme = 0) { init() }
}

inline fun ViewManager.themedImageButton(theme: Int = 0): android.widget.ImageButton =
    themedImageButton(theme) {}

inline fun ViewManager.themedImageButton(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.ImageButton).() -> Unit
): android.widget.ImageButton {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_BUTTON, theme) { init() }
}

inline fun ViewManager.imageButton(imageDrawable: android.graphics.drawable.Drawable?): android.widget.ImageButton {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_BUTTON, theme = 0) {
        setImageDrawable(imageDrawable)
    }
}

inline fun ViewManager.imageButton(
    imageDrawable: android.graphics.drawable.Drawable?,
    init: (@AnkoViewDslMarker android.widget.ImageButton).() -> Unit
): android.widget.ImageButton {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_BUTTON, theme = 0) {
        init()
        setImageDrawable(imageDrawable)
    }
}

inline fun ViewManager.themedImageButton(
    imageDrawable: android.graphics.drawable.Drawable?,
    theme: Int
): android.widget.ImageButton {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_BUTTON, theme) {
        setImageDrawable(imageDrawable)
    }
}

inline fun ViewManager.themedImageButton(
    imageDrawable: android.graphics.drawable.Drawable?,
    theme: Int,
    init: (@AnkoViewDslMarker android.widget.ImageButton).() -> Unit
): android.widget.ImageButton {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_BUTTON, theme) {
        init()
        setImageDrawable(imageDrawable)
    }
}

inline fun ViewManager.imageButton(imageResource: Int): android.widget.ImageButton {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_BUTTON, theme = 0) {
        setImageResource(imageResource)
    }
}

inline fun ViewManager.imageButton(
    imageResource: Int,
    init: (@AnkoViewDslMarker android.widget.ImageButton).() -> Unit
): android.widget.ImageButton {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_BUTTON, theme = 0) {
        init()
        setImageResource(imageResource)
    }
}

inline fun ViewManager.themedImageButton(
    imageResource: Int,
    theme: Int
): android.widget.ImageButton {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_BUTTON, theme) {
        setImageResource(imageResource)
    }
}

inline fun ViewManager.themedImageButton(
    imageResource: Int,
    theme: Int,
    init: (@AnkoViewDslMarker android.widget.ImageButton).() -> Unit
): android.widget.ImageButton {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_BUTTON, theme) {
        init()
        setImageResource(imageResource)
    }
}

inline fun ViewManager.imageView(): android.widget.ImageView = imageView {}
inline fun ViewManager.imageView(init: (@AnkoViewDslMarker android.widget.ImageView).() -> Unit): android.widget.ImageView {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_VIEW, theme = 0) { init() }
}

inline fun ViewManager.themedImageView(theme: Int = 0): android.widget.ImageView =
    themedImageView(theme) {}

inline fun ViewManager.themedImageView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.ImageView).() -> Unit
): android.widget.ImageView {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_VIEW, theme) { init() }
}

inline fun ViewManager.imageView(imageDrawable: android.graphics.drawable.Drawable?): android.widget.ImageView {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_VIEW, theme = 0) {
        setImageDrawable(imageDrawable)
    }
}

inline fun ViewManager.imageView(
    imageDrawable: android.graphics.drawable.Drawable?,
    init: (@AnkoViewDslMarker android.widget.ImageView).() -> Unit
): android.widget.ImageView {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_VIEW, theme = 0) {
        init()
        setImageDrawable(imageDrawable)
    }
}

inline fun ViewManager.themedImageView(
    imageDrawable: android.graphics.drawable.Drawable?,
    theme: Int
): android.widget.ImageView {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_VIEW, theme) {
        setImageDrawable(imageDrawable)
    }
}

inline fun ViewManager.themedImageView(
    imageDrawable: android.graphics.drawable.Drawable?,
    theme: Int,
    init: (@AnkoViewDslMarker android.widget.ImageView).() -> Unit
): android.widget.ImageView {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_VIEW, theme) {
        init()
        setImageDrawable(imageDrawable)
    }
}

inline fun ViewManager.imageView(imageResource: Int): android.widget.ImageView {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_VIEW, theme = 0) {
        setImageResource(imageResource)
    }
}

inline fun ViewManager.imageView(
    imageResource: Int,
    init: (@AnkoViewDslMarker android.widget.ImageView).() -> Unit
): android.widget.ImageView {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_VIEW, theme = 0) {
        init()
        setImageResource(imageResource)
    }
}

inline fun ViewManager.themedImageView(imageResource: Int, theme: Int): android.widget.ImageView {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_VIEW, theme) {
        setImageResource(imageResource)
    }
}

inline fun ViewManager.themedImageView(
    imageResource: Int,
    theme: Int,
    init: (@AnkoViewDslMarker android.widget.ImageView).() -> Unit
): android.widget.ImageView {
    return ankoView(AnkoFactoriesSdk28View.IMAGE_VIEW, theme) {
        init()
        setImageResource(imageResource)
    }
}

inline fun ViewManager.listView(): android.widget.ListView = listView {}
inline fun ViewManager.listView(init: (@AnkoViewDslMarker android.widget.ListView).() -> Unit): android.widget.ListView {
    return ankoView(AnkoFactoriesSdk28View.LIST_VIEW, theme = 0) { init() }
}

inline fun ViewManager.themedListView(theme: Int = 0): android.widget.ListView =
    themedListView(theme) {}

inline fun ViewManager.themedListView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.ListView).() -> Unit
): android.widget.ListView {
    return ankoView(AnkoFactoriesSdk28View.LIST_VIEW, theme) { init() }
}

inline fun Context.listView(): android.widget.ListView = listView {}
inline fun Context.listView(init: (@AnkoViewDslMarker android.widget.ListView).() -> Unit): android.widget.ListView {
    return ankoView(AnkoFactoriesSdk28View.LIST_VIEW, theme = 0) { init() }
}

inline fun Context.themedListView(theme: Int = 0): android.widget.ListView =
    themedListView(theme) {}

inline fun Context.themedListView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.ListView).() -> Unit
): android.widget.ListView {
    return ankoView(AnkoFactoriesSdk28View.LIST_VIEW, theme) { init() }
}

inline fun Activity.listView(): android.widget.ListView = listView {}
inline fun Activity.listView(init: (@AnkoViewDslMarker android.widget.ListView).() -> Unit): android.widget.ListView {
    return ankoView(AnkoFactoriesSdk28View.LIST_VIEW, theme = 0) { init() }
}

inline fun Activity.themedListView(theme: Int = 0): android.widget.ListView =
    themedListView(theme) {}

inline fun Activity.themedListView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.ListView).() -> Unit
): android.widget.ListView {
    return ankoView(AnkoFactoriesSdk28View.LIST_VIEW, theme) { init() }
}

inline fun ViewManager.multiAutoCompleteTextView(): android.widget.MultiAutoCompleteTextView =
    multiAutoCompleteTextView {}

inline fun ViewManager.multiAutoCompleteTextView(init: (@AnkoViewDslMarker android.widget.MultiAutoCompleteTextView).() -> Unit): android.widget.MultiAutoCompleteTextView {
    return ankoView(AnkoFactoriesSdk28View.MULTI_AUTO_COMPLETE_TEXT_VIEW, theme = 0) { init() }
}

inline fun ViewManager.themedMultiAutoCompleteTextView(theme: Int = 0): android.widget.MultiAutoCompleteTextView =
    themedMultiAutoCompleteTextView(theme) {}

inline fun ViewManager.themedMultiAutoCompleteTextView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.MultiAutoCompleteTextView).() -> Unit
): android.widget.MultiAutoCompleteTextView {
    return ankoView(AnkoFactoriesSdk28View.MULTI_AUTO_COMPLETE_TEXT_VIEW, theme) { init() }
}

inline fun ViewManager.progressBar(): android.widget.ProgressBar = progressBar {}
inline fun ViewManager.progressBar(init: (@AnkoViewDslMarker android.widget.ProgressBar).() -> Unit): android.widget.ProgressBar {
    return ankoView(AnkoFactoriesSdk28View.PROGRESS_BAR, theme = 0) { init() }
}

inline fun ViewManager.themedProgressBar(theme: Int = 0): android.widget.ProgressBar =
    themedProgressBar(theme) {}

inline fun ViewManager.themedProgressBar(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.ProgressBar).() -> Unit
): android.widget.ProgressBar {
    return ankoView(AnkoFactoriesSdk28View.PROGRESS_BAR, theme) { init() }
}

inline fun ViewManager.radioButton(): android.widget.RadioButton = radioButton {}
inline fun ViewManager.radioButton(init: (@AnkoViewDslMarker android.widget.RadioButton).() -> Unit): android.widget.RadioButton {
    return ankoView(AnkoFactoriesSdk28View.RADIO_BUTTON, theme = 0) { init() }
}

inline fun ViewManager.themedRadioButton(theme: Int = 0): android.widget.RadioButton =
    themedRadioButton(theme) {}

inline fun ViewManager.themedRadioButton(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.RadioButton).() -> Unit
): android.widget.RadioButton {
    return ankoView(AnkoFactoriesSdk28View.RADIO_BUTTON, theme) { init() }
}

inline fun ViewManager.ratingBar(): android.widget.RatingBar = ratingBar {}
inline fun ViewManager.ratingBar(init: (@AnkoViewDslMarker android.widget.RatingBar).() -> Unit): android.widget.RatingBar {
    return ankoView(AnkoFactoriesSdk28View.RATING_BAR, theme = 0) { init() }
}

inline fun ViewManager.themedRatingBar(theme: Int = 0): android.widget.RatingBar =
    themedRatingBar(theme) {}

inline fun ViewManager.themedRatingBar(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.RatingBar).() -> Unit
): android.widget.RatingBar {
    return ankoView(AnkoFactoriesSdk28View.RATING_BAR, theme) { init() }
}

inline fun ViewManager.searchView(): android.widget.SearchView = searchView {}
inline fun ViewManager.searchView(init: (@AnkoViewDslMarker android.widget.SearchView).() -> Unit): android.widget.SearchView {
    return ankoView(AnkoFactoriesSdk28View.SEARCH_VIEW, theme = 0) { init() }
}

inline fun ViewManager.themedSearchView(theme: Int = 0): android.widget.SearchView =
    themedSearchView(theme) {}

inline fun ViewManager.themedSearchView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.SearchView).() -> Unit
): android.widget.SearchView {
    return ankoView(AnkoFactoriesSdk28View.SEARCH_VIEW, theme) { init() }
}

inline fun Context.searchView(): android.widget.SearchView = searchView {}
inline fun Context.searchView(init: (@AnkoViewDslMarker android.widget.SearchView).() -> Unit): android.widget.SearchView {
    return ankoView(AnkoFactoriesSdk28View.SEARCH_VIEW, theme = 0) { init() }
}

inline fun Context.themedSearchView(theme: Int = 0): android.widget.SearchView =
    themedSearchView(theme) {}

inline fun Context.themedSearchView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.SearchView).() -> Unit
): android.widget.SearchView {
    return ankoView(AnkoFactoriesSdk28View.SEARCH_VIEW, theme) { init() }
}

inline fun Activity.searchView(): android.widget.SearchView = searchView {}
inline fun Activity.searchView(init: (@AnkoViewDslMarker android.widget.SearchView).() -> Unit): android.widget.SearchView {
    return ankoView(AnkoFactoriesSdk28View.SEARCH_VIEW, theme = 0) { init() }
}

inline fun Activity.themedSearchView(theme: Int = 0): android.widget.SearchView =
    themedSearchView(theme) {}

inline fun Activity.themedSearchView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.SearchView).() -> Unit
): android.widget.SearchView {
    return ankoView(AnkoFactoriesSdk28View.SEARCH_VIEW, theme) { init() }
}

inline fun ViewManager.seekBar(): android.widget.SeekBar = seekBar {}
inline fun ViewManager.seekBar(init: (@AnkoViewDslMarker android.widget.SeekBar).() -> Unit): android.widget.SeekBar {
    return ankoView(AnkoFactoriesSdk28View.SEEK_BAR, theme = 0) { init() }
}

inline fun ViewManager.themedSeekBar(theme: Int = 0): android.widget.SeekBar =
    themedSeekBar(theme) {}

inline fun ViewManager.themedSeekBar(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.SeekBar).() -> Unit
): android.widget.SeekBar {
    return ankoView(AnkoFactoriesSdk28View.SEEK_BAR, theme) { init() }
}

inline fun ViewManager.space(): android.widget.Space = space {}
inline fun ViewManager.space(init: (@AnkoViewDslMarker android.widget.Space).() -> Unit): android.widget.Space {
    return ankoView(AnkoFactoriesSdk28View.SPACE, theme = 0) { init() }
}

inline fun ViewManager.themedSpace(theme: Int = 0): android.widget.Space = themedSpace(theme) {}
inline fun ViewManager.themedSpace(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.Space).() -> Unit
): android.widget.Space {
    return ankoView(AnkoFactoriesSdk28View.SPACE, theme) { init() }
}

inline fun ViewManager.spinner(): android.widget.Spinner = spinner {}
inline fun ViewManager.spinner(init: (@AnkoViewDslMarker android.widget.Spinner).() -> Unit): android.widget.Spinner {
    return ankoView(AnkoFactoriesSdk28View.SPINNER, theme = 0) { init() }
}

inline fun ViewManager.themedSpinner(theme: Int = 0): android.widget.Spinner =
    themedSpinner(theme) {}

inline fun ViewManager.themedSpinner(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.Spinner).() -> Unit
): android.widget.Spinner {
    return ankoView(AnkoFactoriesSdk28View.SPINNER, theme) { init() }
}

inline fun Context.spinner(): android.widget.Spinner = spinner {}
inline fun Context.spinner(init: (@AnkoViewDslMarker android.widget.Spinner).() -> Unit): android.widget.Spinner {
    return ankoView(AnkoFactoriesSdk28View.SPINNER, theme = 0) { init() }
}

inline fun Context.themedSpinner(theme: Int = 0): android.widget.Spinner = themedSpinner(theme) {}
inline fun Context.themedSpinner(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.Spinner).() -> Unit
): android.widget.Spinner {
    return ankoView(AnkoFactoriesSdk28View.SPINNER, theme) { init() }
}

inline fun Activity.spinner(): android.widget.Spinner = spinner {}
inline fun Activity.spinner(init: (@AnkoViewDslMarker android.widget.Spinner).() -> Unit): android.widget.Spinner {
    return ankoView(AnkoFactoriesSdk28View.SPINNER, theme = 0) { init() }
}

inline fun Activity.themedSpinner(theme: Int = 0): android.widget.Spinner = themedSpinner(theme) {}
inline fun Activity.themedSpinner(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.Spinner).() -> Unit
): android.widget.Spinner {
    return ankoView(AnkoFactoriesSdk28View.SPINNER, theme) { init() }
}

inline fun ViewManager.stackView(): android.widget.StackView = stackView {}
inline fun ViewManager.stackView(init: (@AnkoViewDslMarker android.widget.StackView).() -> Unit): android.widget.StackView {
    return ankoView(AnkoFactoriesSdk28View.STACK_VIEW, theme = 0) { init() }
}

inline fun ViewManager.themedStackView(theme: Int = 0): android.widget.StackView =
    themedStackView(theme) {}

inline fun ViewManager.themedStackView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.StackView).() -> Unit
): android.widget.StackView {
    return ankoView(AnkoFactoriesSdk28View.STACK_VIEW, theme) { init() }
}

inline fun Context.stackView(): android.widget.StackView = stackView {}
inline fun Context.stackView(init: (@AnkoViewDslMarker android.widget.StackView).() -> Unit): android.widget.StackView {
    return ankoView(AnkoFactoriesSdk28View.STACK_VIEW, theme = 0) { init() }
}

inline fun Context.themedStackView(theme: Int = 0): android.widget.StackView =
    themedStackView(theme) {}

inline fun Context.themedStackView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.StackView).() -> Unit
): android.widget.StackView {
    return ankoView(AnkoFactoriesSdk28View.STACK_VIEW, theme) { init() }
}

inline fun Activity.stackView(): android.widget.StackView = stackView {}
inline fun Activity.stackView(init: (@AnkoViewDslMarker android.widget.StackView).() -> Unit): android.widget.StackView {
    return ankoView(AnkoFactoriesSdk28View.STACK_VIEW, theme = 0) { init() }
}

inline fun Activity.themedStackView(theme: Int = 0): android.widget.StackView =
    themedStackView(theme) {}

inline fun Activity.themedStackView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.StackView).() -> Unit
): android.widget.StackView {
    return ankoView(AnkoFactoriesSdk28View.STACK_VIEW, theme) { init() }
}

inline fun ViewManager.switch(): android.widget.Switch = switch {}
inline fun ViewManager.switch(init: (@AnkoViewDslMarker android.widget.Switch).() -> Unit): android.widget.Switch {
    return ankoView(AnkoFactoriesSdk28View.SWITCH, theme = 0) { init() }
}

inline fun ViewManager.themedSwitch(theme: Int = 0): android.widget.Switch = themedSwitch(theme) {}
inline fun ViewManager.themedSwitch(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.Switch).() -> Unit
): android.widget.Switch {
    return ankoView(AnkoFactoriesSdk28View.SWITCH, theme) { init() }
}

inline fun ViewManager.tabHost(): android.widget.TabHost = tabHost {}
inline fun ViewManager.tabHost(init: (@AnkoViewDslMarker android.widget.TabHost).() -> Unit): android.widget.TabHost {
    return ankoView(AnkoFactoriesSdk28View.TAB_HOST, theme = 0) { init() }
}

inline fun ViewManager.themedTabHost(theme: Int = 0): android.widget.TabHost =
    themedTabHost(theme) {}

inline fun ViewManager.themedTabHost(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.TabHost).() -> Unit
): android.widget.TabHost {
    return ankoView(AnkoFactoriesSdk28View.TAB_HOST, theme) { init() }
}

inline fun Context.tabHost(): android.widget.TabHost = tabHost {}
inline fun Context.tabHost(init: (@AnkoViewDslMarker android.widget.TabHost).() -> Unit): android.widget.TabHost {
    return ankoView(AnkoFactoriesSdk28View.TAB_HOST, theme = 0) { init() }
}

inline fun Context.themedTabHost(theme: Int = 0): android.widget.TabHost = themedTabHost(theme) {}
inline fun Context.themedTabHost(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.TabHost).() -> Unit
): android.widget.TabHost {
    return ankoView(AnkoFactoriesSdk28View.TAB_HOST, theme) { init() }
}

inline fun Activity.tabHost(): android.widget.TabHost = tabHost {}
inline fun Activity.tabHost(init: (@AnkoViewDslMarker android.widget.TabHost).() -> Unit): android.widget.TabHost {
    return ankoView(AnkoFactoriesSdk28View.TAB_HOST, theme = 0) { init() }
}

inline fun Activity.themedTabHost(theme: Int = 0): android.widget.TabHost = themedTabHost(theme) {}
inline fun Activity.themedTabHost(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.TabHost).() -> Unit
): android.widget.TabHost {
    return ankoView(AnkoFactoriesSdk28View.TAB_HOST, theme) { init() }
}

inline fun ViewManager.tabWidget(): android.widget.TabWidget = tabWidget {}
inline fun ViewManager.tabWidget(init: (@AnkoViewDslMarker android.widget.TabWidget).() -> Unit): android.widget.TabWidget {
    return ankoView(AnkoFactoriesSdk28View.TAB_WIDGET, theme = 0) { init() }
}

inline fun ViewManager.themedTabWidget(theme: Int = 0): android.widget.TabWidget =
    themedTabWidget(theme) {}

inline fun ViewManager.themedTabWidget(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.TabWidget).() -> Unit
): android.widget.TabWidget {
    return ankoView(AnkoFactoriesSdk28View.TAB_WIDGET, theme) { init() }
}

inline fun Context.tabWidget(): android.widget.TabWidget = tabWidget {}
inline fun Context.tabWidget(init: (@AnkoViewDslMarker android.widget.TabWidget).() -> Unit): android.widget.TabWidget {
    return ankoView(AnkoFactoriesSdk28View.TAB_WIDGET, theme = 0) { init() }
}

inline fun Context.themedTabWidget(theme: Int = 0): android.widget.TabWidget =
    themedTabWidget(theme) {}

inline fun Context.themedTabWidget(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.TabWidget).() -> Unit
): android.widget.TabWidget {
    return ankoView(AnkoFactoriesSdk28View.TAB_WIDGET, theme) { init() }
}

inline fun Activity.tabWidget(): android.widget.TabWidget = tabWidget {}
inline fun Activity.tabWidget(init: (@AnkoViewDslMarker android.widget.TabWidget).() -> Unit): android.widget.TabWidget {
    return ankoView(AnkoFactoriesSdk28View.TAB_WIDGET, theme = 0) { init() }
}

inline fun Activity.themedTabWidget(theme: Int = 0): android.widget.TabWidget =
    themedTabWidget(theme) {}

inline fun Activity.themedTabWidget(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.TabWidget).() -> Unit
): android.widget.TabWidget {
    return ankoView(AnkoFactoriesSdk28View.TAB_WIDGET, theme) { init() }
}

inline fun ViewManager.textClock(): android.widget.TextClock = textClock {}
inline fun ViewManager.textClock(init: (@AnkoViewDslMarker android.widget.TextClock).() -> Unit): android.widget.TextClock {
    return ankoView(AnkoFactoriesSdk28View.TEXT_CLOCK, theme = 0) { init() }
}

inline fun ViewManager.themedTextClock(theme: Int = 0): android.widget.TextClock =
    themedTextClock(theme) {}

inline fun ViewManager.themedTextClock(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.TextClock).() -> Unit
): android.widget.TextClock {
    return ankoView(AnkoFactoriesSdk28View.TEXT_CLOCK, theme) { init() }
}

inline fun ViewManager.textView(): TextView = textView {}
inline fun ViewManager.textView(init: (@AnkoViewDslMarker TextView).() -> Unit): TextView {
    return ankoView(AnkoFactoriesSdk28View.TEXT_VIEW, theme = 0) { init() }
}

inline fun ViewManager.themedTextView(theme: Int = 0): TextView = themedTextView(theme) {}
inline fun ViewManager.themedTextView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker TextView).() -> Unit
): TextView {
    return ankoView(AnkoFactoriesSdk28View.TEXT_VIEW, theme) { init() }
}

inline fun ViewManager.textView(text: CharSequence?): TextView {
    return ankoView(AnkoFactoriesSdk28View.TEXT_VIEW, theme = 0) {
        setText(text)
    }
}

inline fun ViewManager.textView(
    text: CharSequence?,
    init: (@AnkoViewDslMarker TextView).() -> Unit
): TextView {
    return ankoView(AnkoFactoriesSdk28View.TEXT_VIEW, theme = 0) {
        init()
        setText(text)
    }
}

inline fun ViewManager.textView(text: Binder<String>, init: TextView.() -> Unit) =
    textView {
        init()
        text.bind { setText(it) }
    }

inline fun ViewManager.themedTextView(text: CharSequence?, theme: Int): TextView {
    return ankoView(AnkoFactoriesSdk28View.TEXT_VIEW, theme) {
        setText(text)
    }
}

inline fun ViewManager.themedTextView(
    text: CharSequence?,
    theme: Int,
    init: (@AnkoViewDslMarker TextView).() -> Unit
): TextView {
    return ankoView(AnkoFactoriesSdk28View.TEXT_VIEW, theme) {
        init()
        setText(text)
    }
}

inline fun ViewManager.textView(text: Int): TextView {
    return ankoView(AnkoFactoriesSdk28View.TEXT_VIEW, theme = 0) {
        setText(text)
    }
}

inline fun ViewManager.textView(
    text: Int,
    init: (@AnkoViewDslMarker TextView).() -> Unit
): TextView {
    return ankoView(AnkoFactoriesSdk28View.TEXT_VIEW, theme = 0) {
        init()
        setText(text)
    }
}

inline fun ViewManager.themedTextView(text: Int, theme: Int): TextView {
    return ankoView(AnkoFactoriesSdk28View.TEXT_VIEW, theme) {
        setText(text)
    }
}

inline fun ViewManager.themedTextView(
    text: Int,
    theme: Int,
    init: (@AnkoViewDslMarker TextView).() -> Unit
): TextView {
    return ankoView(AnkoFactoriesSdk28View.TEXT_VIEW, theme) {
        init()
        setText(text)
    }
}

inline fun ViewManager.timePicker(): android.widget.TimePicker = timePicker {}
inline fun ViewManager.timePicker(init: (@AnkoViewDslMarker android.widget.TimePicker).() -> Unit): android.widget.TimePicker {
    return ankoView(AnkoFactoriesSdk28View.TIME_PICKER, theme = 0) { init() }
}

inline fun ViewManager.themedTimePicker(theme: Int = 0): android.widget.TimePicker =
    themedTimePicker(theme) {}

inline fun ViewManager.themedTimePicker(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.TimePicker).() -> Unit
): android.widget.TimePicker {
    return ankoView(AnkoFactoriesSdk28View.TIME_PICKER, theme) { init() }
}

inline fun Context.timePicker(): android.widget.TimePicker = timePicker {}
inline fun Context.timePicker(init: (@AnkoViewDslMarker android.widget.TimePicker).() -> Unit): android.widget.TimePicker {
    return ankoView(AnkoFactoriesSdk28View.TIME_PICKER, theme = 0) { init() }
}

inline fun Context.themedTimePicker(theme: Int = 0): android.widget.TimePicker =
    themedTimePicker(theme) {}

inline fun Context.themedTimePicker(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.TimePicker).() -> Unit
): android.widget.TimePicker {
    return ankoView(AnkoFactoriesSdk28View.TIME_PICKER, theme) { init() }
}

inline fun Activity.timePicker(): android.widget.TimePicker = timePicker {}
inline fun Activity.timePicker(init: (@AnkoViewDslMarker android.widget.TimePicker).() -> Unit): android.widget.TimePicker {
    return ankoView(AnkoFactoriesSdk28View.TIME_PICKER, theme = 0) { init() }
}

inline fun Activity.themedTimePicker(theme: Int = 0): android.widget.TimePicker =
    themedTimePicker(theme) {}

inline fun Activity.themedTimePicker(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.TimePicker).() -> Unit
): android.widget.TimePicker {
    return ankoView(AnkoFactoriesSdk28View.TIME_PICKER, theme) { init() }
}

inline fun ViewManager.toggleButton(): android.widget.ToggleButton = toggleButton {}
inline fun ViewManager.toggleButton(init: (@AnkoViewDslMarker android.widget.ToggleButton).() -> Unit): android.widget.ToggleButton {
    return ankoView(AnkoFactoriesSdk28View.TOGGLE_BUTTON, theme = 0) { init() }
}

inline fun ViewManager.themedToggleButton(theme: Int = 0): android.widget.ToggleButton =
    themedToggleButton(theme) {}

inline fun ViewManager.themedToggleButton(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.ToggleButton).() -> Unit
): android.widget.ToggleButton {
    return ankoView(AnkoFactoriesSdk28View.TOGGLE_BUTTON, theme) { init() }
}

inline fun ViewManager.viewFlipper(): android.widget.ViewFlipper = viewFlipper {}
inline fun ViewManager.viewFlipper(init: (@AnkoViewDslMarker android.widget.ViewFlipper).() -> Unit): android.widget.ViewFlipper {
    return ankoView(AnkoFactoriesSdk28View.VIEW_FLIPPER, theme = 0) { init() }
}

inline fun ViewManager.themedViewFlipper(theme: Int = 0): android.widget.ViewFlipper =
    themedViewFlipper(theme) {}

inline fun ViewManager.themedViewFlipper(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.ViewFlipper).() -> Unit
): android.widget.ViewFlipper {
    return ankoView(AnkoFactoriesSdk28View.VIEW_FLIPPER, theme) { init() }
}

inline fun Context.viewFlipper(): android.widget.ViewFlipper = viewFlipper {}
inline fun Context.viewFlipper(init: (@AnkoViewDslMarker android.widget.ViewFlipper).() -> Unit): android.widget.ViewFlipper {
    return ankoView(AnkoFactoriesSdk28View.VIEW_FLIPPER, theme = 0) { init() }
}

inline fun Context.themedViewFlipper(theme: Int = 0): android.widget.ViewFlipper =
    themedViewFlipper(theme) {}

inline fun Context.themedViewFlipper(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.ViewFlipper).() -> Unit
): android.widget.ViewFlipper {
    return ankoView(AnkoFactoriesSdk28View.VIEW_FLIPPER, theme) { init() }
}

inline fun Activity.viewFlipper(): android.widget.ViewFlipper = viewFlipper {}
inline fun Activity.viewFlipper(init: (@AnkoViewDslMarker android.widget.ViewFlipper).() -> Unit): android.widget.ViewFlipper {
    return ankoView(AnkoFactoriesSdk28View.VIEW_FLIPPER, theme = 0) { init() }
}

inline fun Activity.themedViewFlipper(theme: Int = 0): android.widget.ViewFlipper =
    themedViewFlipper(theme) {}

inline fun Activity.themedViewFlipper(
    theme: Int = 0,
    init: (@AnkoViewDslMarker android.widget.ViewFlipper).() -> Unit
): android.widget.ViewFlipper {
    return ankoView(AnkoFactoriesSdk28View.VIEW_FLIPPER, theme) { init() }
}

@PublishedApi
internal object AnkoFactoriesSdk28ViewGroup {
    val FRAME_LAYOUT = { ctx: Context -> _FrameLayout(ctx) }
    val HORIZONTAL_SCROLL_VIEW = { ctx: Context -> _HorizontalScrollView(ctx) }
    val LINEAR_LAYOUT = { ctx: Context -> _LinearLayout(ctx) }
    val RADIO_GROUP = { ctx: Context -> _RadioGroup(ctx) }
    val RELATIVE_LAYOUT = { ctx: Context -> _RelativeLayout(ctx) }
    val SCROLL_VIEW = { ctx: Context -> _ScrollView(ctx) }
    val TABLE_LAYOUT = { ctx: Context -> _TableLayout(ctx) }
    val TABLE_ROW = { ctx: Context -> _TableRow(ctx) }
    val TEXT_SWITCHER = { ctx: Context -> _TextSwitcher(ctx) }
    val VIEW_ANIMATOR = { ctx: Context -> _ViewAnimator(ctx) }
}

inline fun ViewManager.frameLayout(): android.widget.FrameLayout = frameLayout {}
inline fun ViewManager.frameLayout(init: (@AnkoViewDslMarker _FrameLayout).() -> Unit): android.widget.FrameLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.FRAME_LAYOUT, theme = 0) { init() }
}

inline fun ViewManager.themedFrameLayout(theme: Int = 0): android.widget.FrameLayout =
    themedFrameLayout(theme) {}

inline fun ViewManager.themedFrameLayout(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _FrameLayout).() -> Unit
): android.widget.FrameLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.FRAME_LAYOUT, theme) { init() }
}

inline fun Context.frameLayout(): android.widget.FrameLayout = frameLayout {}
inline fun Context.frameLayout(init: (@AnkoViewDslMarker _FrameLayout).() -> Unit): android.widget.FrameLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.FRAME_LAYOUT, theme = 0) { init() }
}

inline fun Context.themedFrameLayout(theme: Int = 0): android.widget.FrameLayout =
    themedFrameLayout(theme) {}

inline fun Context.themedFrameLayout(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _FrameLayout).() -> Unit
): android.widget.FrameLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.FRAME_LAYOUT, theme) { init() }
}

inline fun Activity.frameLayout(): android.widget.FrameLayout = frameLayout {}
inline fun Activity.frameLayout(init: (@AnkoViewDslMarker _FrameLayout).() -> Unit): android.widget.FrameLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.FRAME_LAYOUT, theme = 0) { init() }
}

inline fun Activity.themedFrameLayout(theme: Int = 0): android.widget.FrameLayout =
    themedFrameLayout(theme) {}

inline fun Activity.themedFrameLayout(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _FrameLayout).() -> Unit
): android.widget.FrameLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.FRAME_LAYOUT, theme) { init() }
}

inline fun ViewManager.horizontalScrollView(): android.widget.HorizontalScrollView =
    horizontalScrollView {}

inline fun ViewManager.horizontalScrollView(init: (@AnkoViewDslMarker _HorizontalScrollView).() -> Unit): android.widget.HorizontalScrollView {
    return ankoView(AnkoFactoriesSdk28ViewGroup.HORIZONTAL_SCROLL_VIEW, theme = 0) { init() }
}

inline fun ViewManager.themedHorizontalScrollView(theme: Int = 0): android.widget.HorizontalScrollView =
    themedHorizontalScrollView(theme) {}

inline fun ViewManager.themedHorizontalScrollView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _HorizontalScrollView).() -> Unit
): android.widget.HorizontalScrollView {
    return ankoView(AnkoFactoriesSdk28ViewGroup.HORIZONTAL_SCROLL_VIEW, theme) { init() }
}

inline fun Context.horizontalScrollView(): android.widget.HorizontalScrollView =
    horizontalScrollView {}

inline fun Context.horizontalScrollView(init: (@AnkoViewDslMarker _HorizontalScrollView).() -> Unit): android.widget.HorizontalScrollView {
    return ankoView(AnkoFactoriesSdk28ViewGroup.HORIZONTAL_SCROLL_VIEW, theme = 0) { init() }
}

inline fun Context.themedHorizontalScrollView(theme: Int = 0): android.widget.HorizontalScrollView =
    themedHorizontalScrollView(theme) {}

inline fun Context.themedHorizontalScrollView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _HorizontalScrollView).() -> Unit
): android.widget.HorizontalScrollView {
    return ankoView(AnkoFactoriesSdk28ViewGroup.HORIZONTAL_SCROLL_VIEW, theme) { init() }
}

inline fun Activity.horizontalScrollView(): android.widget.HorizontalScrollView =
    horizontalScrollView {}

inline fun Activity.horizontalScrollView(init: (@AnkoViewDslMarker _HorizontalScrollView).() -> Unit): android.widget.HorizontalScrollView {
    return ankoView(AnkoFactoriesSdk28ViewGroup.HORIZONTAL_SCROLL_VIEW, theme = 0) { init() }
}

inline fun Activity.themedHorizontalScrollView(theme: Int = 0): android.widget.HorizontalScrollView =
    themedHorizontalScrollView(theme) {}

inline fun Activity.themedHorizontalScrollView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _HorizontalScrollView).() -> Unit
): android.widget.HorizontalScrollView {
    return ankoView(AnkoFactoriesSdk28ViewGroup.HORIZONTAL_SCROLL_VIEW, theme) { init() }
}

inline fun ViewManager.linearLayout(): android.widget.LinearLayout = linearLayout {}
inline fun ViewManager.linearLayout(init: (@AnkoViewDslMarker _LinearLayout).() -> Unit): android.widget.LinearLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.LINEAR_LAYOUT, theme = 0) { init() }
}

inline fun ViewManager.themedLinearLayout(theme: Int = 0): android.widget.LinearLayout =
    themedLinearLayout(theme) {}

inline fun ViewManager.themedLinearLayout(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _LinearLayout).() -> Unit
): android.widget.LinearLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.LINEAR_LAYOUT, theme) { init() }
}

inline fun Context.linearLayout(): android.widget.LinearLayout = linearLayout {}
inline fun Context.linearLayout(init: (@AnkoViewDslMarker _LinearLayout).() -> Unit): android.widget.LinearLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.LINEAR_LAYOUT, theme = 0) { init() }
}

inline fun Context.themedLinearLayout(theme: Int = 0): android.widget.LinearLayout =
    themedLinearLayout(theme) {}

inline fun Context.themedLinearLayout(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _LinearLayout).() -> Unit
): android.widget.LinearLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.LINEAR_LAYOUT, theme) { init() }
}

inline fun Activity.linearLayout(): android.widget.LinearLayout = linearLayout {}
inline fun Activity.linearLayout(init: (@AnkoViewDslMarker _LinearLayout).() -> Unit): android.widget.LinearLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.LINEAR_LAYOUT, theme = 0) { init() }
}

inline fun Activity.themedLinearLayout(theme: Int = 0): android.widget.LinearLayout =
    themedLinearLayout(theme) {}

inline fun Activity.themedLinearLayout(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _LinearLayout).() -> Unit
): android.widget.LinearLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.LINEAR_LAYOUT, theme) { init() }
}

inline fun ViewManager.radioGroup(): android.widget.RadioGroup = radioGroup {}
inline fun ViewManager.radioGroup(init: (@AnkoViewDslMarker _RadioGroup).() -> Unit): android.widget.RadioGroup {
    return ankoView(AnkoFactoriesSdk28ViewGroup.RADIO_GROUP, theme = 0) { init() }
}

inline fun ViewManager.themedRadioGroup(theme: Int = 0): android.widget.RadioGroup =
    themedRadioGroup(theme) {}

inline fun ViewManager.themedRadioGroup(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _RadioGroup).() -> Unit
): android.widget.RadioGroup {
    return ankoView(AnkoFactoriesSdk28ViewGroup.RADIO_GROUP, theme) { init() }
}

inline fun Context.radioGroup(): android.widget.RadioGroup = radioGroup {}
inline fun Context.radioGroup(init: (@AnkoViewDslMarker _RadioGroup).() -> Unit): android.widget.RadioGroup {
    return ankoView(AnkoFactoriesSdk28ViewGroup.RADIO_GROUP, theme = 0) { init() }
}

inline fun Context.themedRadioGroup(theme: Int = 0): android.widget.RadioGroup =
    themedRadioGroup(theme) {}

inline fun Context.themedRadioGroup(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _RadioGroup).() -> Unit
): android.widget.RadioGroup {
    return ankoView(AnkoFactoriesSdk28ViewGroup.RADIO_GROUP, theme) { init() }
}

inline fun Activity.radioGroup(): android.widget.RadioGroup = radioGroup {}
inline fun Activity.radioGroup(init: (@AnkoViewDslMarker _RadioGroup).() -> Unit): android.widget.RadioGroup {
    return ankoView(AnkoFactoriesSdk28ViewGroup.RADIO_GROUP, theme = 0) { init() }
}

inline fun Activity.themedRadioGroup(theme: Int = 0): android.widget.RadioGroup =
    themedRadioGroup(theme) {}

inline fun Activity.themedRadioGroup(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _RadioGroup).() -> Unit
): android.widget.RadioGroup {
    return ankoView(AnkoFactoriesSdk28ViewGroup.RADIO_GROUP, theme) { init() }
}

inline fun ViewManager.relativeLayout(): android.widget.RelativeLayout = relativeLayout {}
inline fun ViewManager.relativeLayout(init: (@AnkoViewDslMarker _RelativeLayout).() -> Unit): android.widget.RelativeLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.RELATIVE_LAYOUT, theme = 0) { init() }
}

inline fun ViewManager.themedRelativeLayout(theme: Int = 0): android.widget.RelativeLayout =
    themedRelativeLayout(theme) {}

inline fun ViewManager.themedRelativeLayout(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _RelativeLayout).() -> Unit
): android.widget.RelativeLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.RELATIVE_LAYOUT, theme) { init() }
}

inline fun Context.relativeLayout(): android.widget.RelativeLayout = relativeLayout {}
inline fun Context.relativeLayout(init: (@AnkoViewDslMarker _RelativeLayout).() -> Unit): android.widget.RelativeLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.RELATIVE_LAYOUT, theme = 0) { init() }
}

inline fun Context.themedRelativeLayout(theme: Int = 0): android.widget.RelativeLayout =
    themedRelativeLayout(theme) {}

inline fun Context.themedRelativeLayout(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _RelativeLayout).() -> Unit
): android.widget.RelativeLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.RELATIVE_LAYOUT, theme) { init() }
}

inline fun Activity.relativeLayout(): android.widget.RelativeLayout = relativeLayout {}
inline fun Activity.relativeLayout(init: (@AnkoViewDslMarker _RelativeLayout).() -> Unit): android.widget.RelativeLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.RELATIVE_LAYOUT, theme = 0) { init() }
}

inline fun Activity.themedRelativeLayout(theme: Int = 0): android.widget.RelativeLayout =
    themedRelativeLayout(theme) {}

inline fun Activity.themedRelativeLayout(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _RelativeLayout).() -> Unit
): android.widget.RelativeLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.RELATIVE_LAYOUT, theme) { init() }
}

inline fun ViewManager.scrollView(): android.widget.ScrollView = scrollView {}
inline fun ViewManager.scrollView(init: (@AnkoViewDslMarker _ScrollView).() -> Unit): android.widget.ScrollView {
    return ankoView(AnkoFactoriesSdk28ViewGroup.SCROLL_VIEW, theme = 0) { init() }
}

inline fun ViewManager.themedScrollView(theme: Int = 0): android.widget.ScrollView =
    themedScrollView(theme) {}

inline fun ViewManager.themedScrollView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _ScrollView).() -> Unit
): android.widget.ScrollView {
    return ankoView(AnkoFactoriesSdk28ViewGroup.SCROLL_VIEW, theme) { init() }
}

inline fun Context.scrollView(): android.widget.ScrollView = scrollView {}
inline fun Context.scrollView(init: (@AnkoViewDslMarker _ScrollView).() -> Unit): android.widget.ScrollView {
    return ankoView(AnkoFactoriesSdk28ViewGroup.SCROLL_VIEW, theme = 0) { init() }
}

inline fun Context.themedScrollView(theme: Int = 0): android.widget.ScrollView =
    themedScrollView(theme) {}

inline fun Context.themedScrollView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _ScrollView).() -> Unit
): android.widget.ScrollView {
    return ankoView(AnkoFactoriesSdk28ViewGroup.SCROLL_VIEW, theme) { init() }
}

inline fun Activity.scrollView(): android.widget.ScrollView = scrollView {}
inline fun Activity.scrollView(init: (@AnkoViewDslMarker _ScrollView).() -> Unit): android.widget.ScrollView {
    return ankoView(AnkoFactoriesSdk28ViewGroup.SCROLL_VIEW, theme = 0) { init() }
}

inline fun Activity.themedScrollView(theme: Int = 0): android.widget.ScrollView =
    themedScrollView(theme) {}

inline fun Activity.themedScrollView(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _ScrollView).() -> Unit
): android.widget.ScrollView {
    return ankoView(AnkoFactoriesSdk28ViewGroup.SCROLL_VIEW, theme) { init() }
}

inline fun ViewManager.tableLayout(): android.widget.TableLayout = tableLayout {}
inline fun ViewManager.tableLayout(init: (@AnkoViewDslMarker _TableLayout).() -> Unit): android.widget.TableLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.TABLE_LAYOUT, theme = 0) { init() }
}

inline fun ViewManager.themedTableLayout(theme: Int = 0): android.widget.TableLayout =
    themedTableLayout(theme) {}

inline fun ViewManager.themedTableLayout(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _TableLayout).() -> Unit
): android.widget.TableLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.TABLE_LAYOUT, theme) { init() }
}

inline fun Context.tableLayout(): android.widget.TableLayout = tableLayout {}
inline fun Context.tableLayout(init: (@AnkoViewDslMarker _TableLayout).() -> Unit): android.widget.TableLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.TABLE_LAYOUT, theme = 0) { init() }
}

inline fun Context.themedTableLayout(theme: Int = 0): android.widget.TableLayout =
    themedTableLayout(theme) {}

inline fun Context.themedTableLayout(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _TableLayout).() -> Unit
): android.widget.TableLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.TABLE_LAYOUT, theme) { init() }
}

inline fun Activity.tableLayout(): android.widget.TableLayout = tableLayout {}
inline fun Activity.tableLayout(init: (@AnkoViewDslMarker _TableLayout).() -> Unit): android.widget.TableLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.TABLE_LAYOUT, theme = 0) { init() }
}

inline fun Activity.themedTableLayout(theme: Int = 0): android.widget.TableLayout =
    themedTableLayout(theme) {}

inline fun Activity.themedTableLayout(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _TableLayout).() -> Unit
): android.widget.TableLayout {
    return ankoView(AnkoFactoriesSdk28ViewGroup.TABLE_LAYOUT, theme) { init() }
}

inline fun ViewManager.tableRow(): android.widget.TableRow = tableRow {}
inline fun ViewManager.tableRow(init: (@AnkoViewDslMarker _TableRow).() -> Unit): android.widget.TableRow {
    return ankoView(AnkoFactoriesSdk28ViewGroup.TABLE_ROW, theme = 0) { init() }
}

inline fun ViewManager.themedTableRow(theme: Int = 0): android.widget.TableRow =
    themedTableRow(theme) {}

inline fun ViewManager.themedTableRow(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _TableRow).() -> Unit
): android.widget.TableRow {
    return ankoView(AnkoFactoriesSdk28ViewGroup.TABLE_ROW, theme) { init() }
}

inline fun Context.tableRow(): android.widget.TableRow = tableRow {}
inline fun Context.tableRow(init: (@AnkoViewDslMarker _TableRow).() -> Unit): android.widget.TableRow {
    return ankoView(AnkoFactoriesSdk28ViewGroup.TABLE_ROW, theme = 0) { init() }
}

inline fun Context.themedTableRow(theme: Int = 0): android.widget.TableRow =
    themedTableRow(theme) {}

inline fun Context.themedTableRow(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _TableRow).() -> Unit
): android.widget.TableRow {
    return ankoView(AnkoFactoriesSdk28ViewGroup.TABLE_ROW, theme) { init() }
}

inline fun Activity.tableRow(): android.widget.TableRow = tableRow {}
inline fun Activity.tableRow(init: (@AnkoViewDslMarker _TableRow).() -> Unit): android.widget.TableRow {
    return ankoView(AnkoFactoriesSdk28ViewGroup.TABLE_ROW, theme = 0) { init() }
}

inline fun Activity.themedTableRow(theme: Int = 0): android.widget.TableRow =
    themedTableRow(theme) {}

inline fun Activity.themedTableRow(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _TableRow).() -> Unit
): android.widget.TableRow {
    return ankoView(AnkoFactoriesSdk28ViewGroup.TABLE_ROW, theme) { init() }
}

inline fun ViewManager.textSwitcher(): android.widget.TextSwitcher = textSwitcher {}
inline fun ViewManager.textSwitcher(init: (@AnkoViewDslMarker _TextSwitcher).() -> Unit): android.widget.TextSwitcher {
    return ankoView(AnkoFactoriesSdk28ViewGroup.TEXT_SWITCHER, theme = 0) { init() }
}

inline fun ViewManager.themedTextSwitcher(theme: Int = 0): android.widget.TextSwitcher =
    themedTextSwitcher(theme) {}

inline fun ViewManager.themedTextSwitcher(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _TextSwitcher).() -> Unit
): android.widget.TextSwitcher {
    return ankoView(AnkoFactoriesSdk28ViewGroup.TEXT_SWITCHER, theme) { init() }
}

inline fun Context.textSwitcher(): android.widget.TextSwitcher = textSwitcher {}
inline fun Context.textSwitcher(init: (@AnkoViewDslMarker _TextSwitcher).() -> Unit): android.widget.TextSwitcher {
    return ankoView(AnkoFactoriesSdk28ViewGroup.TEXT_SWITCHER, theme = 0) { init() }
}

inline fun Context.themedTextSwitcher(theme: Int = 0): android.widget.TextSwitcher =
    themedTextSwitcher(theme) {}

inline fun Context.themedTextSwitcher(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _TextSwitcher).() -> Unit
): android.widget.TextSwitcher {
    return ankoView(AnkoFactoriesSdk28ViewGroup.TEXT_SWITCHER, theme) { init() }
}

inline fun Activity.textSwitcher(): android.widget.TextSwitcher = textSwitcher {}
inline fun Activity.textSwitcher(init: (@AnkoViewDslMarker _TextSwitcher).() -> Unit): android.widget.TextSwitcher {
    return ankoView(AnkoFactoriesSdk28ViewGroup.TEXT_SWITCHER, theme = 0) { init() }
}

inline fun Activity.themedTextSwitcher(theme: Int = 0): android.widget.TextSwitcher =
    themedTextSwitcher(theme) {}

inline fun Activity.themedTextSwitcher(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _TextSwitcher).() -> Unit
): android.widget.TextSwitcher {
    return ankoView(AnkoFactoriesSdk28ViewGroup.TEXT_SWITCHER, theme) { init() }
}

inline fun ViewManager.viewAnimator(): android.widget.ViewAnimator = viewAnimator {}
inline fun ViewManager.viewAnimator(init: (@AnkoViewDslMarker _ViewAnimator).() -> Unit): android.widget.ViewAnimator {
    return ankoView(AnkoFactoriesSdk28ViewGroup.VIEW_ANIMATOR, theme = 0) { init() }
}

inline fun ViewManager.themedViewAnimator(theme: Int = 0): android.widget.ViewAnimator =
    themedViewAnimator(theme) {}

inline fun ViewManager.themedViewAnimator(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _ViewAnimator).() -> Unit
): android.widget.ViewAnimator {
    return ankoView(AnkoFactoriesSdk28ViewGroup.VIEW_ANIMATOR, theme) { init() }
}

inline fun Context.viewAnimator(): android.widget.ViewAnimator = viewAnimator {}
inline fun Context.viewAnimator(init: (@AnkoViewDslMarker _ViewAnimator).() -> Unit): android.widget.ViewAnimator {
    return ankoView(AnkoFactoriesSdk28ViewGroup.VIEW_ANIMATOR, theme = 0) { init() }
}

inline fun Context.themedViewAnimator(theme: Int = 0): android.widget.ViewAnimator =
    themedViewAnimator(theme) {}

inline fun Context.themedViewAnimator(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _ViewAnimator).() -> Unit
): android.widget.ViewAnimator {
    return ankoView(AnkoFactoriesSdk28ViewGroup.VIEW_ANIMATOR, theme) { init() }
}

inline fun Activity.viewAnimator(): android.widget.ViewAnimator = viewAnimator {}
inline fun Activity.viewAnimator(init: (@AnkoViewDslMarker _ViewAnimator).() -> Unit): android.widget.ViewAnimator {
    return ankoView(AnkoFactoriesSdk28ViewGroup.VIEW_ANIMATOR, theme = 0) { init() }
}

inline fun Activity.themedViewAnimator(theme: Int = 0): android.widget.ViewAnimator =
    themedViewAnimator(theme) {}

inline fun Activity.themedViewAnimator(
    theme: Int = 0,
    init: (@AnkoViewDslMarker _ViewAnimator).() -> Unit
): android.widget.ViewAnimator {
    return ankoView(AnkoFactoriesSdk28ViewGroup.VIEW_ANIMATOR, theme) { init() }
}
