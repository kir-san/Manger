package com.san.kir.manger.components.storage

import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.sdk28._LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.room.entities.Storage
import com.san.kir.manger.utils.extensions.formatDouble
import com.san.kir.manger.view_models.StorageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.san.kir.ankofork.recyclerview.recyclerView

class StorageActivity : DrawerActivity() {
    private val titleObserver by lazy {
        Observer<List<Storage>> { list ->
            list?.let { it ->
                lifecycleScope.launch(Dispatchers.Default) {
                    val sum = it.sumByDouble { it.sizeFull }
                    val size = getString(R.string.storage_title_size, formatDouble(sum))

                    val length = Html.fromHtml(
                        "<font color='#FFFFFF'>${
                        resources.getQuantityString(
                            R.plurals.storage_subtitle,
                            it.size,
                            it.size
                        )
                        }</font>"
                    )

                    withContext(Dispatchers.Main) {
                        supportActionBar?.title = size
                        supportActionBar?.subtitle = length
                    }
                }
            }
        }
    }

    val mViewModel by viewModels<StorageViewModel>()

    override val _LinearLayout.customView: View
        get() = recyclerView {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context).apply {
                initialPrefetchItemCount = 15
            }
            setHasFixedSize(true)
            StorageRecyclerPresenter(this@StorageActivity).into(this@recyclerView)
            lparams(width = matchParent, height = matchParent)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = getString(R.string.main_menu_storage)
        mViewModel.getStorageItems().observe(this, titleObserver)

    }
}
