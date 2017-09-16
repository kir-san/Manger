package com.san.kir.manger.components.Storage

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.san.kir.manger.R
import com.san.kir.manger.utils.MainRouter
import com.san.kir.manger.Extending.Views.showAlways
import dagger.android.support.DaggerFragment
import org.jetbrains.anko.support.v4.act
import javax.inject.Inject

class StorageMainDirFragment : DaggerFragment(), LifecycleRegistryOwner {

    private val _lifecycle = LifecycleRegistry(this)

    @Inject lateinit var router: MainRouter

    override fun getLifecycle() = _lifecycle

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        act.setTitle(R.string.main_menu_storage)
        ViewModelProviders.of(this)
                .get(StorageViewModel::class.java)
                .allSize
                .observe(this, Observer { act.title = getString(R.string.storage_title_size, it) })

        return RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(this@apply.context)
            adapter = StorageMainDirAdapter(this@StorageMainDirFragment)
            setHasFixedSize(true)

        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
        menu.clear() // очистить меню перед созданием нового
        menu.add(0, 0, 0, R.string.storage_option_menu_toggle)
                .showAlways().setIcon(R.drawable.ic_toggle_switch_off)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> router.showScreen(StorageMangaDirFragment())
        }
        return true
    }
}

