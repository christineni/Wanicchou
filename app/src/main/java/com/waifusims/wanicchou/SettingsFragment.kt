package com.waifusims.wanicchou


import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.ListPreference
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class SettingsFragment : PreferenceFragmentCompat(),
                         SharedPreferences.OnSharedPreferenceChangeListener {

    private var dictionaryListPref: ListPreference? = null
    private var matchTypeListPref: ListPreference? = null
    private var autoSaveListPref: ListPreference? = null
    private var autoDeleteListPref: ListPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dictionaryListPref = preferenceScreen
                .findPreference(context!!.getString(R.string.pref_dictionary_key)) as ListPreference
        matchTypeListPref = preferenceScreen
                .findPreference(context!!.getString(R.string.pref_match_type_key)) as ListPreference
        autoSaveListPref = preferenceScreen
                .findPreference(context!!.getString(R.string.pref_auto_save_key)) as ListPreference
        autoDeleteListPref = preferenceScreen
                .findPreference(context!!.getString(R.string.pref_auto_delete_key)) as ListPreference
    }

    override fun onResume() {
        super.onResume()
        dictionaryListPref!!.summary = dictionaryListPref!!.entry.toString()
        matchTypeListPref!!.summary = matchTypeListPref!!.entry.toString()
        autoDeleteListPref!!.summary = autoDeleteListPref!!.entry.toString()
        autoSaveListPref!!.summary = autoSaveListPref!!.entry.toString()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_search, rootKey)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key){
            context!!.getString(R.string.pref_match_type_key) -> {
                matchTypeListPref!!.summary = matchTypeListPref!!.entry.toString()
            }
            context!!.getString(R.string.pref_auto_delete_key) -> {
                autoDeleteListPref!!.summary = autoDeleteListPref!!.entry.toString()
            }
            context!!.getString(R.string.pref_auto_save_key) -> {
                autoSaveListPref!!.summary = autoSaveListPref!!.entry.toString()
            }

            context!!.getString(R.string.pref_dictionary_key) -> {
                dictionaryListPref!!.summary = dictionaryListPref!!.entry.toString()
            }

        }
    }
}