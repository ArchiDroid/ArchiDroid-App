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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;

public final class ArchiDroidUpdateAlarm extends WakefulBroadcastReceiver {
	private AlarmManager alarmMgr;
	private PendingIntent alarmIntent;

	@Override
	public final void onReceive(final Context context, final Intent intent) {
		startWakefulService(context, new Intent(context, ArchiDroidUpdateService.class));
	}

	protected final void setAlarm(final Context context) {
		alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		final Intent intent = new Intent(context, ArchiDroidUpdateAlarm.class);
		alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

		alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_DAY, alarmIntent);
		//alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 10000, alarmIntent); // THIS IS FOR DEBUG TESTS ONLY, every 10 seconds while device is turned on
	}

	protected final void cancelAlarm(final Context context) {
		if (alarmMgr!= null) {
			alarmMgr.cancel(alarmIntent);
		}
	}
}
