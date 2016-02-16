package net.justarchi.archidroid;

import android.text.TextUtils;
import android.util.Log;

public final class Logging {
    private static final String TAG = "ArchiDroid";

    public static final void logGenericInfo(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        Log.i(TAG, text);
    }

    public static final void logGenericDebug2(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        Log.d(TAG, text);
    }

    public static final void logGenericError(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        Log.e(TAG, text);
    }

    public static final void logNullError(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        logGenericError(text + " is null!");
    }
}
