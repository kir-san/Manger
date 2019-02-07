package com.san.kir.manger.utils.enums

// Названия папок
object DIR {
    const val ROOT = "Manger"
    const val PROFILE = "$ROOT/profile"
    const val CATALOGS = "$ROOT/catalogs"
    const val MANGA = "$ROOT/manga"
    const val CACHE = "$PROFILE/.cache"
    private const val LOCAL = "$MANGA/local"
    val ALL = listOf(
        CATALOGS,
        MANGA,
        PROFILE,
        LOCAL,
        CACHE
    )
}
