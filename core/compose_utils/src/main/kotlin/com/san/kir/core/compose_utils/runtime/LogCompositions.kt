package com.san.kir.core.compose_utils.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import com.san.kir.core.compose_utils.BuildConfig
import timber.log.Timber

class Ref(var value: Int)

@Composable
inline fun LogCompositions(msg: String) {
    if (BuildConfig.DEBUG) {
        val ref = remember { Ref(0) }
        SideEffect { ref.value++ }
        Timber.tag("Compose").d("Compositions: $msg ${ref.value}")
    }
}
