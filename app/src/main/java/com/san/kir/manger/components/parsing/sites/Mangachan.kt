package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.components.main.Main

class Mangachan : MangachanTemplate() {
    override var volume = Main.db.siteDao.getItem(name)?.volume ?: 0
    override var oldVolume = volume
}
