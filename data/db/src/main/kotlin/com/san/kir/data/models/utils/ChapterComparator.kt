package com.san.kir.data.models.utils

import com.san.kir.data.models.base.Chapter
import java.util.regex.Pattern

class ChapterComparator : Comparator<Chapter> {
    override fun compare(o1: Chapter, o2: Chapter) = compareChapterNames(o1.name, o2.name)
}

fun compareChapterNames(o1: String, o2: String): Int {
    val reg = Pattern.compile("\\d+")
    val matcher1 = reg.matcher(o1)
    val matcher2 = reg.matcher(o2)

    var numbers1 = listOf<String>()
    var numbers2 = listOf<String>()

    while (matcher1.find()) {
        numbers1 = numbers1 + matcher1.group()
    }

    while (matcher2.find()) {
        numbers2 = numbers2 + matcher2.group()
    }

    val prepareNumber1 = when (numbers1.size) {
        2 -> numbers1[1].toInt(10)
        1 -> numbers1[0].toInt(10)
        else -> 0
    }

    val prepareNumber2 = when (numbers2.size) {
        2 -> numbers2[1].toInt(10)
        1 -> numbers2[0].toInt(10)
        else -> 0
    }

    val prepare1 = String.format("%04d", prepareNumber1)
    val prepare2 = String.format("%04d", prepareNumber2)

    val finishNumber1 = "${numbers1.firstOrNull() ?: 0}$prepare1".toInt(10)
    val finishNumber2 = "${numbers2.firstOrNull() ?: 0}$prepare2".toInt(10)

    return finishNumber1 - finishNumber2
}

