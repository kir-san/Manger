package com.san.kir.manger.utils.extensions

import androidx.annotation.StringRes

fun SharedPreferencesHolder.boolean(key: String, defValue: Boolean) =
    BooleanPreferenceDelegate(this, key, defValue)

fun SharedPreferencesHolder.boolean(key: String, defValue: String) =
    boolean(key, defValue == "true")

fun SharedPreferencesHolder.boolean(@StringRes key: Int, @StringRes defValue: Int) =
    BooleanPreference2Delegate(this, key, defValue)

fun SharedPreferencesHolder.string(key: String, defValue: String) =
    StringPreferenceDelegate(this, key, defValue)

fun SharedPreferencesHolder.string(key: Int, defValue: Int) =
    StringPreference2Delegate(this, key, defValue)
