package com.san.kir.manger.components.Storage

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.san.kir.manger.Extending.BaseActivity
import com.san.kir.manger.room.models.Manga

class StorageDialogFragment : DialogFragment() {

    private val mView = StorageDialogView()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        with(AlertDialog.Builder(activity)) {
            setView(mView.createView(this@StorageDialogFragment))
            setPositiveButton("Закрыть") { _, _ -> }
            return create()
        }
    }

    fun bind(manga: Manga, act: BaseActivity) {
        mView.bind(manga, act)
    }
}
