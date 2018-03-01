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

// Переключение отображения названия категории в библиотеке
const val NAME_SHOW_CATEGORY = "show_category"

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
