package com.san.kir.manger.components.Parsing.Sites

import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.components.Parsing.ManageSites

class Selfmanga : ReadmangaTemplate() {
    override val ID: Int = 3
    override val name: String = "Self Manga"
    override val catalogName: String = "selfmanga.ru"
    override val categories = listOf("Веб", "Сборник", "Ранобэ", "Журнал")
    override var volume = Main.db.siteDao.loadSite(name)?.volume ?: 0
    override var oldVolume = volume

    override fun init(): Selfmanga {
        if (!isInit) {
            oldVolume = Main.db.siteDao.loadSite(name)?.volume ?: 0
            val doc = ManageSites.getDocument(host)
            doc.select(".rightContent .rightBlock h5")
                    .filter { it -> it.text() == "У нас сейчас" }
                    .forEach { it -> volume = it.parent().select("li b").first().text().toInt() }
            isInit = true
        }
        return this
    }
}
