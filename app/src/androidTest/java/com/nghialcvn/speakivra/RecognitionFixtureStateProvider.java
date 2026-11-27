package com.nghialcvn.speakivra;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

/** Cross-process fixture controls. Included only in the separate test APK. */
public class RecognitionFixtureStateProvider extends ContentProvider {
    @Override public boolean onCreate() { return true; }
    @Override public Bundle call(String method, String arg, Bundle extras) {
        switch (method) {
            case "configure":
                RecognitionProtocolService.ignorePipe = Boolean.parseBoolean(arg);
                RecognitionProtocolService.waitForEnd = false;
                RecognitionProtocolService.nativeResultsWithPipe = false;
                RecognitionProtocolService.directStarts.set(0);
                RecognitionProtocolService.liveBytes.set(0);
                RecognitionProtocolService.lastLanguage = "";
                break;
            case "answer": RecognitionProtocolService.answer = arg; break;
            case "nativeResultsWithPipe": RecognitionProtocolService.nativeResultsWithPipe = true; break;
            case "waitForEnd": RecognitionProtocolService.waitForEnd = true; break;
        }
        Bundle result = new Bundle();
        result.putString("language", RecognitionProtocolService.lastLanguage);
        result.putInt("directStarts", RecognitionProtocolService.directStarts.get());
        result.putInt("liveBytes", RecognitionProtocolService.liveBytes.get());
        return result;
    }
    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] args, String order) { return null; }
    @Override public String getType(Uri uri) { return null; }
    @Override public Uri insert(Uri uri, ContentValues values) { return null; }
    @Override public int delete(Uri uri, String selection, String[] args) { return 0; }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] args) { return 0; }
}
