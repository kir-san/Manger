package com.san.kir.manger.room.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.io.File

@Entity
class StorageDir {
    @PrimaryKey(autoGenerate = true)
    var id = 0L
    var name = ""
    var file = File("")
    var size = 0
    var countDir = 0

    constructor()
    constructor(name: String = "",
                file: File = File(""),
                size: Int = 0,
                countDir: Int = 0) {
        this.name = name
        this.file = file
        this.size = size
        this.countDir = countDir
    }
}
