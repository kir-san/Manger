package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.components.parsing.ConnectManager

class Mangachan(connectManager: ConnectManager) : MangachanTemplate(connectManager) {

    override val host: String
        get() = "https://$catalogName"

    override val name: String = "Манга - тян"
    override val catalogName: String = "manga-chan.me"

    override var volume = 0

    override val allCatalogName: List<String>
        get() = super.allCatalogName + "mangachan.ru" + "mangachan.me"
}
