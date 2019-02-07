package com.san.kir.manger.extending.views

import android.support.v7.widget.SearchView
import android.widget.ImageView
import android.widget.TextView
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.textColor

fun SearchView.setButton(resId: Int) {
    find<ImageView>(android.support.v7.appcompat.R.id.search_button).imageResource = resId
}

fun SearchView.setCloseButton(resId: Int) {
    find<ImageView>(android.support.v7.appcompat.R.id.search_close_btn).imageResource = resId
}

fun SearchView.setTextColor(color: Int) {
    find<TextView>(android.support.v7.appcompat.R.id.search_src_text).textColor = color
}
