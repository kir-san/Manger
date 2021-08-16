package com.san.kir.manger.ui.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.stringResource
import com.san.kir.manger.R
import com.san.kir.manger.ui.application_navigation.additional_manga_screens.StatusLogo
import com.san.kir.manger.utils.loadImage

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ImageWithStatus(image: String) {
    var isShowLogo by remember { mutableStateOf(false) }
    var statusLogo by remember { mutableStateOf(StatusLogo.Standart) }
    var logo by remember { mutableStateOf(ImageBitmap(60, 60)) }

    AnimatedVisibility(visible = !isShowLogo) {
        DialogText(
            text = stringResource(
                id = when (statusLogo) {
                    StatusLogo.Standart -> R.string.manga_info_dialog_loading
                    StatusLogo.Error -> R.string.manga_info_dialog_loading_failed
                    StatusLogo.None -> R.string.manga_info_dialog_not_image
                }
            )
        )
    }
    AnimatedVisibility(visible = isShowLogo) { Image(logo, null) }

    LaunchedEffect(logo) {
        if (image.isNotEmpty()) {
            loadImage(image) {
                onSuccess { image ->
                    logo = image
                    isShowLogo = true
                }
                onError {
                    statusLogo = StatusLogo.Error
                }
                start()
            }
        } else {
            statusLogo = StatusLogo.None
        }
    }
}
