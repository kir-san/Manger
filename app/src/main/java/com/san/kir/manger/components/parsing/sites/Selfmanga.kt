package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.repositories.SiteRepository

class Selfmanga(private val siteRepository: SiteRepository) : ReadmangaTemplate(siteRepository) {
    override val name: String = "Self Manga"
    override val catalogName: String = "selfmanga.ru"
    override val categories = listOf("Веб", "Сборник", "Ранобэ", "Журнал")
    override var volume = siteRepository.getItem(name)?.volume ?: 0
    override var oldVolume = volume

    override suspend fun init(): Selfmanga {
        if (!isInit) {
            oldVolume = siteRepository.getItem(name)?.volume ?: 0
            val doc = ManageSites.getDocument(host)
            doc.select(".rightContent .rightBlock h5")
                    .filter { it.text() == "У нас сейчас" }
                    .forEach { volume = it.parent().select("li b").first().text().toInt() }
            isInit = true
        }
        return this
    }
}
