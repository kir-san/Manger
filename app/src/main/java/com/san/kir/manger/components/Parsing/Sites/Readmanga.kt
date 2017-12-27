package com.san.kir.manger.components.Parsing.Sites

import com.san.kir.manger.components.Main.Main

class Readmanga : ReadmangaTemplate() {
    override var volume = Main.db.siteDao.loadSite(name)?.volume ?: 0
    override var oldVolume = volume
}
