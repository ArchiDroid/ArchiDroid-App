package net.justarchi.archidroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

import java.net.InetAddress;

public final class NetworkReceiver extends BroadcastReceiver {
    private static boolean isConnected = false;

    @Override
    public final void onReceive(final Context context, final Intent intent) {
        if (context == null || intent == null) {
            return;
        }

        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        refreshConnection(cm);
        networkHasChanged(cm);
    }

    private static final boolean isConnected(final Context context) {
        if (context == null) {
            return false;
        }

        refreshConnection(context);
        return isConnected;
    }

    private static final void refreshConnection(final ConnectivityManager cm) {
        if (cm == null) {
            return;
        }

        final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnected();
    }

    private static final void refreshConnection(final Context context) {
        if (context == null) {
            return;
        }

        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return;
        }

        refreshConnection(cm);
    }

    private static final void networkHasChanged(final ConnectivityManager cm) {
        if (cm == null) {
            return;
        }

        final NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            return;
        }

        final String activeNetworkInfoString = activeNetworkInfo.toString();
        final Network networks[] = cm.getAllNetworks();
        for (byte i = 0; i < networks.length; i++) {
            if (networks[i] == null || !cm.getNetworkInfo(networks[i]).toString().equals(activeNetworkInfoString)) {
                continue;
            }

            final StringBuilder sb = new StringBuilder("CONNECTIVITY_CHANGE " + networks[i].toString());
            for (InetAddress dns : cm.getLinkProperties(networks[i]).getDnsServers()) {
                sb.append(" " + dns.getHostAddress());
            }

            ArchiDroid.onEvent(sb.toString());
            break;
        }
    }
}
