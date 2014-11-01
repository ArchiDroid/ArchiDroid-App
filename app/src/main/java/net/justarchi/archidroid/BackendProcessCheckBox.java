/*
 * ========================================================================
 *     _             _     _ ____            _     _
 *    / \   _ __ ___| |__ (_)  _ \ _ __ ___ (_) __| |
 *   / _ \ | '__/ __| '_ \| | | | | '__/ _ \| |/ _` |
 *  / ___ \| | | (__| | | | | |_| | | | (_) | | (_| |
 * /_/   \_\_|  \___|_| |_|_|____/|_|  \___/|_|\__,_|
 *
 * Copyright 2014 Łukasz "JustArchi" Domeradzki
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

import android.content.Context;
import android.graphics.Color;
import android.widget.CheckBox;

import java.io.File;

public final class BackendProcessCheckBox extends CheckBox {

	private final Context context;
	private final File file;
	private final String name;
	private final String process;

	private boolean isRunning;

	public BackendProcessCheckBox(final Context context, final File file) {
		super(context);
		this.context = context;
		this.file = file;
		this.name = file.getName();
		this.process = ArchiDroidUtilities.readFileOneLine(file);
		setEnabled(false);
		setText(name);
		refresh();
	}

	public BackendProcessCheckBox(final Context context) {
		super(context);
		this.context = context;
		this.file = null;
		this.name = null;
		this.process = null;
	}

	protected final String getName() {
		return name;
	}

	protected final String getProcess() {
		return process;
	}

	protected final boolean isRunning() {
		return isRunning;
	}

	protected final void refresh() {
		isRunning = ArchiDroidUtilities.processIsRunning(process);
		setChecked(isRunning);
		if (isRunning) {
			setTextColor(Color.GREEN);
		} else {
			setTextColor(Color.RED);
		}
	}
}
