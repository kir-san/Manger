package com.san.kir.data.models.base

import androidx.room.Entity
import androidx.room.PrimaryKey

@Deprecated("Больше не используется", level = DeprecationLevel.WARNING)
@Entity(tableName = "latestChapters")
class LatestChapter {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var manga = ""
    var name = ""
    var date = ""
    var path = ""
    var site = ""

}
