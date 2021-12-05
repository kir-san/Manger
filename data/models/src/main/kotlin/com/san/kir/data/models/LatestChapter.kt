package com.san.kir.data.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.san.kir.data.models.Chapter

@Entity(tableName = "latestChapters")
class LatestChapter {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var manga = ""
    var name = ""
    var date = ""
    var path = ""
    var site = ""

     constructor()
    @Ignore constructor(chapter: Chapter) {
        manga = chapter.manga
        name = chapter.name
        date = chapter.date
        site = chapter.link
        path = chapter.path
    }
}
