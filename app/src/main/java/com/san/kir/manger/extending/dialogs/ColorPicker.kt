package com.san.kir.manger.extending.dialogs

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import com.san.kir.manger.eventBus.Binder
import org.jetbrains.anko.alert
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.customView
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageView
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.noButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk25.coroutines.onSeekBarChangeListener
import org.jetbrains.anko.seekBar
import org.jetbrains.anko.verticalLayout

class ColorPicker(context: Context, oldColor: Int? = null, onOk: (Int) -> Unit) {
    private val maxProgress = 255
    private val color = Binder(Color.BLUE)
    private var red = 0
    private var green = 0
    private var blue = 0

    init {
        oldColor?.let {
            red = Color.red(it)
            green = Color.green(it)
            blue = Color.blue(it)
            color.item = it
        }

        context.alert {
            customView {
                verticalLayout {
                    imageView {
                        color.bind {
                            backgroundColor = it
                        }
                    }.lparams(dip(100), dip(100)) {
                        gravity = Gravity.CENTER
                        margin = dip(10)
                    }
                    seekBar {
                        max = maxProgress
                        progress = red
                        onSeekBarChangeListener {
                            onProgressChanged { _, progress, _ ->
                                red = progress
                                color.item = Color.rgb(red, green, blue)
                            }
                        }

                    }.lparams(matchParent) { margin = dip(10) }
                    seekBar {
                        max = maxProgress
                        progress = green
                        onSeekBarChangeListener {
                            onProgressChanged { _, progress, _ ->
                                green = progress
                                color.item = Color.rgb(red, green, blue)
                            }
                        }
                    }.lparams(matchParent) { margin = dip(10) }
                    seekBar {
                        max = maxProgress
                        progress = blue
                        onSeekBarChangeListener {
                            onProgressChanged { _, progress, _ ->
                                blue = progress
                                color.item = Color.rgb(red, green, blue)
                            }
                        }
                    }.lparams(matchParent) { margin = dip(10) }
                }
            }
            okButton {
                onOk.invoke(color.item)
            }
            noButton {  }
        }.show()
    }
}
