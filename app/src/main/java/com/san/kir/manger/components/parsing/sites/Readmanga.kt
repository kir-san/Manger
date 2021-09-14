package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.components.parsing.Parsing
import com.san.kir.manger.room.dao.SiteDao

class Readmanga(parsing: Parsing, siteDao: SiteDao) :
    ReadmangaTemplate(parsing, siteDao) {
    override val name = "Read Manga"
    override val catalogName = "readmanga.live"
    override var volume = siteDao.getItem(name)?.volume ?: 0
    override var oldVolume = volume
    override val allCatalogName: List<String>
        get() = super.allCatalogName +  "readmanga.me"
}
