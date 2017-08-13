package com.san.kir.manger.Extending.Views

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.RelativeLayout

open class SquareRelativeLayout : RelativeLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, def: Int) : super(context, attrs, def)
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(con: Context, attrs: AttributeSet, def: Int, res: Int) : super(con, attrs, def, res)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}
