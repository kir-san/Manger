package com.san.kir.manger.Extending.AnkoExtend

import android.graphics.Typeface
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.EventBus.BinderRx
import com.san.kir.manger.Extending.Views.DiagramForManga
import com.san.kir.manger.Extending.Views.SpecialViewPager
import com.san.kir.manger.photoview.PhotoView
import com.san.kir.manger.utils.ID
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.dip
import org.jetbrains.anko.editText
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.radioButton
import org.jetbrains.anko.textView
import org.jetbrains.anko.topPadding

inline fun ViewManager.specialViewPager(theme: Int = 0,
                                        init: SpecialViewPager.() -> Unit) = ankoView(::SpecialViewPager,
                                                                                      theme) {
    id = ID.generate()
    init()
}

inline fun ViewManager.diagramForManga(theme: Int = 0,
                                       init: DiagramForManga.() -> Unit) = ankoView(::DiagramForManga,
                                                                                    theme) {
    id = ID.generate()
    init()
}

inline fun ViewManager.photoView(theme: Int = 0, init: PhotoView.() -> Unit) = ankoView(::PhotoView,
                                                                                        theme) {
    id = ID.generate()
    init()
}

fun <T> View.bind(binder: Binder<T>, binding: (item: T) -> Unit) = binder.bind(this.id, binding)
fun <T> View.unBind(binder: Binder<T>) = binder.unBind(this.id)

fun <T> View.bind(binder: BinderRx<T>, binding: (T) -> Unit) = binder.bind(this.id, binding)
fun <T> View.unBind(binder: BinderRx<T>) = binder.unBind(this.id)

inline fun ViewManager.squareRelativeLayout(theme: Int = 0,
                                            init: _SquareRelativeLayout.() -> Unit) = ankoView(::_SquareRelativeLayout,
                                                                                               theme) {
    id = ID.generate()
    init()
}

inline fun ViewManager.textView(text: Binder<String>,
                                init: TextView.() -> Unit): TextView {

    return textView {
        id = ID.generate()
        init()
        bind(text) {
            setText(it)
        }
    }
}

inline fun ViewManager.textView(text: BinderRx<String>,
                                init: TextView.() -> Unit): TextView {
    return textView {
        id = ID.generate()
        init()
        bind(text) {
            setText(it)
        }
    }
}

fun ViewManager.labelView(text: String): TextView {
    return textView(text) {
        id = ID.generate()
        textSize = 10f
        bottomPadding = 0
        topPadding = dip(5)
    }
}

fun ViewManager.textViewBold15Size(text: String): TextView {
    return textViewBold15Size(text) {}
}

fun ViewManager.textViewBold15Size(text: String, init: TextView.() -> Unit): TextView {
    return textView(text) {
        id = ID.generate()
        textSize = 15f
        setTypeface(typeface, Typeface.BOLD)
        init()
    }
}

fun EditText.typeText(): EditText {
    inputType = InputType.TYPE_CLASS_TEXT
    return this
}

fun EditText.typeTextMultiLine(): EditText {
    InputType.TYPE_TEXT_FLAG_MULTI_LINE
    return this
}


fun ViewManager.editText(text: BinderRx<String>) = editText(text) {}
inline fun ViewManager.editText(text: BinderRx<String>, init: EditText.() -> Unit): EditText {
    return editText {
        id = ID.generate()
        bind(text) { setText(it) }
        init()
    }
}

fun ViewManager.radioButton(id: Int = ID.generate(),
                            text: Int,
                            init: RadioButton.() -> Unit): RadioButton {
    return radioButton {
        this.id = id
        if (text != 0) setText(text)
        init()
    }
}

fun ViewManager.storageItem(color: Int,
                            textBinder: BinderRx<Long>,
                            icon: Int = 0,
                            actionBinder: (TextView, Long) -> Unit): LinearLayout {
    return linearLayout {
        lparams(width = matchParent, height = dip(30))
        padding = dip(4)

        imageView {
            backgroundColor = color
        }.lparams(width = dip(50), height = dip(28))

        textView {
            leftPadding = dip(4)
            bind(textBinder) {
                actionBinder.invoke(this, it)
            }
        }.lparams {
            gravity = Gravity.CENTER_VERTICAL
        }

        if (icon > 0)
            imageView {
                backgroundResource = icon
            }
    }
}
