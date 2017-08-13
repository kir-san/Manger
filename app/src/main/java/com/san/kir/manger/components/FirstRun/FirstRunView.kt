package com.san.kir.manger.components.FirstRun

import android.graphics.Color
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.widget.Button
import com.san.kir.manger.R
import com.san.kir.manger.dbflow.models.Setting
import com.san.kir.manger.dbflow.wrapers.SettingsWrapper
import com.san.kir.manger.utils.DIR
import com.san.kir.manger.utils.FIRST_RUN
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.SET_MEMORY
import com.san.kir.manger.utils.createDirs
import com.san.kir.manger.utils.isExternalStorageWritable
import com.san.kir.manger.utils.log
import com.san.kir.manger.utils.name_SET_MEMORY
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.above
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.alignParentLeft
import org.jetbrains.anko.alignParentTop
import org.jetbrains.anko.below
import org.jetbrains.anko.centerHorizontally
import org.jetbrains.anko.dip
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.radioButton
import org.jetbrains.anko.radioGroup
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView
import org.jetbrains.anko.themedButton
import org.jetbrains.anko.themedLinearLayout
import org.jetbrains.anko.wrapContent
import java.io.File

class FirstRunView(private val act: FirstRunActivity) : AnkoComponent<FirstRunActivity> {

    private object _id {
        val btn_group = ID.generate()
        val storage_group = ID.generate()
        val text = ID.generate()
    }

    fun createView(parent: FirstRunActivity): View {
        return createView(AnkoContext.create(parent, parent))
    }

    override fun createView(ui: AnkoContext<FirstRunActivity>) = with(ui) {
        var buttonOk = Button(act)
        relativeLayout {
            lparams(width = matchParent, height = matchParent)

            textView {
                id = _id.text
                gravity = Gravity.CENTER_HORIZONTAL
                padding = dip(10)
                setText(R.string.first_run_text)
                textSize = 18f
                textColor = Color.BLACK
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentLeft()
                alignParentTop()
            }

            textView {
                padding = dip(10)
                setText(R.string.first_run_help)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                textSize = 17f
            }.lparams(width = matchParent, height = wrapContent) {
                below(_id.text)
                centerHorizontally()
            }

            textView {
                gravity = Gravity.CENTER_HORIZONTAL
                setText(R.string.first_run_storage)
                textSize = 18f
                textColor = Color.BLACK
            }.lparams(width = matchParent, height = wrapContent) {
                above(_id.storage_group)
            }



            radioGroup {
                id = _id.storage_group

                if (isExternalStorageWritable()) {
                    Environment.getExternalStorageDirectory().apply {
                        radioButton {
                            text = "Внутренняя память: доступно ${usableSpace / (1024 * 1024)} Мб"

                            setOnClickListener {
                                SET_MEMORY = absolutePath
                                buttonOk.isEnabled = true
                            }
                        }
                    }
                }

            }.lparams(width = matchParent, height = wrapContent) {
                above(_id.btn_group)
            }

            themedLinearLayout(android.R.attr.buttonBarStyle) {
                id = _id.btn_group

                themedButton(text = R.string.first_run_exit,
                             theme = android.R.attr.buttonBarButtonStyle) {
                    onClick { act.finishAffinity() }
                }.lparams(width = wrapContent, height = wrapContent) {
                    weight = 1f
                }

                buttonOk = themedButton(text = R.string.first_run_okay,
                                        theme = android.R.attr.buttonBarButtonStyle) {
                    isEnabled = false

                    onClick { applyChanges() }
                }.lparams(width = wrapContent, height = wrapContent) {
                    weight = 1f
                }

            }.lparams(width = matchParent, height = wrapContent) {
                alignParentBottom()
            }
        }
    }

    private fun applyChanges() {
        for (dir in DIR.ALL) {
            createDirs(File(SET_MEMORY, dir))
        }
        val temp = File(act.getExternalFilesDir(FIRST_RUN), FIRST_RUN)
        try {
            temp.createNewFile()
        } catch(ex: Exception) {
            log = "$ex при создании файла activity_first_run"
        }
        act.prefenceManager.edit().putString(
                name_SET_MEMORY,
                SET_MEMORY).apply()
        SettingsWrapper.addSetting(Setting(name_SET_MEMORY, SET_MEMORY))
        act.finish()
    }
}
