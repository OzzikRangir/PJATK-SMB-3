package com.ozzikrangir.productlist.ui.settings

import android.app.TaskStackBuilder
import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.ozzikrangir.productlist.R
import com.ozzikrangir.productlist.ui.main.MainActivity


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val alternative = preferences.getBoolean("alternative", false)
        if (alternative)
            setTheme(R.style.Theme_ProductListAlternative)
        else
            setTheme(R.style.Theme_ProductList)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val sharedPreferenceChangeListener =
            OnSharedPreferenceChangeListener { prefs, key ->
                when (key){
                    "darkmode" -> {
                        val darkMode = preferences.getBoolean("darkmode", false)
                        if (darkMode)
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        else
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        delegate.applyDayNight()
                    }
                    "alternative" -> {


                        recreate()
                    }

                    else -> {

                    }
                }

            }
        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

    }
}