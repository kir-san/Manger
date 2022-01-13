package com.san.kir.data.db.type_converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.san.kir.data.models.base.ShikimoriAccount

class ShikimoriRateConverter {
    @TypeConverter
    fun rateToString(state: ShikimoriAccount.Rate): String {
        return Gson().toJson(state)
    }

    @TypeConverter
    fun stringToRate(json: String): ShikimoriAccount.Rate {
        return Gson().fromJson(json, ShikimoriAccount.Rate::class.java)
    }
}

class ShikimoriMangaConverter {
    @TypeConverter
    fun rateToString(state: ShikimoriAccount.Manga): String {
        return Gson().toJson(state)
    }

    @TypeConverter
    fun stringToRate(json: String): ShikimoriAccount.Manga {
        return Gson().fromJson(json, ShikimoriAccount.Manga::class.java)
    }
}
