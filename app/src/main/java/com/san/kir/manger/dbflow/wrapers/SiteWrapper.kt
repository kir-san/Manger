package com.san.kir.manger.dbflow.wrapers

import com.raizlabs.android.dbflow.kotlinextensions.`is`
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.list
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.kotlinextensions.where
import com.san.kir.manger.dbflow.models.Manga_Table.name
import com.san.kir.manger.dbflow.models.Site

object SiteWrapper {
    fun getAll(): List<Site> = (select from Site::class).list
    fun get(site: String): Site? = (select from Site::class where  (name `is` site)).querySingle()
}
