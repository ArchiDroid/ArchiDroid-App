/*
 * ========================================================================
 *     _             _     _ ____            _     _
 *    / \   _ __ ___| |__ (_)  _ \ _ __ ___ (_) __| |
 *   / _ \ | '__/ __| '_ \| | | | | '__/ _ \| |/ _` |
 *  / ___ \| | | (__| | | | | |_| | | | (_) | | (_| |
 * /_/   \_\_|  \___|_| |_|_|____/|_|  \___/|_|\__,_|
 *
 * Copyright 2014-2015 Łukasz "JustArchi" Domeradzki
 * Contact: JustArchi@JustArchi.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================================
 */

package net.justarchi.archidroid;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

public final class ArchiDroidUpdateForegroundService extends Service {

	private PowerManager.WakeLock mWakeLock;

	@Override
	public final IBinder onBind(final Intent intent) {
		return null;
	}

	@Override
	public final int onStartCommand(final Intent intent, final int flags, final int startId) {
		super.onStartCommand(intent, flags, startId);
		start();
		return START_NOT_STICKY;
	}

	@Override
	public final void onDestroy() {
		stop();
		super.onDestroy();
	}

	public final void start() {

		// Make sure there are no zombie wakelocks, this should actually never happen
		if (mWakeLock != null && mWakeLock.isHeld()) {
			mWakeLock.release();
		}

		final PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ArchiDroidUpdateForegroundService");
		mWakeLock.acquire();

		//Intent notificationIntent = new Intent(this, Main.class);
		//notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		//notificationIntent.setAction(Intent.ACTION_MAIN);
		//PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification note = new Notification.Builder(this)
				//.setContentIntent(pendingIntent)
				.setContentTitle(getString(R.string.stringUpdateService))
				.setContentText(getString(R.string.stringUpdateServiceInProgress))
				.setSmallIcon(R.mipmap.ic_launcher)
				.build();
		startForeground(1337, note);
	}

	public final void stop() {
		mWakeLock.release();
		stopForeground(true);
	}

}
