package com.san.kir.data.db.type_converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.san.kir.data.models.base.ShikimoriManga
import com.san.kir.data.models.base.ShikimoriRate

private val gson = Gson()

internal class ShikimoriRateConverter {
    @TypeConverter
    fun rateToString(state: ShikimoriRate): String {
        return gson.toJson(state)
    }

    @TypeConverter
    fun stringToRate(json: String): ShikimoriRate {
        return gson.fromJson(json, ShikimoriRate::class.java)
    }
}

internal class ShikimoriMangaConverter {
    @TypeConverter
    fun rateToString(state: ShikimoriManga): String {
        return gson.toJson(state)
    }

    @TypeConverter
    fun stringToRate(json: String): ShikimoriManga {
        return gson.fromJson(json, ShikimoriManga::class.java)
    }
}
