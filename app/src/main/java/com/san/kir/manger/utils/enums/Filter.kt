package com.san.kir.manger.utils.enums

interface Filter {
    val allRead: com.san.kir.core.support.ChapterFilter
    val isRead: com.san.kir.core.support.ChapterFilter
    val notRead: com.san.kir.core.support.ChapterFilter
    fun reverse(): Filter
    val isAsc: Boolean
}

object FilterAsc : Filter {
    override val allRead = com.san.kir.core.support.ChapterFilter.ALL_READ_ASC
    override val isRead = com.san.kir.core.support.ChapterFilter.IS_READ_ASC
    override val notRead = com.san.kir.core.support.ChapterFilter.NOT_READ_ASC
    override fun reverse() = FilterDesc
    override val isAsc = true
}

object FilterDesc : Filter {
    override val allRead = com.san.kir.core.support.ChapterFilter.ALL_READ_DESC
    override val isRead = com.san.kir.core.support.ChapterFilter.IS_READ_DESC
    override val notRead = com.san.kir.core.support.ChapterFilter.NOT_READ_DESC
    override fun reverse() = FilterAsc
    override val isAsc = false
}
