package com.san.kir.chapters.pages

import android.content.Context
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.sp
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.systemBarsPadding
import com.san.kir.chapters.MainViewModel
import com.san.kir.chapters.R
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.rememberImage
import com.san.kir.core.compose_utils.systemBarsHorizontalPadding
import com.san.kir.features.viewer.MangaViewer
import kotlinx.coroutines.ExperimentalCoroutinesApi

// Простая страница с минимум возможностей для быстрого продолжения чтения
@OptIn(ExperimentalCoroutinesApi::class)
@Composable
internal fun AboutPageContent(
    viewModel: MainViewModel,
    context: Context = LocalContext.current,
) {
    val chapters by viewModel.chapters.collectAsState()
    val manga by viewModel.manga.collectAsState()

    val fullChaptersCount = chapters.count()
    val readChaptersCount = chapters.count { it.isRead }

    // для кнопки продолжить чтение
    val firstNotReadChapter by viewModel.firstNotReadChapter.collectAsState(null)
    val messageContinue =
        firstNotReadChapter?.let { ch ->
            context.getString(R.string.list_chapters_about_continue, ch.name)
        } ?: run {
            context.getString(R.string.list_chapters_about_not_continue)
        }

    Box(modifier = Modifier.fillMaxSize()) {
        // Большое лого манги
        Image(
            rememberImage(manga.logo),
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
                    readChaptersCount,
                    LocalContext.current.resources.getQuantityString(
                        R.plurals.chapters,
                        readChaptersCount
                    ),
                    fullChaptersCount
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
                    firstNotReadChapter?.let {
                        MangaViewer.start(context, it.id)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.default)
                    .navigationBarsPadding(start = false, end = false)
                    .systemBarsPadding(top = false, bottom = false),
                enabled = firstNotReadChapter != null,
            ) {
                Text(
                    messageContinue.toUpperCase(Locale.current),
                    modifier = Modifier.padding(Dimensions.default),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
