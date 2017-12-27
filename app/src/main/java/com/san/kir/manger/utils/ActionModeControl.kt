package com.san.kir.manger.utils

import android.view.ActionMode
import com.san.kir.manger.Extending.BaseActivity

class ActionModeControl(private val act: BaseActivity) {
    var actionMode: ActionMode? = null

    fun finish() {
        actionMode?.finish()
    }

    fun setTitle(value: String) {
        actionMode?.title = value
    }

    fun clear() {
        actionMode = null // ЗаNULLить переменную
    }

    fun hasFinish() = actionMode == null

    fun start(callback: ActionMode.Callback) {
        actionMode = act.startActionMode(callback)
    }
}
