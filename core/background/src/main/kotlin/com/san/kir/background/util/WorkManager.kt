package com.san.kir.background.util

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.collectLatest

suspend fun Context.collectWorkInfoByTag(tag: String, action: suspend (List<WorkInfo>) -> Unit) {
    WorkManager.getInstance(this)
        .getWorkInfosByTagLiveData(tag)
        .asFlow()
        .collectLatest(action)
}
