package com.san.kir.manger.utils

import com.san.kir.ankofork.AnkoComponent
import com.san.kir.ankofork.AnkoContext
import com.san.kir.manger.utils.extensions.BaseActivity

abstract class ActivityView : AnkoComponent<BaseActivity> {
    open fun createView(act: BaseActivity) = createView(AnkoContext.create(act, act))
}
