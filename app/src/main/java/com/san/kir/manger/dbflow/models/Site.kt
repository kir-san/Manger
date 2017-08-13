package com.san.kir.manger.dbflow.models

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.san.kir.manger.dbflow.AppDatabase

@Table(name = "sites", database = AppDatabase::class)
class Site : BaseModel {
    @PrimaryKey(autoincrement = true) @Column var id: Long = 0
    @Column var name: String = String()
    @Column var count: Int = 0

    constructor() // not delete
    constructor(name: String, count: Int) {
        this.name = name
        this.count = count
    }
}
