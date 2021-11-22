package com.san.kir.manger.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.san.kir.manger.Viewer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException
import javax.inject.Inject

class ViewerRepository @Inject constructor(private val store: DataStore<Viewer>) {
    private val tag: String = "ViewerRepo"

    val data: Flow<Viewer> = store.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(tag, "Error reading sort order preferences.", exception)
                emit(Viewer.getDefaultInstance())
            } else {
                throw exception
            }
        }

    suspend fun setOrientation(state: Viewer.Orientation) {
        store.updateData { preference ->
            preference.toBuilder().setOrientation(state).build()
        }
    }

    suspend fun setCutOut(state: Boolean) {
        store.updateData { preference ->
            preference.toBuilder().setCutout(state).build()
        }
    }

    suspend fun setControl(taps: Boolean, swipes: Boolean, keys: Boolean) {
        store.updateData { preference ->
            preference.toBuilder().setControl(
                Viewer.Control
                    .newBuilder()
                    .setTaps(taps)
                    .setSwipes(swipes)
                    .setKeys(keys)
                    .build()
            ).build()
        }
    }

    suspend fun setWithoutSaveFiles(state: Boolean) {
        store.updateData { preference ->
            preference.toBuilder().setWithoutSaveFiles(state).build()
        }
    }
}

val Context.viewerStore: DataStore<Viewer> by dataStore(
    fileName = "viewer.proto",
    serializer = ViewerSerializer
)
