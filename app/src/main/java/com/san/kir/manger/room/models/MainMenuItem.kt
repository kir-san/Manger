package com.san.kir.manger.room.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import com.san.kir.manger.components.drawer.MainMenuType

@Entity(tableName = "mainmenuitems")
class MainMenuItem {
    @PrimaryKey(autoGenerate = true)
    var id = 0L
    var name = ""
    var isVisible = true
    var order = 0
    var type = MainMenuType.Default

    @Ignore constructor()
    constructor(name: String, order: Int, type: MainMenuType) {
        this.name = name
        this.order = order
        this.type = type
    }
}
