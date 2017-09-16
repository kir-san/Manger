package com.san.kir.manger.components.Storage

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.san.kir.manger.Extending.BaseFragment
import com.san.kir.manger.dbflow.models.Manga

class StorageItemFragment : DialogFragment() {

    private val mView = StorageItemView()
    private lateinit var _manga: Manga

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        with(AlertDialog.Builder(activity)) {
            setView(mView.createView(this@StorageItemFragment))
            setPositiveButton("Закрыть") { _, _ -> }
            setTitle(_manga.name)
            return create()
        }
    }

    fun bind(manga: Manga, fragment: BaseFragment) {
        mView.bind(manga, fragment)
    }
}
