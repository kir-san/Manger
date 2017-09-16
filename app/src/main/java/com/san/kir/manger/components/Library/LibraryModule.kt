package com.san.kir.manger.components.Library

import com.san.kir.manger.utils.FragmentScope
import dagger.Module
import dagger.Provides

@Module
class LibraryModule {
    @FragmentScope
    @Provides
    fun pageAdapter(fragment: LibraryFragment) = LibraryPageAdapter(fragment)

    @FragmentScope
    @Provides
    fun view(adapter: LibraryPageAdapter) = LibraryView(adapter)
}
