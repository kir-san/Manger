package com.san.kir.manger.components.Parsing.Sites

import com.san.kir.manger.components.Main.Main

class Mangachan : MangachanTemplate() {
    override var volume = Main.db.siteDao.loadSite(name)?.volume ?: 0
    override var oldVolume = volume
}
