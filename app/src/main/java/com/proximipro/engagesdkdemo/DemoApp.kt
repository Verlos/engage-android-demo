package com.proximipro.engagesdkdemo

import android.app.Application
import timber.log.Timber

/*
 * Created by Birju Vachhani on 06 May 2019
 * Copyright © 2019 engage-sdk. All rights reserved.
 */

/**
 * Application class used to initialize everything when the app starts
 */
class DemoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}

