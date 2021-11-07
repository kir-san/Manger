package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.components.parsing.ConnectManager

class Allhentai(connectManager: ConnectManager) : ReadmangaTemplate(connectManager) {
    override val name = "All Hentai"
    override val catalogName = "23.allhen.online"
    override var volume = 0

    override val allCatalogName: List<String>
        get() = super.allCatalogName + "allhentai.ru"

    override val categories = listOf(
        "3D",
        "Анимация",
        "Без текста",
        "Порно комикс",
        "Порно манхва"
    )
}
