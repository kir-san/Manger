package com.san.kir.manger.components.download_manager

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.san.kir.ankofork.above
import com.san.kir.ankofork.alignParentBottom
import com.san.kir.ankofork.alignParentTop
import com.san.kir.ankofork.defaultSharedPreferences
import com.san.kir.ankofork.design.coordinatorLayout
import com.san.kir.ankofork.design.indefiniteSnackbar
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.doFromSdk
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.recyclerview.recyclerView
import com.san.kir.ankofork.sdk28.backgroundResource
import com.san.kir.ankofork.sdk28.imageButton
import com.san.kir.ankofork.sdk28.linearLayout
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.relativeLayout
import com.san.kir.ankofork.sdk28.space
import com.san.kir.ankofork.support.nestedScrollView
import com.san.kir.ankofork.verticalLayout
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.data.datastore.DownloadRepository
import com.san.kir.manger.data.datastore.downloadStore
import com.san.kir.manger.extending.dialogs.ClearDownloadsMenu
import com.san.kir.manger.services.DownloadService
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.extensions.doOnApplyWindowInstets
import com.san.kir.manger.utils.extensions.isNetworkAvailable
import com.san.kir.manger.utils.extensions.isOnWifi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

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
                    lparams(width = matchParent)

                    verticalLayout {
                        lparams(height = matchParent, width = matchParent)

                        recyclerView {
                            layoutManager =
                                LinearLayoutManager(context)
                            allAdapter(act).into(this)
                        }.lparams(width = matchParent, height = wrapContent)
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
//                backgroundColorResource = R.color.backgroundColor
                gravity = Gravity.CENTER_HORIZONTAL

                doFromSdk(21) {
                    elevation = dip(15).toFloat()
                }

                doOnApplyWindowInstets { v, insets, _ ->
                    v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        bottomMargin = insets.systemWindowInsetBottom
                    }
                    insets
                }

                // Кнопка старта
                btn {
                    onClick { DownloadService.startAll(act) }
                    backgroundResource =
                        R.drawable.ic_start
                }

                space { }.lparams(width = dip(32))
                // Кнопка паузы
                btn {
                    onClick { DownloadService.pauseAll(act) }

                    backgroundResource = R.drawable.ic_stop
                }

                space { }.lparams(width = dip(64))

                // Кнопка очистки
                btn {
                    onClick {
                        ClearDownloadsMenu(act, this@btn)
                    }
                    backgroundResource = R.drawable.ic_action_delete_t
                }
            }.lparams(width = matchParent, height = actionBarSize) { alignParentBottom() }


        }
    }

    private fun CoordinatorLayout.bind() {
        val downloadStore = DownloadRepository(context.downloadStore)

        var snack: Snackbar? = null
        findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            downloadStore.data.collect { data ->
                act.updateNetwork.bind {
                    snack?.let { s ->
                        if (s.isShown) s.dismiss()
                    }
                    when {
                        data.wifi && !act.isOnWifi() -> {
                            snack = this@bind.indefiniteSnackbar(R.string.download_view_wifi_off)
                        }
                        !act.isNetworkAvailable() -> {
                            snack =
                                this@bind.indefiniteSnackbar(R.string.download_view_internet_off)
                        }
                    }

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
