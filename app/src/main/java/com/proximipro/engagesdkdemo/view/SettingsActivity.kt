package com.proximipro.engagesdkdemo.view

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.proximipro.engage.android.util.getEngage
import com.proximipro.engagesdkdemo.R
import com.proximipro.engagesdkdemo.helper.Constants

/**
 * Controller for Settings screen
 */
class SettingsActivity : AppCompatActivity() {

    companion object {
        private const val SETTINGS_RESULT_CODE = 135
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        getSharedPreferences(Constants.DEFAULT_PREF, Context.MODE_PRIVATE)?.apply {
            val config = getEngage().config()
            edit().apply {
                putString(SettingsFragment.API_KEY, config.apiKey)
                putString(SettingsFragment.BEACON_UUID, config.beaconUUID)
                commit()
            }
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        setResult(SETTINGS_RESULT_CODE)
        super.onBackPressed()
    }
}
