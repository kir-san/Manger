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
const val categoryAll = "Все"

const val name_SHOW_CATEGORY = "show_category"

const val name_PORT_SPAN = "portrait_span"
const val name_LAND_SPAN = "landscape_span"

val sPrefViewer: String = "ViewerActivity"
val sPrefListChapters: String = "ListChaptersActivity"


// Логи
val TAG = "myLogs"

val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("en"))

enum class SortLibrary {
    AddTime,
    AbcSort
}
