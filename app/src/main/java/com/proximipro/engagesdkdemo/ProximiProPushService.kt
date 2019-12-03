package com.proximipro.engagesdkdemo

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.proximipro.engagesdkdemo.helper.Constants
import timber.log.Timber

/*
 * Created by Birju Vachhani on 16 September 2019
 * Copyright © 2019 engage-sdk. All rights reserved.
 */

/**
 * Firebase Messaging service for receiving push notifications
 */
class ProximiProPushService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.e("ProximiProPushService: Received push message: $message")
        val intent = Intent(Constants.PUSH_BROADCAST_INTENT_FILTER).apply {
            putExtra(Constants.INTENT_KEY_PUSH, message)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onNewToken(token: String) {
        Timber.e("Received new firebase token")
        getSharedPreferences(Constants.PREF_FIREBASE, Context.MODE_PRIVATE).edit()
            .putString(Constants.PREF_KEY_TOKEN, token)
            .putBoolean(Constants.PREF_KEY_TOKEN_IS_SYNCED, false)
            .apply()
    }
}