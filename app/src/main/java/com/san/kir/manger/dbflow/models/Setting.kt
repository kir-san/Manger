package com.san.kir.manger.dbflow.models

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.san.kir.manger.dbflow.AppDatabase

@Table(name = "settings", database = AppDatabase::class)
class Setting : BaseModel {
    @PrimaryKey(autoincrement = true) @Column var id: Long = 0
    @Column var name: String = String()
    @Column var value: String = String()

    constructor() // not delete
    constructor(name: String, value: String) {
        this.name = name
        this.value = value
    }
}
