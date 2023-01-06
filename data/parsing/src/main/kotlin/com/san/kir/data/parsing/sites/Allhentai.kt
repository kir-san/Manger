package com.san.kir.data.parsing.sites

import com.san.kir.core.internet.ConnectManager

class Allhentai(connectManager: ConnectManager) : ReadmangaTemplate(connectManager) {
    override val name = SITE_NAME
    override val catalogName = HOST_NAME
    override var volume = 0

    override val allCatalogName: List<String>
        get() = super.allCatalogName + "allhentai.ru" + "23.allhen.online" + "22.allhen.online"

    override val categories = listOf(
        "3D",
        "Анимация",
        "Без текста",
        "Порно комикс",
        "Порно манхва"
    )

    companion object {
        const val SITE_NAME = "All Hentai"
        const val HOST_NAME = "2023.allhen.online"
        const val AUTH_URL = "$HOST_NAME/internal/auth"
    }
}
