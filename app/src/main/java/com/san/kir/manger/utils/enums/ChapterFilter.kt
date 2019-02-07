package com.san.kir.manger.utils.enums

enum class ChapterFilter {
    ALL_READ_ASC {
        override fun inverse() = ALL_READ_DESC
    },
    NOT_READ_ASC {
        override fun inverse() = NOT_READ_DESC
    },
    IS_READ_ASC {
        override fun inverse() = IS_READ_DESC
    },
    ALL_READ_DESC {
        override fun inverse() = ALL_READ_ASC
    },
    NOT_READ_DESC {
        override fun inverse() = NOT_READ_ASC
    },
    IS_READ_DESC {
        override fun inverse() = IS_READ_ASC
    };

    abstract fun inverse(): ChapterFilter
}
