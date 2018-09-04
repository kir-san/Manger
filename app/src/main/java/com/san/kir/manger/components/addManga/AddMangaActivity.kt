package com.san.kir.manger.components.addManga

import android.R.id
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.san.kir.manger.R
import com.san.kir.manger.R.string
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.extending.ThemedActionBarActivity
import com.san.kir.manger.extending.views.showAlways
import com.san.kir.manger.room.dao.insertAsync
import com.san.kir.manger.room.dao.updateAsync
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.MangaColumn
import com.san.kir.manger.room.models.Storage
import com.san.kir.manger.utils.getChapters
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.getMangaLogo
import com.san.kir.manger.utils.getShortPath
import com.san.kir.manger.utils.shortPath
import org.jetbrains.anko.setContentView
import java.io.File

class AddMangaActivity : ThemedActionBarActivity() {
    private var isEditMode = false

    private var mOldCat = ""
    private val mView = AddMangaView()

    private val mangaDao = Main.db.mangaDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mView.setContentView(this)
        setTitle(R.string.add_manga_title)
        when {
            intent.hasExtra(Storage::class.java.canonicalName) -> {
                val manga = intent.getParcelableExtra<Storage>(Storage::class.java.canonicalName)
                mView.setManga(Manga(unic = manga.name,
                                     name = manga.name,
                                     path = manga.path,
                                     logo = getShortPath(getMangaLogo(manga.path))))
            }
            intent.hasExtra(MangaColumn.unic) -> {
                val manga = mangaDao.loadManga(intent.getStringExtra(MangaColumn.unic))
                mView.setManga(manga)
                mOldCat = manga.categories
                isEditMode = true
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
                val manga: Manga = mView.getManga()
                if (isEditMode) {
                    mangaDao.updateAsync(manga)
                } else {
                    mangaDao.insertAsync(manga)
                    getChapters(getFullPath(manga.path)).forEach {
                        val chapterFile = File(it)
                        Main.db.chapterDao.insertAsync(Chapter(manga = manga.unic,
                                                               name = chapterFile.name,
                                                               path = chapterFile.shortPath))
                    }
                }
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
