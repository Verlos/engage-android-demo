package com.proximipro.engagesdkdemo.logger

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException

/*
 * Created by Birju Vachhani on 04 June 2019
 * Copyright © 2019 engage-sdk. All rights reserved.
 */

class LogCollector {

    var logLevel: LogLevel = LogLevel.DEBUG
    private val formatModifiers: ArrayList<String> = arrayListOf("color")
    var outputFormat: LogOutputFormat? = null

    fun setFormatModifier(vararg modifiers: LogFormatModifier) {
        formatModifiers.clear()
        formatModifiers.addAll(modifiers.map { it.modifier })
    }

    fun addFormatModifier(logFormatModifier: LogFormatModifier) {
        formatModifiers.add(logFormatModifier.modifier)
    }

    fun clearLog() {
        try {
            Runtime.getRuntime().exec("logcat -c")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /*suspend fun dumpLog(file: File) {
        withContext(Dispatchers.IO) {
            runCatching {
                val command = getDumpCommand(file)
                Log.e("LOG COMMAND", command)
                val process = Runtime.getRuntime().exec(command)
                process.waitFor()
            }.onFailure {
                it.printStackTrace()
            }
        }
    }*/

    suspend fun dumpLog(reportFile: File): File {
        return withContext(Dispatchers.Default) {
            val list = try {
//                val command = getDumpCommand()
                val command = "logcat -d"
                Log.e("LOG COMMAND", command)
                val process = Runtime.getRuntime().exec(command)
                val reader = process.inputStream.bufferedReader()
                var line = reader.readLine()
                val lines = arrayListOf<String>()
                while ((line) != null && line.isNotBlank()) {
                    lines.add(line)
                    line = reader.readLine()
                }
                process.waitFor()
                Log.e("dumpLog", "collected log, reading from input stream")
//                Log.e("dumpLog",process.errorStream.bufferedReader().readLines().joinToString(" "))
                lines
            } catch (e: IOException) {
                e.printStackTrace()
                arrayListOf<String>()
            }
            Log.e("dumpLog", "processing logs to generate html report")
            processLog(reportFile, list)
            reportFile
        }
    }

    private fun processLog(reportFile: File, logs: List<String>) {
        val fout = FileWriter(reportFile)
        val data = generateHtml(logs)
        fout.write(data)
        fout.close()
        Log.e("processLog", "Sending Device log")
        val newData = data.replace("[0m", "")
        reportFile.writeText(newData)
        Log.e("processLog", "Device log successfully sent")
    }

    private fun getDumpCommand(file: File? = null): String {
        return StringBuilder().apply {
            append("logcat")
            append(" ${logLevel.level}")
            append(" -d")
            if (file != null) append(" -f $file")
            if (outputFormat != null) append(" -v ${outputFormat?.format}")
            formatModifiers.forEach { modifier -> append(" -v $modifier") }
        }.toString()
    }

    private fun generateHtml(logs: List<String>): String {
        val header =
            """<html><head><title>Device Log</title></head><body style="background-color:#2e2e2e">"""
        val footer = """</body></html>"""
        val body = logs.joinToString("\n") { log ->
            "<div style='color:${getLogColor(log)}'>$log</div>"
        }
        return StringBuilder().apply { append(header, body, footer) }.toString()
    }

    private fun getLogColor(log: String): String {
        val tokens = log.split(" ")
        if (tokens.isEmpty() || tokens.size <= 6) return "#FFFFFF"
        return when (tokens[6]) {
            "V" -> "#BBBBBB"
            "D" -> "#6897BB"
            "I" -> "#94B479"
            "W" -> "#D8D228"
            "E" -> "#FF6B68"
            else -> "#9876AA"
        }
    }
}