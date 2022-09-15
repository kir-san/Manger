package com.san.kir.data.db.typeConverters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.san.kir.data.models.base.ShikimoriManga
import com.san.kir.data.models.base.ShikimoriRate


internal class ShikimoriRateConverter {
    @TypeConverter
    fun rateToString(state: ShikimoriRate): String {
        return Gson().toJson(state)
    }

    @TypeConverter
    fun stringToRate(json: String): ShikimoriRate {
        return Gson().fromJson(json, ShikimoriRate::class.java)
    }
}

internal class ShikimoriMangaConverter {
    @TypeConverter
    fun rateToString(state: ShikimoriManga): String {
        return Gson().toJson(state)
    }

    @TypeConverter
    fun stringToRate(json: String): ShikimoriManga {
        return Gson().fromJson(json, ShikimoriManga::class.java)
    }
}
