package com.san.kir.manger.Extending

import android.annotation.SuppressLint
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import dagger.android.support.DaggerAppCompatActivity
import dagger.android.support.DaggerFragment
import javax.inject.Inject


@SuppressLint("Registered")
class BaseActivity : DaggerAppCompatActivity(), LifecycleRegistryOwner {
    private val _lifecycle = LifecycleRegistry(this)

    override fun getLifecycle(): LifecycleRegistry = _lifecycle
}


open class BaseFragment @Inject constructor() : DaggerFragment(), LifecycleRegistryOwner {
    private val _lifecycle = LifecycleRegistry(this)

    override fun getLifecycle(): LifecycleRegistry = _lifecycle
}
