package com.san.kir.manger.components.downloadManager

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.nestedScrollView
import org.jetbrains.anko.verticalLayout

class DownloadManagerView(private val act: DownloadManagerActivity) {
    fun view(view: LinearLayout): View = with(view) {
        nestedScrollView {
            verticalLayout {
                // Загружаемые
                recyclerView {
                    layoutManager = LinearLayoutManager(context)
                    loadingAdapter(act).into(this)
                }

                // Все остальные
                recyclerView {
                    layoutManager = LinearLayoutManager(context)
                    otherAdapter(act).into(this)
                }
            }
        }
    }

}
