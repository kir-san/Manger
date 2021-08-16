package com.san.kir.manger.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.san.kir.manger.Chapters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException
import javax.inject.Inject

class ChaptersRepository @Inject constructor(private val store: DataStore<Chapters>) {
    private val TAG: String = "ChaptersRepo"

    val data: Flow<Chapters> = store.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading sort order preferences.", exception)
                emit(Chapters.getDefaultInstance())
            } else {
                throw exception
            }
        }

    suspend fun setTitleVisibility(state: Boolean) {
        store.updateData { preference ->
            preference.toBuilder().setIsTitle(state).build()
        }
    }

    suspend fun setIndividualFilter(state: Boolean) {
        store.updateData { preference ->
            preference.toBuilder().setIsIndividual(state).build()
        }
    }

    suspend fun setFilter(state: String) {
        store.updateData { preference ->
            preference.toBuilder().setFilterStatus(state).build()
        }
    }
}

val Context.chaptersStore: DataStore<Chapters> by dataStore(
    fileName = "chapters.proto",
    serializer = ChaptersSerializer
)
