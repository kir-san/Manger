package com.san.kir.manger.components.drawer

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.san.kir.ankofork.dialogs.toast
import com.san.kir.ankofork.doFromSdk
import com.san.kir.ankofork.sdk28._LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.utils.extensions.BaseActivity

abstract class DrawerActivity : BaseActivity() {
    private val mView by lazy { DrawerView(this) }

    abstract val _LinearLayout.customView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        doFromSdk(Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.transparent_dark)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.transparent_dark2)
        }

        mView.createView(this) {
            customView
        }
    }

    private var backPressed: Long = 0
    override fun onBackPressed() {
        if (mView.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            if (backPressed + 2000 > System.currentTimeMillis()) {
                finishAffinity()
            } else {
                toast(R.string.first_run_exit_text)
            }
            backPressed = System.currentTimeMillis()
        } else {
            mView.drawerLayout.openDrawer(GravityCompat.START)
        }
    }
}
