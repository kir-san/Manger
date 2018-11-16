package com.san.kir.manger.components.downloadManager

import android.graphics.Color
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.View
import android.view.ViewManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.extending.ankoExtend.isNetworkAvailable
import com.san.kir.manger.extending.ankoExtend.isOnWifi
import com.san.kir.manger.extending.ankoExtend.onClick
import com.san.kir.manger.extending.dialogs.ClearDownloadsMenu
import com.san.kir.manger.utils.ID
import org.jetbrains.anko.above
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.alignParentTop
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.indefiniteSnackbar
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageButton
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.space
import org.jetbrains.anko.support.v4.nestedScrollView
import org.jetbrains.anko.verticalLayout

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
                            layoutManager = LinearLayoutManager(context)
                            allAdapter(act).into(this)
                        }
                    }
                }
                bind()
            }.lparams(height = matchParent) {
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
                    onClick { act.downloadManager.startAll() }
                    backgroundResource =
                            R.drawable.ic_start_white
                }

                // Кнопка паузы
                btn {
                    onClick { act.downloadManager.pauseAll() }

                    backgroundResource = R.drawable.ic_stop_white
                }


                // Кнопка перезапуска
                btn {
                    onClick { act.downloadManager.retryAll() }
                    backgroundResource = R.drawable.ic_update
                }

                space { }.lparams(width = dip(34))

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

    private fun CoordinatorLayout.bind() {
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
                    snack = this@bind.indefiniteSnackbar("Вай вай! Не включен Wi-fi!")
                }
                !act.isNetworkAvailable() -> {
                    snack = this@bind.indefiniteSnackbar("Нет доступа в интернет")
                }
            }

        }
    }

    private fun ViewManager.btn(action: ImageButton.() -> Unit): ImageButton {
        val buttonSize = 38 // Размер кнопок
        return imageButton {
            action()
            scaleType = ImageView.ScaleType.CENTER
            layoutParams = LinearLayout.LayoutParams(dip(buttonSize), dip(buttonSize)).apply {
                gravity = Gravity.CENTER_VERTICAL
                leftMargin = dip(12)
                rightMargin = dip(12)
            }
        }
    }
}
