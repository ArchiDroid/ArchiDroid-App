/*
 * ========================================================================
 *     _             _     _ ____            _     _
 *    / \   _ __ ___| |__ (_)  _ \ _ __ ___ (_) __| |
 *   / _ \ | '__/ __| '_ \| | | | | '__/ _ \| |/ _` |
 *  / ___ \| | | (__| | | | | |_| | | | (_) | | (_| |
 * /_/   \_\_|  \___|_| |_|_|____/|_|  \___/|_|\__,_|
 *
 * Copyright 2015 Łukasz "JustArchi" Domeradzki
 * Contact: JustArchi@JustArchi.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE=2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================================
 */

package net.justarchi.archidroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

public final class NetworkReceiver extends BroadcastReceiver {

	private static boolean isConnected = false;
    private static String lastNetworkID = "";

	protected final static boolean isConnectedNow(final Context context) {
		refreshConnection(context);
		return isConnected;
	}

	protected final static void refreshConnection(final Context context) {
		final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		isConnected = activeNetwork != null && activeNetwork.isConnected();
	}

	@Override
	public final void onReceive(final Context context, final Intent intent) {
		refreshConnection(context);

        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null) {
                final Network network[] = cm.getAllNetworks();
                for (int i = 0; i < network.length; i++) {
                    if (network[i] != null && cm.getNetworkInfo(network[i]).toString().equals(networkInfo.toString()) && !network[i].toString().equals(lastNetworkID)) {
                        lastNetworkID = network[i].toString();
                        ArchiDroidUtilities.sendEvent("CONNECTIVITY_CHANGE " + lastNetworkID);
                        break;
                    }
                }
            }
        }
	}
}

