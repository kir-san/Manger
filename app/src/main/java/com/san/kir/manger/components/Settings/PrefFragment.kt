package com.san.kir.manger.components.Settings

import android.os.Bundle
import android.support.v7.preference.ListPreference
import android.support.v7.preference.PreferenceFragmentCompat
import com.san.kir.manger.R.xml

class PrefFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        addPreferencesFromResource(xml.pref_main)

        val portSpan = findPreference("portrait_span") as ListPreference
        var span = arrayListOf<String>()
        (2..5).mapTo(span) { "$it" }
        portSpan.entries = span.toTypedArray()
        portSpan.entryValues = span.toTypedArray()

        val landSpan = findPreference("landscape_span") as ListPreference
        span = arrayListOf<String>()
        (3..6).mapTo(span) { "$it" }
        landSpan.entries = span.toTypedArray()
        landSpan.entryValues = span.toTypedArray()
    }
}
