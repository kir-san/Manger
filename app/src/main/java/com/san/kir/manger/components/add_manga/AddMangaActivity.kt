package com.san.kir.manger.components.add_manga

import android.R.id
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.san.kir.manger.R
import com.san.kir.manger.R.string
import com.san.kir.manger.extending.ThemedActionBarActivity
import com.san.kir.manger.extending.launchCtx
import com.san.kir.manger.extending.views.showAlways
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.MangaColumn
import com.san.kir.manger.view_models.AddMangaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.anko.setContentView

class AddMangaActivity : ThemedActionBarActivity() {
    private val mView by lazy { AddMangaView(this) }
    val mViewModel by lazy {
        ViewModelProviders.of(this).get(AddMangaViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mView.setContentView(this)
        setTitle(R.string.add_manga_title)
        when {
            intent.hasExtra(MangaColumn.unic) -> {
                launchCtx {
                    val manga = mViewModel.getMangaItem(intent.getStringExtra(MangaColumn.unic))

                    withContext(Dispatchers.Main) {
                        mView.setManga(manga)
                    }
                }
            }
            else -> mView.setManga(Manga())
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 1, 1, string.add_manga_ready).showAlways()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            id.home -> onBackPressed()
            1 -> {
                mViewModel.mangaUpdate(mView.getManga())
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
