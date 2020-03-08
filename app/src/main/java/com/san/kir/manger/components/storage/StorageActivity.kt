package com.san.kir.manger.components.storage

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.recyclerview.recyclerView
import com.san.kir.ankofork.sdk28._LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.room.entities.Storage
import com.san.kir.manger.utils.extensions.doOnApplyWindowInstets
import com.san.kir.manger.utils.extensions.formatDouble
import com.san.kir.manger.view_models.StorageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StorageActivity : DrawerActivity() {
    private val titleObserver by lazy {
        Observer<List<Storage>> { list ->
            list?.let { it ->
                lifecycleScope.launch(Dispatchers.Default) {
                    val sum = it.sumByDouble { it.sizeFull }
                    val size = getString(R.string.storage_title_size, formatDouble(sum))

                    val length = resources.getQuantityString(
                        R.plurals.storage_subtitle, it.size, it.size
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
            layoutManager = LinearLayoutManager(context).apply {
                initialPrefetchItemCount = 15
            }
            setHasFixedSize(true)
            StorageRecyclerPresenter(this@StorageActivity).into(this@recyclerView)
            lparams(width = matchParent, height = matchParent)

            clipToPadding = false

            doOnApplyWindowInstets { view, insets, padding ->
                view.updatePadding(bottom = padding.bottom + insets.systemWindowInsetBottom)
                insets
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = getString(R.string.main_menu_storage)
        mViewModel.getStorageItems().observe(this, titleObserver)

    }
}
