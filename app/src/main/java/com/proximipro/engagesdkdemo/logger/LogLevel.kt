package com.proximipro.engagesdkdemo.logger

/*
 * Created by Birju Vachhani on 04 June 2019
 * Copyright © 2019 engage-sdk. All rights reserved.
 */

sealed class LogLevel(val level: String) {
    object VERBOSE : LogLevel("*:V")
    object DEBUG : LogLevel("*:D")
    object INFO : LogLevel("*:I")
    object WARNING : LogLevel("*:W")
    object ERROR : LogLevel("*:E")
    object FATAL : LogLevel("*:F")
    object SILENT : LogLevel("*:S")
}