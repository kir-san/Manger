package com.san.kir.manger.components.Parsing.Sites

import com.san.kir.manger.dbflow.wrapers.SiteWrapper

class Readmanga : ReadmangaTemplate() {
    override var volume = SiteWrapper.get(name)?.count ?: 0
    override var oldVolume = volume
}
