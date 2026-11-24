package com.nghialcvn.speakivra;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

/** Test-APK-only Binder fixture, NOT a recognizer. Java keeps its separate service
 * process independent of the target APK's Kotlin/Compose runtime. */
public class RecognitionProtocolService extends RecognitionService {
    static volatile boolean ignorePipe, nativeResultsWithPipe, waitForEnd;
    static volatile String answer = "Hello there.";
    static final AtomicInteger directStarts = new AtomicInteger();
    static final AtomicInteger liveBytes = new AtomicInteger();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private volatile Callback active;
    private ParcelFileDescriptor input;
    @Override protected void onStartListening(Intent intent, Callback callback) {
        active = callback;
        try {
            callback.readyForSpeech(new Bundle());
            ParcelFileDescriptor source = intent.getParcelableExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE);
            if (source != null) {
                input = source;
                if (ignorePipe) {
                    if (nativeResultsWithPipe) {
                        handler.postDelayed(() -> partial(callback), 4500);
                        handler.postDelayed(() -> complete(callback), 5000);
                    } else callback.error(SpeechRecognizer.ERROR_CLIENT);
                    return;
                }
                new Thread(() -> {
                    try (InputStream stream = new ParcelFileDescriptor.AutoCloseInputStream(source)) {
                        byte[] bytes = new byte[1024];
                        int received = 0;
                        while (active == callback) {
                            int count = stream.read(bytes);
                            if (count < 0) { complete(callback); break; }
                            received += count;
                            liveBytes.addAndGet(count);
                            if (received >= 8192) handler.post(() -> partial(callback));
                            if (received >= 16000 && !waitForEnd) { complete(callback); break; }
                        }
                    } catch (Exception ignored) { /* cancel closes the source */ }
                }, "test-recognition-pipe").start();
            } else {
                directStarts.incrementAndGet();
                handler.postDelayed(() -> partial(callback), 100);
                handler.postDelayed(() -> complete(callback), 500);
            }
        } catch (Exception error) { throw new IllegalStateException(error); }
    }
    private static Bundle result(String text) {
        Bundle result = new Bundle();
        result.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION,
            new ArrayList<>(Collections.singletonList(text)));
        return result;
    }
    private void partial(Callback callback) {
        if (active == callback) {
            try { callback.partialResults(result("Hello")); }
            catch (Exception error) { throw new IllegalStateException(error); }
        }
    }
    private void complete(Callback callback) {
        handler.post(() -> {
            if (active == callback) {
                try {
                    callback.endOfSpeech();
                    if (!ignorePipe && android.os.Build.VERSION.SDK_INT >= 33) {
                        callback.segmentResults(result(answer));
                        callback.endOfSegmentedSession();
                    } else callback.results(result(answer));
                    active = null;
                } catch (Exception error) { throw new IllegalStateException(error); }
            }
        });
    }
    @Override protected void onStopListening(Callback callback) { complete(callback); }
    @Override protected void onCancel(Callback callback) {
        active = null;
        try { if (input != null) input.close(); } catch (Exception ignored) { }
        input = null;
    }
}
