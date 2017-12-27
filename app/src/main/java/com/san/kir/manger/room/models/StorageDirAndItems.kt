package com.san.kir.manger.room.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation

class StorageDirAndItems {
    @Embedded
    lateinit var dir: StorageDir

    @Relation(parentColumn = "name", entityColumn = "catalogName")
    lateinit var items: List<Storage>

}
