package com.san.kir.manger.utils

import java.text.SimpleDateFormat
import java.util.*

var isFirstRun = true // Временный костыль

// Названия папок
object DIR {
    val ROOT = "Manger"
    val PARSERS = "$ROOT/parsers"
    val PROFILE = "$ROOT/profile"
    val CATALOGS = "$ROOT/catalogs"
    val MANGA = "$ROOT/manga"
    val LOCAL = "$MANGA/local"
    val ALL = listOf(CATALOGS, MANGA, PROFILE, LOCAL)
}


// Настройки
const val CATEGORY_ALL = "Все"

const val NAME_SHOW_CATEGORY = "show_category"

const val NAME_PORT_SPAN = "portrait_span"
const val NAME_LAND_SPAN = "landscape_span"

val sPrefViewer = "ViewerActivity"
val sPrefListChapters = "ListChaptersActivity"


// Логи
val TAG = "myLogs"

val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("en"))

enum class SortLibrary {
    AddTime,
    AbcSort,
    Populate,
    Manual
}
