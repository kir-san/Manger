package com.san.kir.features.shikimori.logic

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import com.san.kir.data.models.base.ShikiDbManga
import com.san.kir.data.models.base.ShikimoriMangaItem
import com.san.kir.data.models.extend.SimplifiedMangaWithChapterCounts

// Алгоритм нечеткого сравнения предолжений
// Исходный код взят с этого проекта https://github.com/denxc/SentencesFuzzyComparison
// Из статьи на хабре https://habr.com/ru/post/341148/

internal infix fun String.fuzzy(that: String): Pair<Double, Boolean> {
    return FuzzyComparison().fuzzyEqual(this, that)
}

internal infix fun ShikimoriMangaItem.fuzzy(that: ShikimoriMangaItem): Pair<Double, Boolean> {
    return when (this) {
        is ShikiDbManga -> this fuzzy (that as SimplifiedMangaWithChapterCounts)
        is SimplifiedMangaWithChapterCounts -> this fuzzy (that as ShikiDbManga)
        else -> 0.0 to false
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

private class FuzzyComparison(
    /// Порог принятия предложений эквивалентными.
    /// Характеризует отношение количества одинаковых слов
    /// к количеству различных.
    private var thresholdSentence: Double = 0.35,

    /// Порог принятия слов эквивалентными.
    /// Характеризует отношение количества одинаковых подстрок
    /// к количеству различных.
    private var thresholdWord: Double = 0.45,

    /// Минимальная длина слова.
    private var minWordLength: Int = 3,

    /// Длина подстроки при сравнении слов.
    private var subtokenLength: Int = 2,
) {

    /// Возвращает результат нечеткого сравнения предложений.
    /// <param name="first">Первое предложение.</param>
    /// <param name="second">Второе предложение.</param>
    /// <returns>True - если результат выше порога, False - иначе.</returns>
    fun fuzzyEqual(first: String, second: String): Pair<Double, Boolean> {
        val calc = calculateFuzzyEqualValue(first, second)
        return calc to (thresholdSentence <= calc)
    }

    /// Вычисляет значение нечеткого сравнения предложений.
    /// <param name="first">Первое предложение.</param>
    /// <param name="second">Второе предложение.</param>
    /// <returns>Результат нечеткого сравнения предложений.</returns>
    fun calculateFuzzyEqualValue(first: String, second: String): Double {
        if (first.isBlank() && second.isBlank()) {
            return 1.0
        }
        if (first.isBlank() || second.isBlank()) {
            return 0.0
        }
        val tokensFirst = first.normalizeSentence.tokens
        val tokensSecond = second.normalizeSentence.tokens
        val fuzzyEqualsTokens = fuzzyEqualsTokens(tokensFirst, tokensSecond)
        val equalsCount = fuzzyEqualsTokens.size
        val firstCount = tokensFirst.size
        val secondCount = tokensSecond.size
        return 1.0 * equalsCount / (firstCount + secondCount - equalsCount)
    }

    /// Возвращает эквивалентные слова из двух наборов.
    /// <param name="tokensFirst">Слова из первого предложения.</param>
    /// <param name="tokensSecond">Слова из второго набора предложений.</param>
    /// <returns>Набор эквивалентных слов.</returns>
    private fun fuzzyEqualsTokens(
        tokensFirst: List<String>,
        tokensSecond: List<String>,
    ): List<String> {
        val equalsToken = mutableListOf<String>()
        val usedToken = BooleanArray(tokensSecond.size)
        for (i in tokensFirst.indices) {
            for (j in tokensSecond.indices) {
                if (!usedToken[j]) {
                    if (tokensFuzzyEqual(tokensFirst[i], tokensSecond[j])) {
                        equalsToken.add(tokensFirst[i])
                        usedToken[j] = true
                        break
                    }
                }
            }
        }
        return equalsToken
    }

    /// Возвращает результат нечеткого сравнения слов.
    /// <param name="firstToken">Первое слово.</param>
    /// <param name="secondToken">Второе слово.</param>
    /// <returns>Результат нечеткого сравения слов.</returns>
    private fun tokensFuzzyEqual(firstToken: String, secondToken: String): Boolean {
        var equalSubtokensCount = 0
        val usedTokens = BooleanArray(secondToken.length - subtokenLength + 1)

        for (i in 0 until firstToken.length - subtokenLength + 1) {
            val subtokenFirst = firstToken.substring(i, i + subtokenLength)
            for (j in 0 until secondToken.length - subtokenLength + 1) {
                if (!usedTokens[j]) {
                    val subtokenSecond = secondToken.substring(j, j + subtokenLength)
                    if (subtokenFirst == subtokenSecond) {
                        equalSubtokensCount++
                        usedTokens[j] = true
                        break
                    }
                }
            }
        }
        val subtokenFirstCount = firstToken.length - subtokenLength + 1
        val subtokenSecondCount = secondToken.length - subtokenLength + 1
        val tanimoto: Double =
            1.0 * equalSubtokensCount / (subtokenFirstCount + subtokenSecondCount - equalSubtokensCount)
        return thresholdWord <= tanimoto
    }

    /// Разбивает предложение на слова.
    /// <param name="sentence">Предложение.</param>
    /// <returns>Набор слов.</returns>
    private val String.tokens: List<String>
        get() = split(" ").filter { word -> word.length >= minWordLength }

    /// Приводит предложение к нормальному виду:
    /// - в нижнем регистре
    /// - удалены не буквы и не цифры
    /// <param name="sentence">Предложение.</param>
    /// <returns>Нормализованное предложение.</returns>
    private val String.normalizeSentence: String
        get() {
            val normalChars = toLowerCase(Locale.current).filter { c -> c.isNormalChar }

            return buildString {
                append(normalChars)
            }
        }

    /// Возвращает признак подходящего символа.
    /// <param name="c">Символ.</param>
    /// <returns>True - если символ буква или цифра или пробел, False - иначе.</returns>
    private val Char.isNormalChar: Boolean
        get() {
            return isLetterOrDigit() || isWhitespace()
        }

}
