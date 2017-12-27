package com.san.kir.manger.utils

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import com.san.kir.manger.Extending.BaseActivity
import com.san.kir.manger.components.Drawer.DrawerView

interface MainRouter {
    fun showScreen(fragment: Fragment, isMain: Boolean = false, isMangaDir: Boolean = false)
    fun isMainScreen(): Boolean
    fun isMangaDir(): Boolean
}

abstract class BaseRouterImpl<T : BaseActivity>(activity: T) {
    private val fragmentManager: FragmentManager = activity.supportFragmentManager

    fun addFragment(fragment: Fragment) {
        fragmentManager.beginTransaction()
                .add(DrawerView._id.fragment, fragment)
                .commit()
    }

    fun replaceFragment(fragment: Fragment) {
        fragmentManager.beginTransaction()
                .replace(DrawerView._id.fragment, fragment)

                .commit()
    }
}

class MainRouterImpl(activity: BaseActivity) :
        MainRouter, BaseRouterImpl<BaseActivity>(activity) {
    private var isFirstRun = false
    private var isMain = false
    private var isMangaDir = false

    override fun showScreen(fragment: Fragment, isMain: Boolean, isMangaDir: Boolean) {
        this.isMain = isMain
        this.isMangaDir = isMangaDir
        if (isFirstRun) {
            addFragment(fragment)
            isFirstRun = !isFirstRun
        } else
            replaceFragment(fragment)
    }

    override fun isMainScreen(): Boolean {
        return isMain
    }

    override fun isMangaDir(): Boolean {
        return isMangaDir
    }
}


