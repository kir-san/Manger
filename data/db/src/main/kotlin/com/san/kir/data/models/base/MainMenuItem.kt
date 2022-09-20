package com.san.kir.data.models.base

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.san.kir.core.support.MainMenuType
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "mainmenuitems")
data class MainMenuItem (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String = "",
    val isVisible: Boolean = true,
    val order: Int = 0,
    val type: MainMenuType = MainMenuType.Default,
) : Parcelable
