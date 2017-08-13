package com.san.kir.manger.components.Storage

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.san.kir.manger.dbflow.models.Manga

class StorageItemFragment : DialogFragment() {

    private val view = StorageItemView()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity)
        builder.setView(view.createView(this))
        builder.setPositiveButton("Закрыть") { _, _ -> }

        return builder.create()
    }

    fun setManga(manga: Manga) {
        view.bind(manga)
    }
}
