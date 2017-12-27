package com.san.kir.manger.utils.executors

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors


object Executors {
    private val mLock = Any()
    @Volatile private var mMainHandler: Handler? = null
    private val mDiskIO = Executors.newFixedThreadPool(2)

    val mainThread = Executor {
        if (mMainHandler == null) {
            synchronized(mLock) {
                if (mMainHandler == null) {
                    mMainHandler = Handler(Looper.getMainLooper())
                }
            }
        }
        mMainHandler?.post(it)
    }
    val IOThread = Executor { mDiskIO.execute(it) }
}
