package com.san.kir.library.ui.mangaAbout

import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.base.Manga

internal data class MangaAboutState(
    val manga: Manga,
    val categoryName: String,
    val size: Double
) : ScreenState
