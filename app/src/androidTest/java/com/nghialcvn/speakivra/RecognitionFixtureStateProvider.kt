package com.nghialcvn.speakivra

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle

/** The instrumentation runner and the fixture service run in different processes. */
class RecognitionFixtureStateProvider : ContentProvider() {
    override fun onCreate() = true
    override fun call(method: String, arg: String?, extras: Bundle?): Bundle {
        when (method) {
            "configure" -> {
                RecognitionProtocolService.ignorePipe = arg.toBoolean()
                RecognitionProtocolService.waitForEnd = false
                RecognitionProtocolService.directStarts.set(0)
                RecognitionProtocolService.liveBytes.set(0)
            }
            "answer" -> RecognitionProtocolService.answer = requireNotNull(arg)
            "waitForEnd" -> RecognitionProtocolService.waitForEnd = true
        }
        return Bundle().apply {
            putInt("directStarts", RecognitionProtocolService.directStarts.get())
            putInt("liveBytes", RecognitionProtocolService.liveBytes.get())
        }
    }
    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0
}
