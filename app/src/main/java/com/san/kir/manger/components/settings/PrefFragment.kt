package com.san.kir.manger.components.settings

import android.os.Bundle
import android.preference.PreferenceFragment
import com.san.kir.manger.R

class PrefFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.pref_main)
    }
}
