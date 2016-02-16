package net.justarchi.archidroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

/**
 * Created by justa on 16.02.2016.
 */
public final class BootReceiver extends BroadcastReceiver {
    @Override
    public final void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            Logging.logNullError("context || intent");
            return;
        }

        final String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            Logging.logNullError("action");
            return;
        }

        if (!action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Logging.logGenericError("Expected: ACTION_BOOT_COMPLETED | Got: " + action);
            return;
        }

        ArchiDroid.init(context);
        ArchiDroid.initBackend();
    }
}

