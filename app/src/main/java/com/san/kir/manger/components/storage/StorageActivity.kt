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
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView

class StorageActivity : DrawerActivity() {

    private val titleObserver by lazy {
        Observer<List<Storage>> {
            it?.let {
                val size = async {
                    val sum = it.sumByDouble { it.sizeFull }
                    getString(R.string.storage_title_size, formatDouble(sum))
                }

                val length = async {
                    Html.fromHtml(
                        "<font color='#FFFFFF'>${
                        resources.getQuantityString(R.plurals.storage_subtitle, it.size, it.size)
                        }</font>"
                    )
                }

                launch(UI) {
                    supportActionBar?.title = size.await()
                    supportActionBar?.subtitle = length.await()
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
        Main.db.storageDao.loadLivedItems().observe(this, titleObserver)

    }
}
