package com.san.kir.manger.components.Storage

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.Drawer.DrawerActivity
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.DAO.loadAllSize
import com.san.kir.manger.utils.formatDouble
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView

class StorageActivity : DrawerActivity() {
    private val _adapter = StorageRecyclerPresenter(this)
    private val storage = Main.db.storageDao
    override val LinearLayout.customView: View
        get() = recyclerView {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            _adapter.into(this)
            lparams(width = matchParent, height = matchParent)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage.loadAllSize()
                .observe(this, Observer {
            title = getString(R.string.storage_title_size,
                              formatDouble(it))
        })

    }
}
