package com.san.kir.manger.utils.coroutines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun ViewModel.defaultLaunchInVM(block: suspend CoroutineScope.() -> Unit) =
    viewModelScope.launch(defaultDispatcher, block = block)

fun ViewModel.mainLaunchInVM(block: suspend CoroutineScope.() -> Unit) =
    viewModelScope.launch(mainDispatcher, block = block)


fun CoroutineScope.mainLaunch(block: suspend CoroutineScope.() -> Unit) =
    launch(mainDispatcher, block = block)

fun CoroutineScope.defaultLaunch(block: suspend CoroutineScope.() -> Unit) =
    launch(defaultDispatcher, block = block)
