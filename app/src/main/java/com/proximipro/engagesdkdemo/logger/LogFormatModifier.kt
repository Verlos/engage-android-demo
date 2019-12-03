package com.proximipro.engagesdkdemo.logger

/*
 * Created by Birju Vachhani on 04 June 2019
 * Copyright © 2019 engage-sdk. All rights reserved.
 */

/**
 * Format modifiers change the logcat output in terms of any combination of one or more of the following modifiers.
 *
 * To specify a format modifier, use the -v option, as follows:
 * Every Android log message has a tag and a priority associated with it.
 *
 * You can combine any format modifier with any one of the following format options:
 * brief, long, process, raw, tag, thread, threadtime, and time.
 * @property modifier String
 * @constructor
 */
sealed class LogFormatModifier(val modifier: String) {

    /**
     * Show each priority level with a different color.
     */
    object COLOR : LogFormatModifier("color")

    /**
     * Show log buffer event descriptions. This modifier affects event log buffer messages only, and has no effect on the other non-binary buffers. The event descriptions come from the event-log-tags database.
     */
    object DESCRIPTIVE : LogFormatModifier("descriptive")

    /**
     * Display time in seconds starting from Jan 1, 1970.
     */
    object EPOCH : LogFormatModifier("epoch")

    /**
     * Display time in CPU seconds starting from the last boot.
     */
    object MONOTONIC : LogFormatModifier("monotonic")

    /**
     * Ensure that any binary logging content is escaped.
     */
    object PRINTABLE : LogFormatModifier("printable")

    /**
     * If permitted by access controls, display the UID or Android ID of the logged process.
     */
    object UID : LogFormatModifier("uid")

    /**
     * Display the time with precision down to microseconds.
     */
    object USEC : LogFormatModifier("usec")

    /**
     * Display time as UTC.
     */
    object UTC : LogFormatModifier("UTC")

    /**
     * Add the year to the displayed time.
     */
    object YEAR : LogFormatModifier("year")

    /**
     * Add the local time zone to the displayed time.
     */
    object ZONE : LogFormatModifier("zone")
}