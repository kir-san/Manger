package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.components.parsing.ConnectManager

class Selfmanga(private val connectManager: ConnectManager) : ReadmangaTemplate(connectManager) {

    override val host: String
        get() = "https://$catalogName"

    override val name: String = "Self Manga"
    override val catalogName: String = "selfmanga.live"
    override val categories = listOf("Веб", "Сборник", "Ранобэ", "Журнал")
    override var volume = 0

    override val allCatalogName: List<String>
        get() = super.allCatalogName + "selfmanga.ru"

    override suspend fun init(): Selfmanga {
        if (!isInit) {
            val doc = connectManager.getDocument(host)
            doc.select(".rightContent .rightBlock h5")
                    .filter { it.text() == "У нас сейчас" }
                    .forEach { volume = it.parent().select("li b").first().text().toInt() }
            isInit = true
        }
        return this
    }
}
