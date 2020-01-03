package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.repositories.SiteRepository

class Mintmanga(siteRepository: SiteRepository) : ReadmangaTemplate(siteRepository) {
    override val name: String = "Mint Manga"
    override val catalogName: String = "mintmanga.live"
    override var volume = siteRepository.getItem(name)?.volume ?: 0
    override var oldVolume = volume

    override val allCatalogName: List<String>
        get() = super.allCatalogName + "mintmanga.com"

    override suspend fun init() = super.init() as Mintmanga
}
