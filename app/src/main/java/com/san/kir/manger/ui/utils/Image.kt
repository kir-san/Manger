package com.san.kir.manger.ui.utils

import android.app.Application
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.san.kir.manger.R
import com.san.kir.manger.components.parsing.ConnectManager

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ImageWithStatus(url: String?, context: Context = LocalContext.current) {
    var statusLogo by remember { mutableStateOf(StatusLogo.Init) }
    var logo by remember { mutableStateOf(ImageBitmap(60, 60)) }

    AnimatedVisibility(statusLogo != StatusLogo.Complete) {
        DialogText(
            text = stringResource(
                id = when (statusLogo) {
                    StatusLogo.Init -> R.string.manga_info_dialog_loading
                    StatusLogo.Error -> R.string.manga_info_dialog_loading_failed
                    StatusLogo.None -> R.string.manga_info_dialog_not_image
                    StatusLogo.Complete -> R.string.manga_info_dialog_loading
                }
            )
        )
    }
    AnimatedVisibility(statusLogo == StatusLogo.Complete) {
        Image(logo, null, modifier = Modifier.fillMaxWidth())
    }

    LaunchedEffect(url) {
        if (url != null && url.isNotEmpty()) {

            val manager = ConnectManager(context.applicationContext as Application)

            manager.downloadBitmap(url)?.let { bitmap ->
                logo = bitmap.asImageBitmap()
                statusLogo = StatusLogo.Complete
            } ?: kotlin.run {
                statusLogo = StatusLogo.Error
            }
        } else {
            statusLogo = StatusLogo.None
        }
    }
}

@Composable
fun rememberImage(url: String?, context: Context = LocalContext.current): ImageBitmap {
    var logo by remember { mutableStateOf(ImageBitmap(60, 60)) }
    LaunchedEffect(url) {
        if (url != null && url.isNotEmpty()) {
            val manager = ConnectManager(context.applicationContext as Application)

            manager.downloadBitmap(url)?.let { bitmap ->
                logo = bitmap.asImageBitmap()
            }
        }
    }
    return logo
}


enum class StatusLogo {
    Init, Complete, Error, None
}
