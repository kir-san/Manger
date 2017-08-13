package com.san.kir.manger.components.AddManga

import android.R.id
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.san.kir.manger.R.string
import com.san.kir.manger.components.Storage.StorageItem
import com.san.kir.manger.dbflow.models.Chapter
import com.san.kir.manger.dbflow.models.Manga
import com.san.kir.manger.dbflow.wrapers.MangaWrapper
import com.san.kir.manger.utils.LibraryAdaptersCount
import com.san.kir.manger.utils.getChapters
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.getMangaLogo
import com.san.kir.manger.utils.getShortPath
import com.san.kir.manger.utils.showAlways
import org.jetbrains.anko.setContentView
import java.io.File

class AddMangaActivity : AppCompatActivity(), LifecycleRegistryOwner {
    private val lifecycleRegystry = LifecycleRegistry(this)

    private var isEditMode = false

    private var mOldCat = ""
    private val mView = AddMangaView()


    override fun getLifecycle(): LifecycleRegistry {
        return lifecycleRegystry
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        mView.setContentView(this)

        when {
            intent.hasExtra(StorageItem::class.java.canonicalName) -> {
                val manga = intent.getParcelableExtra<StorageItem>(StorageItem::class.java.canonicalName)
                mView.setManga(Manga(name = manga.name,
                                     path = manga.path,
                                     logo = getShortPath(getMangaLogo(getFullPath(manga.path)))))
            }
            intent.hasExtra("unic") -> {
                val manga = MangaWrapper.get(intent.getStringExtra("unic"))!!
                mView.setManga(manga)
                mOldCat = manga.categories
                isEditMode = true
            }
            else -> mView.setManga(Manga())
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu!!.add(0, 1, 1, string.add_manga_ready).showAlways()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            id.home -> onBackPressed()
            1 -> {
                val manga: Manga = mView.getManga()
                if (isEditMode) {
                    manga.update()
                    LibraryAdaptersCount.update()
                } else {
                    manga.insert()
                    getChapters(getFullPath(manga.path)).forEach {
                        val chapter = File(it)
                        Chapter(manga = manga.unic,
                                name = chapter.name,
                                path = getShortPath(chapter.path)).insert()
                    }
                }
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
