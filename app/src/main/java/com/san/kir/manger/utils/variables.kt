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
const val FIRST_RUN = "activity_first_run"

const val categoryAll = "Все"

val FILE_SELECT_CODE = 0

const val name_SET_MEMORY = "set_memory"
const val name_SHOW_CATEGORY = "show_category"
var SET_MEMORY = ""

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
