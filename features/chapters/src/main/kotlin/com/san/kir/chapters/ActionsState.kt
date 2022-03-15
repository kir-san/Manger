package com.san.kir.chapters

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Управление состоянием UI элементов
internal class ActionsState {
    var showDeleteDialog by mutableStateOf(false)
        private set
    var showFullDeleteDialog by mutableStateOf(false)
        private set

    fun showDeleteDialog() {
        showDeleteDialog = true
    }

    fun closeDeleteDialog() {
        showDeleteDialog = false
    }

    fun showFullDeleteDialog() {
        showFullDeleteDialog = true
    }

    fun closeFullDeleteDialog() {
        showFullDeleteDialog = false
    }
}
