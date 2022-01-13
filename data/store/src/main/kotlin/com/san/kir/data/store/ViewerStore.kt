package com.san.kir.data.store

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.san.kir.manger.Viewer
import com.san.kir.manger.ViewerKt.control
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import com.san.kir.data.models.datastore.Viewer as Model

class ViewerStore @Inject constructor(context: Application) {
    private val tag: String = "ViewerRepo"
    private val store = context.viewerStore
    val data: Flow<Model> = store.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(tag, "Error reading sort order preferences.", exception)
                emit(Viewer.getDefaultInstance())
            } else {
                throw exception
            }
        }.map { store ->
            Model(
                cutOut = store.cutout,
                withoutSaveFiles = store.withoutSaveFiles,
                orientation = Model.Orientation.values()
                    .first { it.number == store.orientation.number },
                control = Model.Control(
                    taps = store.control.taps,
                    swipes = store.control.swipes,
                    keys = store.control.keys
                )
            )
        }

    suspend fun setOrientation(state: Model.Orientation) {
        store.updateData { preference ->
            preference.toBuilder().setOrientation(
                Viewer.Orientation.values().first { it.number == state.number }
            ).build()
        }
    }

    suspend fun setCutOut(state: Boolean) {
        store.updateData { preference ->
            preference.toBuilder().setCutout(state).build()
        }
    }

    suspend fun setControl(taps: Boolean, swipes: Boolean, keys: Boolean) {
        store.updateData { preference ->
            preference
                .toBuilder()
                .setControl(
                    control {
                        this.taps = taps
                        this.swipes = swipes
                        this.keys = keys
                    }
                )
                .build()
        }
    }

    suspend fun setWithoutSaveFiles(state: Boolean) {
        store.updateData { preference ->
            preference.toBuilder().setWithoutSaveFiles(state).build()
        }
    }
}

internal val Context.viewerStore: DataStore<Viewer> by dataStore(
    fileName = "viewer.proto",
    serializer = ViewerSerializer
)
