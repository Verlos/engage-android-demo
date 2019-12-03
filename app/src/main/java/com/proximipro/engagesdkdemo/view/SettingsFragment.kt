package com.proximipro.engagesdkdemo.view

import android.app.AlertDialog
import android.content.Intent
import android.content.Intent.makeRestartActivityTask
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.proximipro.engage.android.Engage
import com.proximipro.engage.android.util.getEngage
import com.proximipro.engagesdkdemo.R

/*
 * Created by Birju Vachhani on 31 May 2019
 * Copyright © 2019 engage-sdk. All rights reserved.
 */

/**
 * Displays settings preference screen
 */
class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        const val API_KEY = "api_key"
        const val BEACON_UUID = "beacon_uuid"
        private const val BACKGROUND_MODE = "background_mode"
        private const val NOTIFICATION = "notification"
        const val AUTO_REFRESH = "auto_refresh"
        const val AUTO_REFRESH_INTERVAL = "auto_refresh_interval"
        private const val LOCATION_BASED_CONTENT = "location_base_content"
        private const val SNOOZE_NOTIFICATIONS = "snooze_notifications"
        private const val SNOOZE_CONTENT = "snooze_content"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_settings, rootKey)
    }

    private val engage: Engage by lazy {
        getEngage()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager.sharedPreferences?.run {
            val config = engage.config()
            edit().apply {
                putString(API_KEY, config.apiKey)
                putString(BEACON_UUID, config.beaconUUID)
                commit()
            }
            setApiKey(Engage.getApiKey(requireContext()))
            setBeaconUuid(config.beaconUUID)
            updateBackgroundScanAndNotificationIcon(this)
            updateAutoRefreshAndIntervalIcon(this)
            updateLocationBasedContentIcon(this)
            updateSnoozeNotificationsUI(this)
            updateSnoozeContentUI(this)
        }
    }

    private fun updateSnoozeContentPref(pref: SharedPreferences) {
        val value = pref.getString(SNOOZE_CONTENT, "")?.toLongOrNull() ?: 0L
        engage.config().snoozeContent(value)
        updateSnoozeContentUI(pref)
    }

    private fun updateSnoozeNotificationsPref(pref: SharedPreferences) {
        val value = pref.getString(SNOOZE_NOTIFICATIONS, "")?.toLongOrNull() ?: 0L
        engage.config().snoozeNotifications(value)
        updateSnoozeNotificationsUI(pref)
    }

    private fun setSnoozeNotificationsIcon(icon: Int) {
        findPreference<ListPreference>(SNOOZE_NOTIFICATIONS)?.setIcon(icon)
    }

    private fun setSnoozeContentIcon(icon: Int) {
        findPreference<ListPreference>(SNOOZE_CONTENT)?.setIcon(icon)
    }

    private fun updateSnoozeContentUI(pref: SharedPreferences) {
        val value = pref.getString(SNOOZE_CONTENT, "")?.toLongOrNull() ?: 0L
        if (value > 0) {
            setSnoozeContentIcon(R.drawable.ic_snooze_content_on)
        } else {
            setSnoozeContentIcon(R.drawable.ic_snooze_content_off)
        }
        findPreference<ListPreference>(SNOOZE_CONTENT)?.apply {
            this.summary = this.entry
        }
    }

    private fun updateSnoozeNotificationsUI(pref: SharedPreferences) {
        val value = pref.getString(SNOOZE_NOTIFICATIONS, "")?.toLongOrNull() ?: 0L
        if (value > 0) {
            setSnoozeNotificationsIcon(R.drawable.ic_snooze_notification_on)
        } else {
            setSnoozeNotificationsIcon(R.drawable.ic_snooze_notification_off)
        }
        findPreference<ListPreference>(SNOOZE_NOTIFICATIONS)?.apply {
            this.summary = this.entry
        }
    }

    private fun updateLocationBasedContentIcon(pref: SharedPreferences) {
        val isEnabled = pref.getBoolean(LOCATION_BASED_CONTENT, false)
        engage.config().isLocationBasedContentEnabled = isEnabled
        if (isEnabled) {
            setLocationBasedContentIcon(R.drawable.ic_location_colored)
        } else {
            setLocationBasedContentIcon(R.drawable.ic_location_gray)
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        preferenceScreen.sharedPreferences
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (sharedPreferences == null) return
        when (key) {
            API_KEY -> updateApiKey(
                pref = sharedPreferences,
                apiKey = sharedPreferences.getString(key, "").toString()
            )
            BEACON_UUID -> updateBeaconUuid(
                pref = sharedPreferences,
                uuid = sharedPreferences.getString(key, "").toString()
            )
            BACKGROUND_MODE, NOTIFICATION -> updateBackgroundScanAndNotificationIcon(
                sharedPreferences
            )
            AUTO_REFRESH_INTERVAL, AUTO_REFRESH -> updateAutoRefreshAndIntervalIcon(
                sharedPreferences
            )
            LOCATION_BASED_CONTENT -> updateLocationBasedContentIcon(sharedPreferences)
            SNOOZE_NOTIFICATIONS -> updateSnoozeNotificationsPref(sharedPreferences)
            SNOOZE_CONTENT -> updateSnoozeContentPref(sharedPreferences)
        }
    }

    private fun updateAutoRefreshAndIntervalIcon(pref: SharedPreferences) {
        val isAutoRefresh = pref.getBoolean(AUTO_REFRESH, false)
        if (isAutoRefresh) {
            setAutoRefreshIcon(R.drawable.ic_autorefresh_colored)
            setAutoRefreshIntervalIcon(R.drawable.ic_time_colored)
        } else {
            setAutoRefreshIcon(R.drawable.ic_autorefresh_gray)
            setAutoRefreshIntervalIcon(R.drawable.ic_time_gray)
        }
        updateAutoRefreshIntervalSummary()
    }

    private fun updateBackgroundScanAndNotificationIcon(pref: SharedPreferences) {
        val isBackGroundMode = pref.getBoolean(BACKGROUND_MODE, false)
        val isNotificationEnabled = pref.getBoolean(NOTIFICATION, false)
        with(engage.config()) {
            this.isBackgroundModeEnabled = isBackGroundMode
            this.isNotificationEnabled = isNotificationEnabled
        }

        if (isBackGroundMode) {
            setBackgroundScanIcon(R.drawable.ic_scan_colored)
            if (isNotificationEnabled) {
                setNotificationIcon(R.drawable.ic_notifications_colored)
            } else {
                setNotificationIcon(R.drawable.ic_notifications_off_colored)
            }
        } else {
            setBackgroundScanIcon(R.drawable.ic_scan_off)
            if (isNotificationEnabled) {
                setNotificationIcon(R.drawable.ic_notifications_gray)
            } else {
                setNotificationIcon(R.drawable.ic_notifications_off_gray)
            }
        }
    }

    private fun updateAutoRefreshIntervalSummary() {
        findPreference<ListPreference>(AUTO_REFRESH_INTERVAL)?.apply {
            this.summary = this.entry
        }
    }

    private fun setAutoRefreshIntervalIcon(@DrawableRes id: Int) {
        findPreference<ListPreference>(AUTO_REFRESH_INTERVAL)?.setIcon(id)
    }

    private fun setLocationBasedContentIcon(@DrawableRes id: Int) {
        findPreference<SwitchPreference>(LOCATION_BASED_CONTENT)?.setIcon(id)
    }

    private fun setAutoRefreshIcon(@DrawableRes id: Int) {
        findPreference<SwitchPreference>(AUTO_REFRESH)?.setIcon(id)
    }

    private fun setBackgroundScanIcon(@DrawableRes id: Int) {
        findPreference<SwitchPreference>(BACKGROUND_MODE)?.setIcon(id)
    }

    private fun setNotificationIcon(@DrawableRes id: Int) {
        findPreference<SwitchPreference>(NOTIFICATION)?.setIcon(id)
    }

    private fun updateBeaconUuid(pref: SharedPreferences, uuid: String) {
        if (uuid.isBlank() || uuid == "null") return
        if (engage.updateBeaconUUID(uuid)) {
            setBeaconUuid(uuid)
            Toast.makeText(requireContext(), "Api key changed", Toast.LENGTH_SHORT).show()
            showAppRestartDialog {
                restartApp()
            }
        } else {
            pref.edit().putString(BEACON_UUID, engage.config().beaconUUID)
            setApiKey(engage.config().apiKey)
            Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
            showApiKeyVerificationFailureDialog()
        }

    }

    private fun restartApp() {
        val packManager = requireActivity().packageManager
        val intent = packManager.getLaunchIntentForPackage(requireActivity().packageName)
        val cName = intent?.component
        val intentNew = makeRestartActivityTask(cName).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intentNew)
        Runtime.getRuntime().exit(0)
    }

    private fun updateApiKey(pref: SharedPreferences, apiKey: String) {
        if (apiKey.isBlank() || apiKey == "null") return
        engage.updateApiKey(apiKey) { isVerified ->
            if (isVerified) {
                setApiKey(apiKey)
                Toast.makeText(requireContext(), "Api key changed", Toast.LENGTH_SHORT).show()
                showAppRestartDialog {
                    restartApp()
                }
            } else {
                pref.edit().putString(API_KEY, engage.config().apiKey)
                setApiKey(engage.config().apiKey)
                Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
                showApiKeyVerificationFailureDialog()
            }
        }
    }

    private fun showApiKeyVerificationFailureDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.invalid_api_key_title))
            .setMessage(getString(R.string.invalid_api_key_desc))
            .setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showAppRestartDialog(onApply: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.restart_title))
            .setMessage(getString(R.string.restart_content))
            .setPositiveButton(getString(R.string.restart)) { _, _ ->
                onApply()
            }
            .setCancelable(false)
            .show()
    }

    private fun setApiKey(key: String) {
        findPreference<EditTextPreference>(API_KEY)?.summary = key
    }

    private fun setBeaconUuid(uuid: String) {
        findPreference<EditTextPreference>(BEACON_UUID)?.summary = uuid
    }
}