package com.san.kir.data.parsing.sites

import com.san.kir.core.internet.ConnectManager

class Readmanga(connectManager: ConnectManager) : ReadmangaTemplate(connectManager) {
    override val host: String
        get() = "https://$catalogName"

    override val name = "Read Manga"
    override val catalogName = "readmanga.live"
    override var volume = 0
    override val allCatalogName: List<String>
        get() = super.allCatalogName + "readmanga.me" + "readmanga.io"
}
