package com.san.kir.manger.dbflow.wrapers

import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.list
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.sql.language.Delete
import com.san.kir.manger.dbflow.models.Category
import com.san.kir.manger.utils.log

object CategoryWrapper {
    fun getCategories(): List<Category> = (select from Category::class).list
    suspend fun asyncGetCategories() = (select from Category::class).list

    fun updateCategories(list: List<Category>) {
        Delete.table(Category::class.java)
        for (category in list) {
            log = ("${category.name} isVisible = ${category.isVisible}")
        }
        list.forEach(Category::insert)
    }
}

fun toStringList(categories: List<Category>): List<String> {
    val stringList = mutableListOf<String>()
    categories.forEach { cat: Category -> stringList.add(cat.name) }
    return stringList
}
