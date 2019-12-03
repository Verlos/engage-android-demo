package com.proximipro.engagesdkdemo.logger

/*
 * Created by Birju Vachhani on 04 June 2019
 * Copyright © 2019 engage-sdk. All rights reserved.
 */

sealed class LogOutputFormat(val format: String) {

    object BRIEF : LogOutputFormat("brief")
    object LONG : LogOutputFormat("long")
    object PROCESS : LogOutputFormat("process")
    object RAW : LogOutputFormat("raw")
    object TAG : LogOutputFormat("tag")
    object THREAD : LogOutputFormat("thread")
    object THREADTIME : LogOutputFormat("threadtime")
    object TIME : LogOutputFormat("time")
}