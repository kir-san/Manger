package com.san.kir.features.viewer

import android.app.Application
import com.san.kir.data.db.RoomDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
internal object DIMofule {
    @Provides
    fun db(app: Application) = RoomDB.getDatabase(app)

//    @Provides
//    fun connect(app: Application) = ConnectManager(app)

//    @Provides
//    fun store(application: Application) = ViewerRepository(application)
}
