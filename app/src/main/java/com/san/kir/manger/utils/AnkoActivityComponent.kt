package com.san.kir.manger.utils

import com.san.kir.manger.extending.BaseActivity
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext

abstract class AnkoActivityComponent : AnkoComponent<BaseActivity> {
    fun createView(act: BaseActivity) = createView(AnkoContext.create(act, act))
}
