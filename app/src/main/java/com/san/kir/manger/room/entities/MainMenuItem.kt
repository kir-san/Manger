package com.san.kir.manger.room.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.san.kir.manger.utils.enums.MainMenuType

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
