package com.san.kir.data.store

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.san.kir.manger.Download
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import com.san.kir.data.models.datastore.Download as Model

class DownloadStore @Inject constructor(context: Application) {
    private val TAG: String = "DownloadRepo"
    private val store: DataStore<Download> = context.downloadStore

    val data: Flow<Model> = store.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading sort order preferences.", exception)
                emit(Download.getDefaultInstance())
            } else {
                throw exception
            }
        }
        .map { store ->
            Model(
                concurrent = store.concurrent,
                retry = store.retry,
                wifi = store.wifi,
            )
        }

    suspend fun setConcurrent(state: Boolean) {
        store.updateData { preference ->
            preference.toBuilder().setConcurrent(state).build()
        }
    }

    suspend fun setRetry(state: Boolean) {
        store.updateData { preference ->
            preference.toBuilder().setRetry(state).build()
        }
    }

    suspend fun setWifi(state: Boolean) {
        store.updateData { preference ->
            preference.toBuilder().setWifi(state).build()
        }
    }
}

val Context.downloadStore: DataStore<Download> by dataStore(
    fileName = "download.proto",
    serializer = DownloadSerializer
)
