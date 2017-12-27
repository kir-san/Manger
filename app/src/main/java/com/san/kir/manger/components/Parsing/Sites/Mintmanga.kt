package com.san.kir.manger.components.Parsing.Sites

import com.san.kir.manger.components.Main.Main

class Mintmanga : ReadmangaTemplate() {
    override val ID: Int = 2
    override val name: String = "Mint Manga"
    override val catalogName: String = "mintmanga.com"
    override var volume = Main.db.siteDao.loadSite(name)?.volume ?: 0
    override var oldVolume = volume

    override fun init() = super.init() as Mintmanga
}
