package com.san.kir.manger.components.Storage

import android.arch.lifecycle.LifecycleRegistry
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
import com.san.kir.manger.Extending.BaseFragment
import com.san.kir.manger.R
import com.san.kir.manger.utils.MainRouter
import com.san.kir.manger.utils.getShortPath
import com.san.kir.manger.Extending.Views.showAlways
import org.jetbrains.anko.support.v4.act
import java.io.File
import javax.inject.Inject

class StorageMangaDirFragment : BaseFragment() {

    companion object {
        private val File_name = "file_name"
        private val Dir_name = "dir_name"

        // Инициализация фрагмента с изображением
        fun newInitstate(name: String, file: File): StorageMangaDirFragment {
            val set = Bundle()
            set.putString(File_name, getShortPath(file))
            set.putString(Dir_name, name)
            val frag = StorageMangaDirFragment()
            frag.arguments = set
            return frag
        }
    }

    private val _lifecycle = LifecycleRegistry(this)
    private val titleObserver = Observer<Long> {
        act.title = getString(R.string.storage_title_size_ex,
                              name,
                              it)
    }
    private var dir = ""
    private var name = ""
    private val recyclerView by lazy {
        RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(this@apply.context)
            setHasFixedSize(true)
        }
    }

    @Inject lateinit var router: MainRouter

    override fun getLifecycle(): LifecycleRegistry = _lifecycle

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        try {
            dir = arguments.getString(File_name)
            name = arguments.getString(Dir_name)
        } catch (e: NullPointerException) {
            name = getString(R.string.storage_title)
        }

        setHasOptionsMenu(true)
        update()
        return recyclerView
    }

    fun update() {
        val model = ViewModelProviders.of(this).get(StorageViewModel::class.java)
        if (dir.isEmpty()) {
            model.allSize.observe(this, titleObserver)
            recyclerView.adapter = StorageMangaDirAdapter(this)
        } else {
            model.dirSize(dir).observe(this, titleObserver)
            recyclerView.adapter = StorageMangaDirAdapter(this, dir)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
        menu.clear() // очистить меню перед созданием нового
        if (dir.isEmpty())
            menu.add(0, 0, 0, R.string.storage_option_menu_toggle)
                    .showAlways().setIcon(R.drawable.ic_toggle_switch_on)
        else
            menu.add(0, 1, 0, "Back")
                    .showAlways().setIcon(R.drawable.ic_arrow_back_white)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> router.showScreen(StorageMainDirFragment())
            1 -> act.onBackPressed()
        }
        return true
    }
}
