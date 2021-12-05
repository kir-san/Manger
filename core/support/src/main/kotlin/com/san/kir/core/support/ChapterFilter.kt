package com.san.kir.core.support

enum class ChapterFilter {
    ALL_READ_ASC {
        override val isAll = true
        override val isRead = false
        override val isNot = false
        override val isAsc = true
        override fun toAll() = ALL_READ_ASC
        override fun toRead() = IS_READ_ASC
        override fun toNot() = NOT_READ_ASC
        override fun inverse() = ALL_READ_DESC
    },
    NOT_READ_ASC {
        override val isAll = false
        override val isRead = false
        override val isNot = true
        override val isAsc = true
        override fun toAll() = ALL_READ_ASC
        override fun toRead() = IS_READ_ASC
        override fun toNot() = NOT_READ_ASC
        override fun inverse() = NOT_READ_DESC
    },
    IS_READ_ASC {
        override val isAll = false
        override val isRead = true
        override val isNot = false
        override val isAsc = true
        override fun toAll() = ALL_READ_ASC
        override fun toRead() = IS_READ_ASC
        override fun toNot() = NOT_READ_ASC
        override fun inverse() = IS_READ_DESC
    },
    ALL_READ_DESC {
        override val isAll = true
        override val isRead = false
        override val isNot = false
        override val isAsc = false
        override fun toAll() = ALL_READ_DESC
        override fun toRead() = IS_READ_DESC
        override fun toNot() = NOT_READ_DESC
        override fun inverse() = ALL_READ_ASC
    },
    NOT_READ_DESC {
        override val isAll = false
        override val isRead = false
        override val isNot = true
        override val isAsc = false
        override fun toAll() = ALL_READ_DESC
        override fun toRead() = IS_READ_DESC
        override fun toNot() = NOT_READ_DESC
        override fun inverse() = NOT_READ_ASC
    },
    IS_READ_DESC {
        override val isAll = false
        override val isRead = true
        override val isNot = false
        override val isAsc = false
        override fun toAll() = ALL_READ_DESC
        override fun toRead() = IS_READ_DESC
        override fun toNot() = NOT_READ_DESC
        override fun inverse() = IS_READ_ASC
    };

    abstract fun inverse(): ChapterFilter
    abstract val isAll: Boolean
    abstract val isRead: Boolean
    abstract val isNot: Boolean
    abstract val isAsc: Boolean
    abstract fun toAll(): ChapterFilter
    abstract fun toRead(): ChapterFilter
    abstract fun toNot(): ChapterFilter
}
