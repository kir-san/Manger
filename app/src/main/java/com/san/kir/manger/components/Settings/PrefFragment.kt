package com.san.kir.manger.components.Settings

import android.os.Bundle
import android.preference.ListPreference
import android.preference.PreferenceFragment
import com.san.kir.manger.R.xml

class PrefFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(xml.pref_main)

        val portSpan = findPreference("portrait_span") as ListPreference
        val port = (2..5).map { "$it" }
        portSpan.entries = port.toTypedArray()
        portSpan.entryValues = port.toTypedArray()

        val landSpan = findPreference("landscape_span") as ListPreference
        val land = (3..6).map { "$it" }
        landSpan.entries = land.toTypedArray()
        landSpan.entryValues = land.toTypedArray()
    }
}
