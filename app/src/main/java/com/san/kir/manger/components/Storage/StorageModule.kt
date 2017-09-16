package com.san.kir.manger.components.Storage

import com.san.kir.manger.utils.FragmentScope
import dagger.Module
import dagger.Provides

@Module
class StorageModule {
    @FragmentScope
    @Provides
    fun itemView() = StorageItemView()
}
