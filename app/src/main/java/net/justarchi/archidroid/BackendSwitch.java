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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import java.io.File;
import java.util.ArrayList;

public final class BackendSwitch extends Switch {

	private final Context context;
	private final File file;
	private final ArrayList<BackendSwitch> childs;

	public BackendSwitch(final Context context, final File file) {
		super(context);
		this.context = context;
		this.file = file;
		childs = new ArrayList<>();
		setText(file.getName());
		setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		setChecked(ArchiDroidUtilities.readFileOneLine(file).equals("Enabled"));

		setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
			@Override
			public final void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
				for (final BackendSwitch child : childs) {
					//ArchiDroidUtilities.log("NOTIFY: " + child.getText());
					child.notifyChild(isChecked);
				}
				new applyBackendChange(context, file, isChecked).execute();
			}
		});
	}

	public BackendSwitch(final Context context) {
		super(context);
		this.context = context;
		this.file = null;
		this.childs = null;
	}

	protected final void refresh() {
		setChecked(ArchiDroidUtilities.readFileOneLine(file).equals("Enabled"));
	}

	protected final void addChilds(final ArrayList<BackendSwitch> bss) {
		for (final BackendSwitch bs : bss) {
			childs.add(bs);
			bs.notifyChild(isEnabled() && isChecked());
		}
	}

	private void notifyChild(final boolean isParentEnabled) {
		setEnabled(isParentEnabled);
		for (final BackendSwitch child : childs) {
			child.notifyChild(isEnabled() && isChecked());
		}
	}

	private class applyBackendChange extends AsyncTask<Void, Void, Void> {

		private final Context context;
		private final File file;
		private final boolean state;
		private ProgressDialog progressDialog;

		private applyBackendChange(final Context context, final File file, final boolean state) {
			this.context = context;
			this.file = file;
			this.state = state;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = ArchiDroidUtilities.getProgressDialog(context, "Applying backend change...");
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(final Void... params) {

			ArchiDroidUtilities.rootSysRW();
			if (state) {
				ArchiDroidUtilities.rootWriteToBackendFile(file, "Enabled");
			} else {
				ArchiDroidUtilities.rootWriteToBackendFile(file, "Disabled");
			}
			ArchiDroidUtilities.rootSysRO();
			ArchiDroidUtilities.reloadINIT(file.getName());
			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			super.onPostExecute(result);
			ArchiDroidUtilities.refreshBackendProcessCheckBoxes();
			progressDialog.dismiss();
		}
	}
}
