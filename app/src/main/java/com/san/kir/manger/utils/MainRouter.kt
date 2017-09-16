package com.san.kir.manger.utils

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import com.san.kir.manger.components.Main.MainActivity
import com.san.kir.manger.components.Main.MainView
import javax.inject.Inject

interface MainRouter {
    fun showScreen(fragment: Fragment, isMain: Boolean = false, isMangaDir: Boolean = false)
    fun isMainScreen(): Boolean
    fun isMangaDir(): Boolean
}

abstract class BaseRouterImpl<T : AppCompatActivity>(activity: T) {
    private val fragmentManager: FragmentManager = activity.supportFragmentManager

    fun addFragment(fragment: Fragment) {
        fragmentManager.beginTransaction()
                .add(MainView._id.fragment, fragment)
                .commit()
    }

    fun replaceFragment(fragment: Fragment) {
        fragmentManager.beginTransaction()
                .replace(MainView._id.fragment, fragment)
                .commit()
    }
}

class MainRouterImpl @Inject constructor(activity: MainActivity) :
        MainRouter, BaseRouterImpl<MainActivity>(activity) {
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


