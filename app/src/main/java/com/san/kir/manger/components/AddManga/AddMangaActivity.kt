package com.san.kir.manger.components.AddManga

import android.R.id
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.san.kir.manger.Extending.BaseActivity
import com.san.kir.manger.Extending.Views.showAlways
import com.san.kir.manger.R
import com.san.kir.manger.R.string
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.DAO.insert
import com.san.kir.manger.room.DAO.update
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.Storage
import com.san.kir.manger.utils.getChapters
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.getMangaLogo
import com.san.kir.manger.utils.getShortPath
import org.jetbrains.anko.setContentView
import java.io.File

class AddMangaActivity : BaseActivity() {
    private var isEditMode = false

    private var mOldCat = ""
    private val mView = AddMangaView()

    private val mangas = Main.db.mangaDao

    @SuppressLint("MissingSuperCall")
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
            intent.hasExtra("unic") -> {
                val manga = mangas.loadManga(intent.getStringExtra("unic"))
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
                    mangas.update(manga)
                } else {
                    mangas.insert(manga)
                    getChapters(getFullPath(manga.path)).forEach {
                        val chapter = File(it)
                        Main.db.chapterDao.insert(Chapter(manga = manga.unic,
                                                          name = chapter.name,
                                                          path = getShortPath(chapter.path)))
                    }
                }
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
