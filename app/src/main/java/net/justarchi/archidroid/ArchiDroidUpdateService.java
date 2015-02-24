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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public final class ArchiDroidUpdateService extends IntentService {
	public ArchiDroidUpdateService() {
		super("ArchiDroidUpdateService");
	}

	@Override
	protected final void onHandleIntent(final Intent intent) {
		checkForUpdate(this);
		ArchiDroidUpdateAlarm.completeWakefulIntent(intent);
	}

	protected final void checkForUpdate(final Context context) {
		if (ArchiDroidUtilities.isConnected(context)) {
			if (ArchiDroidUtilities.isNewArchiDroidAvailable()) {
				ArchiDroidUtilities.showNotification(context, getString(R.string.stringUpdateService), getString(R.string.stringUpdateServiceNewAvailable));
			}
		}
	}
}