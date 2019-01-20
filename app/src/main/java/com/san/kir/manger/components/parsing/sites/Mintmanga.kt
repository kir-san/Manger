package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.components.main.Main

class Mintmanga : ReadmangaTemplate() {
    override val id: Int = 2
    override val name: String = "Mint Manga"
    override val catalogName: String = "mintmanga.com"
    override var volume = Main.db.siteDao.getItem(name)?.volume ?: 0
    override var oldVolume = volume

    override suspend fun init() = super.init() as Mintmanga
}
