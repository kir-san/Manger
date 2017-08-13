package com.san.kir.manger.dbflow.models

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.san.kir.manger.dbflow.AppDatabase

@Table(name = "categories", database = AppDatabase::class)
class Category : BaseModel {
    @PrimaryKey(autoincrement = true) @Column var id: Long = 0
    @Column var name: String = String()
    @Column var order: Int = 0
    @Column var isVisible: Boolean = true
    @Column var typeSort: String? = String()
    @Column var isReverseSort: Boolean = false

    constructor() // not delete
    constructor(name: String, order: Int) {
        this.name = name
        this.order = order
    }
}
