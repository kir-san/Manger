package com.san.kir.manger.components.drawer

import android.view.View
import com.san.kir.ankofork.sdk28._LinearLayout
import com.san.kir.manger.utils.extensions.BaseActivity

abstract class DrawerActivity : BaseActivity() {

    abstract val _LinearLayout.customView: View
}
