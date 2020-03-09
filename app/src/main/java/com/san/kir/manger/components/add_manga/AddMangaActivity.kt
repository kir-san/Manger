package com.san.kir.manger.components.add_manga

import android.R.id
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.doFromSdk
import com.san.kir.ankofork.setContentView
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.MangaColumn
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.utils.extensions.showAlways
import com.san.kir.manger.view_models.AddMangaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddMangaActivity : BaseActivity() {
    private val mView by lazy { AddMangaView(this) }
    val mViewModel by viewModels<AddMangaViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        doFromSdk(Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.transparent_dark)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.transparent_dark2)
        }

        mView.setContentView(this)

        // Получение переданных данных и получение манги на их основе
        val mangaUnic = intent.getStringExtra(MangaColumn.unic)
        if (mangaUnic?.isNotBlank() == true) {
            lifecycleScope.launch(Dispatchers.Main) {
                val manga = withContext(Dispatchers.Default) {
                    mViewModel.getMangaItem(mangaUnic)
                }
                setManga(manga)
            }
        }
        else setManga(Manga())

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun setManga(manga: Manga) {
        lifecycleScope.launchWhenResumed {
            mView.setManga(manga)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, R.id.add_manga_menu_item_ready, 1, R.string.add_manga_ready).showAlways()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            id.home -> onBackPressed()
            R.id.add_manga_menu_item_ready -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    withContext(Dispatchers.Main) {
                        mViewModel.update(mView.getManga())
                    }
                    onBackPressed()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
