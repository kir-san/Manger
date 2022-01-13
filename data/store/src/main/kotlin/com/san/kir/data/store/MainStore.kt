package com.san.kir.data.store

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.san.kir.manger.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import com.san.kir.data.models.datastore.Main as Model

class MainStore @Inject constructor(context: Application) {
    private val TAG: String = "MainRepo"
    private val store = context.mainStore

    val data: Flow<Model> = store.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading sort order preferences.", exception)
                emit(Main.getDefaultInstance())
            } else {
                throw exception
            }
        }
        .map { store ->
            Model(
                theme = store.theme,
                isShowCatagery = store.isShowCatagery,
                editMenu = store.editMenu,
            )
        }

    suspend fun setShowCategory(state: Boolean) {
        store.updateData { preference ->
            preference.toBuilder().setIsShowCatagery(state).build()
        }
    }

    suspend fun setTheme(state: Boolean) {
        store.updateData { preference ->
            preference.toBuilder().setTheme(state).build()
        }
    }

    suspend fun setEditMenu(state: Boolean) {
        store.updateData { preference ->
            preference.toBuilder().setEditMenu(state).build()
        }
    }
}

internal val Context.mainStore: DataStore<Main> by dataStore(
    fileName = "main.proto",
    serializer = MainSerializer
)
