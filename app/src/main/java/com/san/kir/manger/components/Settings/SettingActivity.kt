package com.san.kir.manger.components.Settings

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.Drawer.DrawerActivity
import com.san.kir.manger.utils.ID
import org.jetbrains.anko.frameLayout

class SettingActivity : DrawerActivity() {
    private val content = ID.generate()

    override val LinearLayout.customView: View
        get() = frameLayout { id = content }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.main_menu_settings)

        fragmentManager.beginTransaction()
                .replace(content, PrefFragment())
                .commit()
    }
}
