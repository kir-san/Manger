package com.san.kir.manger.components.drawer

import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.margin
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.sdk28.imageView
import com.san.kir.ankofork.sdk28.linearLayout
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.MainMenuItem
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.enums.MainMenuType
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.utils.extensions.formatDouble
import com.san.kir.manger.view_models.DrawerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainMenuItemView(private val act: BaseActivity, private val viewModel: DrawerViewModel) :
    RecyclerViewAdapterFactory.AnkoView<MainMenuItem>() {
    private lateinit var name: TextView
    private lateinit var type: TextView
    private lateinit var icon: ImageView

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        linearLayout {
            lparams(width = matchParent)

            icon = imageView {
            }.lparams(width = dip(24), height = dip(24)) {
                gravity = Gravity.CENTER_VERTICAL
                margin = dip(16)
            }

            name = textView {
                textSize = 16f
            }.lparams(width = matchParent) {
                weight = 1f
                gravity = Gravity.CENTER_VERTICAL
            }

            type = textView {
                textSize = 15f
            }.lparams {
                gravity = Gravity.CENTER_VERTICAL
                margin = dip(16)
            }
        }
    }

    override fun bind(item: MainMenuItem, isSelected: Boolean, position: Int) {
        act.lifecycleScope.launch(Dispatchers.Main) {
            name.text = item.name
            type.setCounter(item.type)
            icon.setImageResource(setIcon(item.type))
        }
    }

    private fun TextView.setCounter(type: MainMenuType) {
        when (type) {
            MainMenuType.Library ->
                viewModel.getMangaData().observe(act, Observer { text = it?.size.toString() })

            MainMenuType.Storage ->
                viewModel.getStorageData().observe(act, Observer {
                    text = context.getString(
                        R.string.main_menu_storage_size_mb,
                        formatDouble(it)
                    )
                })

            MainMenuType.Category ->
                act.lifecycleScope.launchWhenCreated {
                    viewModel.getCategoryData().collect { text = it?.size.toString() }
                }

            MainMenuType.Catalogs ->
                viewModel.getSiteData().observe(act, Observer { list ->
                    text = context.getString(R.string.main_menu_item_catalogs,
                                             list?.size,
                                             list?.sumBy { it.volume })
                })

            MainMenuType.Downloader ->
                viewModel.getDownloadData().observe(act, Observer { text = it.toString() })

            MainMenuType.Latest ->
                act.lifecycleScope.launchWhenCreated {
                    viewModel.getLatestData().collect { text = it.size.toString() }
                }

            MainMenuType.Schedule ->
                viewModel.getPlannedData().observe(act, Observer { text = it?.size.toString() })

            MainMenuType.Settings -> text = "^_^"
            MainMenuType.Statistic, MainMenuType.Default -> text = ""
        }
    }

    private fun setIcon(type: MainMenuType): Int = when (type) {
        MainMenuType.Library -> R.drawable.ic_library
        MainMenuType.Storage -> R.drawable.ic_storage
        MainMenuType.Category -> R.drawable.ic_category
        MainMenuType.Catalogs -> R.drawable.ic_catalogs
        MainMenuType.Downloader -> R.drawable.ic_action_download
        MainMenuType.Latest -> R.drawable.ic_update
        MainMenuType.Schedule -> R.drawable.ic_schedule
        MainMenuType.Settings -> R.drawable.ic_settings
        MainMenuType.Statistic -> R.drawable.ic_statistic
        MainMenuType.Default -> R.drawable.ic_library
    }
}
