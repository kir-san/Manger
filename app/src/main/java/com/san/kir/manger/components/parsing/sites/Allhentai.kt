package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.components.parsing.Parsing
import com.san.kir.manger.room.dao.SiteDao

class Allhentai(parsing: Parsing, siteDao: SiteDao) : ReadmangaTemplate(parsing, siteDao) {
    override val name = "All Hentai"
    override val catalogName = "23.allhen.online"
    override var volume = siteDao.getItem(name)?.volume ?: 0
    override var oldVolume = volume

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
