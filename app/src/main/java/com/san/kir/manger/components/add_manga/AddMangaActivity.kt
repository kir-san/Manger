package com.san.kir.manger.components.add_manga

import android.R.id
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.setContentView
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.MangaColumn
import com.san.kir.manger.utils.extensions.ThemedActionBarActivity
import com.san.kir.manger.utils.extensions.showAlways
import com.san.kir.manger.view_models.AddMangaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddMangaActivity : ThemedActionBarActivity() {
    private val mView by lazy { AddMangaView(this) }
    val mViewModel by viewModels<AddMangaViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mView.setContentView(this)
        setTitle(R.string.add_manga_title)
        when {
            intent.hasExtra(MangaColumn.unic) -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    val manga = withContext(Dispatchers.Default) {
                        val mangaUnic = intent.getStringExtra(MangaColumn.unic)
                        mViewModel.getMangaItem(mangaUnic)
                    }
                    mView.setManga(manga)
                }
            }
            else -> mView.setManga(Manga())
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 1, 1, R.string.add_manga_ready).showAlways()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            id.home -> onBackPressed()
            1 -> {
                lifecycleScope.launch(Dispatchers.Default) {
                    mViewModel.update(mView.getManga())

                    withContext(Dispatchers.Main) {
                        onBackPressed()
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
