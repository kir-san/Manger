package com.san.kir.manger.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.san.kir.manger.FirstLaunch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException
import javax.inject.Inject

class FirstLaunchRepository @Inject constructor(
    private val firstLaunchStore: DataStore<FirstLaunch>
) {
    private val TAG: String = "FirstLaunchRepo"

    val data: Flow<FirstLaunch> = firstLaunchStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading sort order preferences.", exception)
                emit(FirstLaunch.getDefaultInstance())
            } else {
                throw exception
            }
        }

    suspend fun initFirstLaunch() {
        firstLaunchStore.updateData { preference ->
            preference.toBuilder().setIsFirstLaunch(true).build()
        }
    }
}

val Context.firstLaunchStore: DataStore<FirstLaunch> by dataStore(
    fileName = "first_launch.proto",
    serializer = FirstLaunchSerializer
)
