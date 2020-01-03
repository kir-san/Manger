package com.san.kir.manger.utils.extensions

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface SharedPreferencesHolder {
    val preferences: SharedPreferences
    val editor: SharedPreferences.Editor
    val ctx: Context

    fun finishEditor() {
        editor.apply()
    }
}

abstract class CommonPreferenceDelegate<T> : ReadWriteProperty<Any, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return getValue()
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        setValue(value)
    }

    abstract fun getValue(): T
    abstract fun setValue(value: T)
}

class BooleanPreferenceDelegate(
    private val holder: SharedPreferencesHolder,
    val key: String, private val defValue: Boolean
) : CommonPreferenceDelegate<Boolean>() {

    override fun getValue(): Boolean {
        return holder.preferences.getBoolean(key, defValue)
    }

    override fun setValue(value: Boolean) {
        holder.editor.putBoolean(key, value)
    }
}

class BooleanPreference2Delegate(
    private val holder: SharedPreferencesHolder,
    @StringRes keyRes: Int, @StringRes defValueRes: Int
) : CommonPreferenceDelegate<Boolean>() {

    private val key by lazy { holder.ctx.getString(keyRes) }
    private val defValue by lazy { holder.ctx.getString(defValueRes) == "true" }

    override fun getValue(): Boolean {
        return holder.preferences.getBoolean(key, defValue)
    }

    override fun setValue(value: Boolean) {
        holder.editor.putBoolean(key, value)
    }
}

class StringPreferenceDelegate(
    private val holder: SharedPreferencesHolder,
    val key: String,
    private val defValue: String
) : CommonPreferenceDelegate<String>() {

    override fun getValue(): String {
        return holder.preferences.getString(key, defValue) ?: ""
    }

    override fun setValue(value: String) {
        holder.editor.putString(key, value)
    }
}

class StringPreference2Delegate(
    private val holder: SharedPreferencesHolder,
    @StringRes keyRes: Int, @StringRes defValueRes: Int
) : CommonPreferenceDelegate<String>() {

    private val key by lazy { holder.ctx.getString(keyRes) }
    private val defValue by lazy { holder.ctx.getString(defValueRes) }

    override fun getValue(): String {
        return holder.preferences.getString(key, defValue) ?: ""
    }

    override fun setValue(value: String) {
        holder.editor.putString(key, value)
    }
}

class StringSetPreference2Delegate(
    private val holder: SharedPreferencesHolder,
    @StringRes keyRes: Int, @ArrayRes defValueRes: Int
) : CommonPreferenceDelegate<Set<String>>() {

    private val key by lazy { holder.ctx.getString(keyRes) }
    private val defValue by lazy { holder.ctx.resources.getStringArray(defValueRes) }

    override fun getValue(): Set<String> {
        return holder.preferences.getStringSet(key, defValue.toSet()) ?: emptySet()
    }

    override fun setValue(value: Set<String>) {
        holder.editor.putStringSet(key, value)
    }
}


