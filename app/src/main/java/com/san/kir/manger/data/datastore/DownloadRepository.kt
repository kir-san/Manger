package com.san.kir.manger.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.san.kir.manger.Download
import com.san.kir.manger.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException
import javax.inject.Inject

class DownloadRepository @Inject constructor(private val downloadStore: DataStore<Download>) {
    private val TAG: String = "DownloadRepo"

    val data: Flow<Download> = downloadStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading sort order preferences.", exception)
                emit(Download.getDefaultInstance())
            } else {
                throw exception
            }
        }

    suspend fun setConcurrent(state: Boolean) {
        downloadStore.updateData { preference ->
            preference.toBuilder().setConcurrent(state).build()
        }
    }

    suspend fun setRetry(state: Boolean) {
        downloadStore.updateData { preference ->
            preference.toBuilder().setRetry(state).build()
        }
    }

    suspend fun setWifi(state: Boolean) {
        downloadStore.updateData { preference ->
            preference.toBuilder().setWifi(state).build()
        }
    }
}

val Context.downloadStore: DataStore<Download> by dataStore(
    fileName = "download.proto",
    serializer = DownloadSerializer
)
