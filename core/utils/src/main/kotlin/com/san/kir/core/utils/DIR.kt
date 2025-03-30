package com.san.kir.core.utils

// Названия папок
public object DIR {
    public const val ROOT: String = "Manger"
    public const val PROFILE: String = "$ROOT/profile"
    public const val CATALOGS: String = "$ROOT/catalogs"
    public const val MANGA: String = "$ROOT/manga"
    public const val CACHE: String = "$PROFILE/.cache"
    private const val LOCAL = "$MANGA/local"
    public val ALL: List<String> = listOf(CATALOGS, MANGA, PROFILE, LOCAL, CACHE)

    public val catalogName: (String) -> String = { "$CATALOGS/$it.db" }
}
