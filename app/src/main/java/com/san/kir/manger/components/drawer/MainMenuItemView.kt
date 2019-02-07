package com.san.kir.manger.components.drawer

import android.arch.lifecycle.Observer
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.san.kir.manger.R
import com.san.kir.manger.extending.BaseActivity
import com.san.kir.manger.room.models.MainMenuItem
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.formatDouble
import com.san.kir.manger.view_models.DrawerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alignParentEnd
import org.jetbrains.anko.alignParentStart
import org.jetbrains.anko.baselineOf
import org.jetbrains.anko.centerHorizontally
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.dip
import org.jetbrains.anko.endOf
import org.jetbrains.anko.imageView
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalPadding

class MainMenuItemView(private val act: BaseActivity, private val viewModel: DrawerViewModel) :
    RecyclerViewAdapterFactory.AnkoView<MainMenuItem>() {
    private lateinit var name: TextView
    private lateinit var type: TextView
    private lateinit var icon: ImageView

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        relativeLayout {
            lparams(width = matchParent)
            padding = dip(5)
            verticalPadding = dip(13)

            icon = imageView {
                id = ID.generate()
                padding = dip(4)
            }.lparams {
                alignParentStart()
            }

            name = textView {
                id = ID.generate()
                textSize = 17.4f
                padding = dip(4)
            }.lparams {
                centerHorizontally()
                endOf(icon)
            }

            type = textView {
                textSize = 14.7f
                padding = dip(3)
            }.lparams {
                alignParentEnd()
                baselineOf(name)
//                endOf(name)
            }
        }
    }

    override fun bind(item: MainMenuItem, isSelected: Boolean, position: Int) {
        act.launch(Dispatchers.Main) {
            name.text = item.name
            type.setCounter(item.type)
            val key = act.getString(R.string.settings_app_dark_theme_key)
            val default = act.getString(R.string.settings_app_dark_theme_default) == "true"
            val isDark = act.defaultSharedPreferences.getBoolean(key, default)
            icon.setImageResource(
                if (isDark)
                    setDarkIcon(item.type)
                else
                    setWhiteIcon(item.type)
            )
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
                viewModel.getCategoryData().observe(act, Observer { text = it?.size.toString() })

            MainMenuType.Catalogs ->
                viewModel.getSiteData().observe(act, Observer { list ->
                    text = context.getString(R.string.main_menu_item_catalogs,
                                             list?.size,
                                             list?.sumBy { it.volume })
                })

            MainMenuType.Downloader ->
                viewModel.getDownloadData().observe(act, Observer { text = it.toString() })

            MainMenuType.Latest ->
                viewModel.getLatestData().observe(act, Observer { text = it?.size.toString() })

            MainMenuType.Schedule ->
                viewModel.getPlannedData().observe(act, Observer { text = it?.size.toString() })

            MainMenuType.Settings -> text = "^_^"
            MainMenuType.Statistic, MainMenuType.Default -> text = ""
        }
    }

    private fun setDarkIcon(type: MainMenuType): Int = when (type) {
        MainMenuType.Library -> R.drawable.ic_library_white
        MainMenuType.Storage -> R.drawable.ic_storage_white
        MainMenuType.Category -> R.drawable.ic_category_white
        MainMenuType.Catalogs -> R.drawable.ic_catalogs_white
        MainMenuType.Downloader -> R.drawable.ic_action_download_white
        MainMenuType.Latest -> R.drawable.ic_update
        MainMenuType.Schedule -> R.drawable.ic_schedule_white
        MainMenuType.Settings -> R.drawable.ic_settings_white
        MainMenuType.Statistic -> R.drawable.ic_statistic_white
        MainMenuType.Default -> R.drawable.ic_library_white
    }

    private fun setWhiteIcon(type: MainMenuType): Int = when (type) {
        MainMenuType.Library -> R.drawable.ic_library_black
        MainMenuType.Storage -> R.drawable.ic_storage_black
        MainMenuType.Category -> R.drawable.ic_category_black
        MainMenuType.Catalogs -> R.drawable.ic_catalogs_black
        MainMenuType.Downloader -> R.drawable.ic_action_download_black
        MainMenuType.Latest -> R.drawable.ic_update_black
        MainMenuType.Schedule -> R.drawable.ic_schedule_black
        MainMenuType.Settings -> R.drawable.ic_settings_black
        MainMenuType.Statistic -> R.drawable.ic_statistic_black
        MainMenuType.Default -> R.drawable.ic_library_black
    }
}
