package com.san.kir.manger.components.settings

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.san.kir.ankofork.sdk28.frameLayout
import com.san.kir.ankofork.ui
import com.san.kir.manger.R
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.utils.extensions.doOnApplyWindowInstets

class SettingActivity : BaseActivity() {
    private val content = ID.generate()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.main_menu_settings)

        ui {
            frameLayout {
                id = content
                doOnApplyWindowInstets { v, insets, _ ->
                    v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        bottomMargin = insets.systemWindowInsetBottom
                    }
                    insets
                }
            }
        }


        supportFragmentManager.beginTransaction()
            .replace(content, PrefFragment())
            .commit()

    }
}
