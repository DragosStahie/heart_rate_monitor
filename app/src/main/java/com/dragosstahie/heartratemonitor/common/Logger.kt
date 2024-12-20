package com.dragosstahie.heartratemonitor.common

import android.content.Context
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

class Logger private constructor(val sessionName: String, context: Context) {

    companion object {
        private var instance: Logger? = null

        fun initialize(sessionName: String, context: Context) {
            instance.let { currentInstance ->
                if (currentInstance == null || currentInstance.sessionName != sessionName) {
                    instance = Logger(sessionName, context)
                }
            }
        }

        fun getInstance(): Logger {
            if (instance == null) throw IllegalStateException("Logger was not initialized yet")

            return instance!!
        }
    }

    private val tag = "Logger $sessionName"
    private val logFile: File = File(context.filesDir, sessionName + System.currentTimeMillis())

    init {
        if (!logFile.exists()) {
            try {
                logFile.createNewFile()
            } catch (e: IOException) {
                Log.e(tag, "Failed to init file saving!")
            }
        }
    }

    fun debug(message: String) {
        Log.d(tag, message)
        writeLog("$tag-debug: $message")
    }

    fun error(message: String) {
        Log.e(tag, message)
        writeLog("$tag-error: $message")
    }

    private fun writeLog(message: String) {
        try {
            val buf = BufferedWriter(FileWriter(logFile, true))
            buf.append(message)
            buf.newLine()
            buf.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}