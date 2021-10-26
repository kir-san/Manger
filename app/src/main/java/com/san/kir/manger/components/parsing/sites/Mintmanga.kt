package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.components.parsing.Parsing
import com.san.kir.manger.room.dao.SiteDao

class Mintmanga(parsing: Parsing, siteDao: SiteDao) : ReadmangaTemplate(parsing, siteDao) {

    override val host: String
        get() = "https://$catalogName"

    override val name: String = "Mint Manga"
    override val catalogName: String = "mintmanga.live"
    override var volume = siteDao.getItem(name)?.volume ?: 0
    override var oldVolume = volume

    override val allCatalogName: List<String>
        get() = super.allCatalogName + "mintmanga.com"

    override suspend fun init() = super.init() as Mintmanga
}
