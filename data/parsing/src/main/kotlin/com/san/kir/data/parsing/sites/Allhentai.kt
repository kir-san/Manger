package com.san.kir.data.parsing.sites

import com.san.kir.core.internet.ConnectManager

class Allhentai(connectManager: ConnectManager) : ReadmangaTemplate(connectManager) {
    override val name = "All Hentai"
    override val catalogName = "22.allhen.online"
    override var volume = 0

    override val allCatalogName: List<String>
        get() = super.allCatalogName + "allhentai.ru" + "23.allhen.online"

    override val categories = listOf(
        "3D",
        "Анимация",
        "Без текста",
        "Порно комикс",
        "Порно манхва"
    )
}
