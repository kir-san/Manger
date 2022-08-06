package com.san.kir.data.models.base

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.san.kir.core.support.MainMenuType
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "mainmenuitems")
data class MainMenuItem (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    var name: String = "",
    var isVisible: Boolean = true,
    var order: Int = 0,
    var type: MainMenuType = MainMenuType.Default,
) : Parcelable
