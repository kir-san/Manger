package com.san.kir.data.parsing

import android.content.Context

object Status {
    fun init(context: Context) {
        COMPLETE = context.getString(R.string.status_complete)
        NOT_COMPLETE = context.getString(R.string.status_not_complete)
        SINGLE = context.getString(R.string.status_single)
        UNKNOWN = context.getString(R.string.status_unknown)
    }

    var COMPLETE: String = ""
        private set
    var NOT_COMPLETE: String = ""
        private set
    var SINGLE: String = ""
        private set
    var UNKNOWN: String = ""
        private set
}

object Translate {
    fun init(context: Context) {
        COMPLETE = context.getString(R.string.translate_complete)
        NOT_COMPLETE = context.getString(R.string.translate_not_complete)
        FREEZE = context.getString(R.string.translate_freeze)
        UNKNOWN = context.getString(R.string.translate_unknown)
    }

    var COMPLETE: String = ""
        private set
    var NOT_COMPLETE: String = ""
        private set
    var FREEZE: String = ""
        private set
    var UNKNOWN: String = ""
        private set
}
