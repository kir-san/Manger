package com.san.kir.data.store

import android.app.Application
import android.content.Context
import com.san.kir.data.models.datastore.Chapters as Model
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.san.kir.manger.Chapters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class ChaptersStore @Inject constructor(context: Application) {
    private val tag: String = "ChaptersRepo"
    private val store = context.chaptersStore

    val data: Flow<Model> = store.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(tag, "Error reading sort order preferences.", exception)
                emit(Chapters.getDefaultInstance())
            } else {
                throw exception
            }
        }
        .map { store ->
            Model(
                isIndividual = store.isIndividual,
                isTitle = store.isTitle,
                filterStatus = store.filterStatus
            )
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
