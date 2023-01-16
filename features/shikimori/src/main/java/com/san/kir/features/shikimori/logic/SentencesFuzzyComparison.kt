package com.san.kir.features.shikimori.logic

import com.san.kir.core.utils.fuzzy
import com.san.kir.data.models.base.ShikiDbManga
import com.san.kir.data.models.base.ShikimoriMangaItem
import com.san.kir.data.models.extend.SimplifiedMangaWithChapterCounts

internal infix fun ShikimoriMangaItem.fuzzy(that: ShikimoriMangaItem): Pair<Double, Boolean> {
    return when (this) {
        is ShikiDbManga                     -> this fuzzy (that as SimplifiedMangaWithChapterCounts)
        is SimplifiedMangaWithChapterCounts -> this fuzzy (that as ShikiDbManga)
        else                                -> 0.0 to false
    }
}

internal infix fun SimplifiedMangaWithChapterCounts.fuzzy(that: ShikiDbManga) = that fuzzy this
internal infix fun ShikiDbManga.fuzzy(that: SimplifiedMangaWithChapterCounts): Pair<Double, Boolean> {
    // сравниваем названия с помочью нечеткого сравнения
    val fuzzy1 = name fuzzy that.name
    val fuzzy2 = (manga.english?.firstOrNull() ?: "") fuzzy that.name

    // Если хотя бы одно из них дало положительный результат
    // то находим значение наилучшего совпадения
    return maxOf(fuzzy1.first, fuzzy2.first) to (fuzzy1.second || fuzzy2.second)
}


