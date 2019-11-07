package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.repositories.SiteRepository

class Mangachan(siteRepository: SiteRepository) : MangachanTemplate(siteRepository) {
    override val name: String = "Манга - тян"
    override val catalogName: String = "manga-chan.me"

    override var volume = siteRepository.getItem(name)?.volume ?: 0
    override var oldVolume = volume

    override val allCatalogName: List<String>
        get() = super.allCatalogName + "mangachan.ru" + "mangachan.me"
}
