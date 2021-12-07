package com.san.kir.manger.utils.enums

import com.san.kir.core.support.ChapterFilter

interface Filter {
    val allRead: ChapterFilter
    val isRead: ChapterFilter
    val notRead: ChapterFilter
    fun reverse(): Filter
    val isAsc: Boolean
}

object FilterAsc : Filter {
    override val allRead = ChapterFilter.ALL_READ_ASC
    override val isRead = ChapterFilter.IS_READ_ASC
    override val notRead = ChapterFilter.NOT_READ_ASC
    override fun reverse() = FilterDesc
    override val isAsc = true
}

object FilterDesc : Filter {
    override val allRead = ChapterFilter.ALL_READ_DESC
    override val isRead = ChapterFilter.IS_READ_DESC
    override val notRead = ChapterFilter.NOT_READ_DESC
    override fun reverse() = FilterAsc
    override val isAsc = false
}
