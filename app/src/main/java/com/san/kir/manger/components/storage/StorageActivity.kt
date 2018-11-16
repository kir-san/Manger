package com.san.kir.manger.components.storage

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.View
import android.widget.LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.models.Storage
import com.san.kir.manger.utils.formatDouble
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView

class StorageActivity : DrawerActivity() {

    private val titleObserver by lazy {
        Observer<List<Storage>> { list ->
            list?.let { it ->
                GlobalScope.launch(Dispatchers.Default) {
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

    override val LinearLayout.customView: View
        get() = recyclerView {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            StorageRecyclerPresenter(this@StorageActivity).into(this)
            lparams(width = matchParent, height = matchParent)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Main.db.storageDao.loadItems().observe(this, titleObserver)

    }
}
