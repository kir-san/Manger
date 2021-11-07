package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.components.parsing.ConnectManager

class Readmanga(connectManager: ConnectManager) : ReadmangaTemplate(connectManager) {
    override val host: String
        get() = "https://$catalogName"

    override val name = "Read Manga"
    override val catalogName = "readmanga.io"
    override var volume = 0
    override val allCatalogName: List<String>
        get() = super.allCatalogName + "readmanga.me" + "readmanga.live"
}
