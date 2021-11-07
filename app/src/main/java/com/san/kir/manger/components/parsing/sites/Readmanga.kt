package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.components.parsing.ConnectManager

class Readmanga(connectManager: ConnectManager) : ReadmangaTemplate(connectManager) {
    override val host: String
        get() = "https://$catalogName"

    override val name = "Read Manga"
    override val catalogName = "readmanga.io"
    override var volume = siteDao.getItem(name)?.volume ?: 0
    override var oldVolume = volume
    override val allCatalogName: List<String>
        get() = super.allCatalogName + "readmanga.me" + "readmanga.live"
}
