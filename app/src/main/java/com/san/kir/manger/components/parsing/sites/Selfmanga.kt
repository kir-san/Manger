package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.components.parsing.Parsing
import com.san.kir.manger.room.dao.SiteDao

class Selfmanga(private val parsing: Parsing, private val siteDao: SiteDao) :
    ReadmangaTemplate(parsing, siteDao) {
    override val name: String = "Self Manga"
    override val catalogName: String = "selfmanga.ru"
    override val categories = listOf("Веб", "Сборник", "Ранобэ", "Журнал")
    override var volume = siteDao.getItem(name)?.volume ?: 0
    override var oldVolume = volume

    override suspend fun init(): Selfmanga {
        if (!isInit) {
            oldVolume = siteDao.getItem(name)?.volume ?: 0
            val doc = parsing.getDocument(host)
            doc.select(".rightContent .rightBlock h5")
                    .filter { it.text() == "У нас сейчас" }
                    .forEach { volume = it.parent().select("li b").first().text().toInt() }
            isInit = true
        }
        return this
    }
}
