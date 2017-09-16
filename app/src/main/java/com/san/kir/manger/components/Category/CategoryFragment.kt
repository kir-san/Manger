package com.san.kir.manger.components.Category

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.san.kir.manger.R
import org.jetbrains.anko.support.v4.act


class CategoryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        act.setTitle(R.string.main_menu_category)
        return CategoryView().createView(this)
    }
}
