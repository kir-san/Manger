package com.san.kir.manger.components.Parsing.Sites

import com.san.kir.manger.dbflow.wrapers.SiteWrapper

class Mintmanga : ReadmangaTemplate() {
    override val ID: Int = 2
    override val name: String = "Mint Manga"
    override val catalogName: String = "mintmanga.com"
    override var volume = SiteWrapper.get(name)?.count ?: 0
    override var oldVolume = volume

    override fun init(): Mintmanga {
        return super.init() as Mintmanga
    }
}
