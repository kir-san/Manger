package com.san.kir.manger.Extending

import com.github.salomonbrys.kodein.android.KodeinAppCompatActivity
import com.san.kir.manger.utils.MainRouter
import com.san.kir.manger.utils.MainRouterImpl


abstract class BaseActivity : KodeinAppCompatActivity() {
    val router: MainRouter by lazy { MainRouterImpl(this) }
}

