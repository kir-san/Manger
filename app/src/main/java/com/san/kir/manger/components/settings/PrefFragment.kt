package com.san.kir.manger.components.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.san.kir.manger.R

class PrefFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        addPreferencesFromResource(R.xml.pref_main)
    }
}
