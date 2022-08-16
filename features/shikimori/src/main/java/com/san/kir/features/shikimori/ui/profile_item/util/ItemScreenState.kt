package com.san.kir.features.shikimori.ui.profile_item.util

import com.san.kir.data.models.base.ShikimoriManga
import com.san.kir.data.models.base.ShikimoriRate

data class ItemScreenState internal constructor(
    val hasProfile: Profile = Profile.Load,
    val rate: ShikimoriRate? = null,
    val manga: ShikimoriManga = ShikimoriManga(),
)
