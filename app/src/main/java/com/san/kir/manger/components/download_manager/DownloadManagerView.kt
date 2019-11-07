package com.san.kir.manger.components.download_manager

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.snackbar.Snackbar
import com.san.kir.ankofork.above
import com.san.kir.ankofork.alignParentBottom
import com.san.kir.ankofork.alignParentTop
import com.san.kir.ankofork.defaultSharedPreferences
import com.san.kir.ankofork.design.coordinatorLayout
import com.san.kir.ankofork.design.indefiniteSnackbar
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.recyclerview.recyclerView
import com.san.kir.ankofork.sdk28.backgroundColor
import com.san.kir.ankofork.sdk28.backgroundResource
import com.san.kir.ankofork.sdk28.imageButton
import com.san.kir.ankofork.sdk28.linearLayout
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.relativeLayout
import com.san.kir.ankofork.sdk28.space
import com.san.kir.ankofork.support.nestedScrollView
import com.san.kir.ankofork.verticalLayout
import com.san.kir.manger.R
import com.san.kir.manger.extending.dialogs.ClearDownloadsMenu
import com.san.kir.manger.services.DownloadService
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.extensions.isNetworkAvailable
import com.san.kir.manger.utils.extensions.isOnWifi

class DownloadManagerView(private val act: DownloadManagerActivity) {
    private object Id {
        val bottomBar = ID.generate()
    }

    fun view(view: LinearLayout): View = with(view) {

        val actionBarSize = dip(50)

        relativeLayout {
            lparams(height = matchParent)

            coordinatorLayout {

                nestedScrollView {

                    verticalLayout {
                        lparams(height = matchParent)

                        recyclerView {
                            layoutManager =
                                androidx.recyclerview.widget.LinearLayoutManager(context)
                            allAdapter(act).into(this)
                        }
                    }
                }
                bind()
            }.lparams(height = matchParent, width = matchParent) {
                alignParentTop()
                above(Id.bottomBar)
            }


            // Блок кнопок переключения сортировки
            linearLayout {
                id = Id.bottomBar
                backgroundColor = Color.parseColor("#ff212121")
                gravity = Gravity.CENTER_HORIZONTAL

                // Кнопка старта
                btn {
                    onClick { DownloadService.startAll(act) }
                    backgroundResource =
                        R.drawable.ic_start_white
                }

                // Кнопка паузы
                btn {
                    onClick { DownloadService.pauseAll(act) }

                    backgroundResource = R.drawable.ic_stop_white
                }

                space { }.lparams(width = dip(64))

                // Кнопка очистки
                btn {
                    onClick {
                        ClearDownloadsMenu(act, this@btn)
                    }
                    backgroundResource = R.drawable.ic_action_delete_white
                }
            }.lparams(width = matchParent, height = actionBarSize) { alignParentBottom() }


        }
    }

    private fun androidx.coordinatorlayout.widget.CoordinatorLayout.bind() {
        val wifiKey = act.getString(R.string.settings_downloader_wifi_only_key)
        val wifiDefault =
            act.getString(R.string.settings_downloader_wifi_only_default) == "true"
        val isWifi = act.defaultSharedPreferences.getBoolean(wifiKey, wifiDefault)

        var snack: Snackbar? = null
        act.updateNetwork.bind {
            snack?.let { s ->
                if (s.isShown) s.dismiss()
            }
            when {
                isWifi && !act.isOnWifi() -> {
                    snack = this@bind.indefiniteSnackbar(R.string.download_view_wifi_off)
                }
                !act.isNetworkAvailable() -> {
                    snack = this@bind.indefiniteSnackbar(R.string.download_view_internet_off)
                }
            }

        }
    }

    private fun ViewManager.btn(action: ImageButton.() -> Unit): ImageButton {
        val buttonSize = 35 // Размер кнопок
        return imageButton {
            action()
            scaleType = ImageView.ScaleType.CENTER
            layoutParams = LinearLayout.LayoutParams(dip(buttonSize), dip(buttonSize)).apply {
                gravity = Gravity.CENTER_VERTICAL
                leftMargin = dip(8)
                rightMargin = dip(8)
            }
        }
    }
}
