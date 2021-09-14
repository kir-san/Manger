package com.san.kir.manger.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.san.kir.manger.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException
import javax.inject.Inject

class MainRepository @Inject constructor(private val mainStore: DataStore<Main>) {
    private val TAG: String = "MainRepo"

    val data: Flow<Main> = mainStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading sort order preferences.", exception)
                emit(Main.getDefaultInstance())
            } else {
                throw exception
            }
        }

    suspend fun setShowCategory(state: Boolean) {
        mainStore.updateData { preference ->
            preference.toBuilder().setIsShowCatagery(state).build()
        }
    }

    suspend fun setTheme(state: String) {
        mainStore.updateData { preference ->
            preference.toBuilder().setTheme(state).build()
        }
    }
}

val Context.mainStore: DataStore<Main> by dataStore(
    fileName = "main.proto",
    serializer = MainSerializer
)
