package com.san.kir.manger.components.parsing

import com.san.kir.manger.App
import com.san.kir.manger.R


object Status {
    val COMPLETE: String = App.context.getString(R.string.status_complete)
    val NOT_COMPLETE: String = App.context.getString(R.string.status_not_complete)
     val SINGLE: String = App.context.getString(R.string.status_single)
     val UNKNOWN: String = App.context.getString(R.string.status_unknown)
}

object Translate {
     val COMPLETE: String = App.context.getString(R.string.translate_complete)
     val NOT_COMPLETE: String = App.context.getString(R.string.translate_not_complete)
     val FREEZE: String = App.context.getString(R.string.translate_freeze)
     val UNKNOWN: String = App.context.getString(R.string.translate_unknown)
}
