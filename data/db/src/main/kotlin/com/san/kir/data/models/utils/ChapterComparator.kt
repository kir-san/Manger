package com.san.kir.data.models.utils

import com.san.kir.data.models.base.Chapter
import java.util.regex.Pattern

private val reg = Pattern.compile("\\d+")

class ChapterComparator : Comparator<Chapter> {
    override fun compare(o1: Chapter, o2: Chapter) = compareChapterNames(o1.name, o2.name)
}

fun compareChapterNames(o1: String, o2: String) = findNumber(o1) - findNumber(o2)

fun findNumber(name: String): Int {
    val matcher1 = reg.matcher(name)
    var numbers1 = listOf<String>()

    while (matcher1.find()) {
        numbers1 = numbers1 + matcher1.group()
    }
    val prepareNumber1 = when (numbers1.size) {
        2    -> numbers1[1].toInt(10)
        1    -> numbers1[0].toInt(10)
        else -> 0
    }
    val prepare1 = String.format("%04d", prepareNumber1)
    return "${numbers1.firstOrNull() ?: 0}$prepare1".toInt(10)
}
