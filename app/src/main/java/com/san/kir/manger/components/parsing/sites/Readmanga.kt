package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.repositories.SiteRepository

class Readmanga(siteRepository: SiteRepository) : ReadmangaTemplate(siteRepository) {
    override val name = "Read Manga"
    override val catalogName = "readmanga.me"
    override var volume = siteRepository.getItem(name)?.volume ?: 0
    override var oldVolume = volume
}
