package com.san.kir.manger.components.drawer

import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.view.View
import android.widget.LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.extending.BaseActivity
import org.jetbrains.anko.toast

abstract class DrawerActivity : BaseActivity() {
    private val mView by lazy { DrawerView(this) }

    abstract val LinearLayout.customView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
