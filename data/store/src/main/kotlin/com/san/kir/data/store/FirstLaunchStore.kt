package com.san.kir.data.store

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.san.kir.manger.FirstLaunch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import com.san.kir.data.models.datastore.FirstLaunch as Model

class FirstLaunchStore @Inject constructor(context: Application) {
    private val TAG: String = "FirstLaunchRepo"
    private val store = context.firstLaunchStore

    val data: Flow<Model> = store.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading sort order preferences.", exception)
                emit(FirstLaunch.getDefaultInstance())
            } else {
                throw exception
            }
        }
        .map { store ->
            Model(store.isFirstLaunch)
        }

    suspend fun initFirstLaunch() {
        store.updateData { preference ->
            preference.toBuilder().setIsFirstLaunch(true).build()
        }
    }
}

val Context.firstLaunchStore: DataStore<FirstLaunch> by dataStore(
    fileName = "first_launch.proto",
    serializer = FirstLaunchSerializer
)
