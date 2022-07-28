package com.san.kir.data.db.typeConverters

import androidx.room.TypeConverter
import com.san.kir.data.models.base.ShikimoriManga
import com.san.kir.data.models.base.ShikimoriRate
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


internal class ShikimoriRateConverter {
    @TypeConverter
    fun rateToString(state: ShikimoriRate): String {
        return Json.encodeToString(state)
    }

    @TypeConverter
    fun stringToRate(json: String): ShikimoriRate {
        return Json.decodeFromString(json)
    }
}

internal class ShikimoriMangaConverter {
    @TypeConverter
    fun rateToString(state: ShikimoriManga): String {
        return Json.encodeToString(state)
    }

    @TypeConverter
    fun stringToRate(json: String): ShikimoriManga {
        return Json.decodeFromString(json)
    }
}
