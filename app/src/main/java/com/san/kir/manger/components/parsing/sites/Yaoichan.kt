package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.repositories.SiteRepository

class Yaoichan(siteRepository: SiteRepository) : MangachanTemplate(siteRepository) {
    override val name: String = "Яой-тян"
    override val catalogName: String = "yaoi-chan.me"
    override var volume = siteRepository.getItem(name)?.volume ?: 0
    override var oldVolume = volume

    override val allCatalogName: List<String>
        get() = super.allCatalogName + "yaoichan.me"
}
