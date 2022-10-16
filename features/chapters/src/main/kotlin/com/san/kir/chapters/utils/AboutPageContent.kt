package com.san.kir.chapters.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.sp
import com.san.kir.chapters.R
import com.san.kir.chapters.ui.chapters.NextChapter
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.bottomInsetsPadding
import com.san.kir.core.compose.horizontalInsetsPadding
import com.san.kir.core.compose.rememberImage
import com.san.kir.core.compose.systemBarsHorizontalPadding

// Простая страница с минимум возможностей для быстрого продолжения чтения
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun AboutPageContent(
    nextChapter: NextChapter,
    logo: String,
    readCount: Int,
    count: Int,
    navigateToViewer: (Long) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Большое лого манги
        Image(
            rememberImage(logo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.fillMaxSize()
        ) {
            // информация о прочитанных главах
            Text(
                stringResource(
                    R.string.list_chapters_about_read,
                    readCount,
                    pluralStringResource(R.plurals.chapters, readCount),
                    count
                ),
                modifier = Modifier
                    .background(MaterialTheme.colors.primary)
                    .padding(systemBarsHorizontalPadding(all = Dimensions.default))
                    .fillMaxWidth(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )

            // Продолжение чтения
            Button(
                onClick = {
                    if (nextChapter is NextChapter.Ok) {
                        navigateToViewer(nextChapter.id)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.default)
                    .horizontalInsetsPadding()
                    .bottomInsetsPadding(),
                enabled =  nextChapter is NextChapter.Ok ,
            ) {
                when (nextChapter) {
                    NextChapter.None ->
                        ButtonText(stringResource(R.string.list_chapters_about_not_continue))

                    is NextChapter.Ok ->
                        ButtonText(
                            stringResource(R.string.list_chapters_about_continue, nextChapter.name)
                        )
                }
            }
        }
    }
}

@Composable
private fun ButtonText(content: String) {
    val locale = Locale.current
    Text(
        text = content.toUpperCase(locale),
        modifier = Modifier.padding(Dimensions.default),
        textAlign = TextAlign.Center
    )
}
