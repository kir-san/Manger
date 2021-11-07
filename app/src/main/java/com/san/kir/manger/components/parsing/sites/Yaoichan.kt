package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.components.parsing.ConnectManager

class Yaoichan(connectManager: ConnectManager) :
    MangachanTemplate(connectManager) {

    override val host: String
        get() = "https://$catalogName"

    override val name: String = "Яой-тян"
    override val catalogName: String = "yaoi-chan.me"
    override var volume = 0

    override val allCatalogName: List<String>
        get() = super.allCatalogName + "yaoichan.me"
}
