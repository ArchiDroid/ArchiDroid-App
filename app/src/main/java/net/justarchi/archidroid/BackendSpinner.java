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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.io.File;

public final class BackendSpinner extends Spinner {

	private final Context context;
	private final File file;

	public BackendSpinner(final Context context, final File fileInitially) {
		super(context);
		this.context = context;
		this.file = fileInitially;

		setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public final void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {

				final String selection = parent.getSelectedItem().toString();
				if (!selection.equals(ArchiDroidUtilities.getSymlinkTargetName(file))) {
					new applyBackendChangeSpinner(context, "_" + file.getName() + "/" + selection, file).execute();
				}
			}

			@Override
			public final void onNothingSelected(final AdapterView<?> parent) {

			}
		});
	}

	public BackendSpinner(final Context context) {
		super(context);
		this.context = context;
		this.file = null;
	}

	protected void refresh() {
		final String target = ArchiDroidUtilities.getSymlinkTargetName(file);
		for (int i = 0; i < getCount(); i++) {
			if (getItemAtPosition(i).toString().equals(target)) {
				//ArchiDroidUtilities.log("OK, " + i);
				setSelection(i);
				break;
			}
		}
	}

	private class applyBackendChangeSpinner extends AsyncTask<Void, Void, Void> {

		private final Context context;
		private final String target;
		private final File source;
		private ProgressDialog progressDialog;

		private applyBackendChangeSpinner(final Context context, final String target, final File source) {
			this.context = context;
			this.target = target;
			this.source = source;
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
			ArchiDroidUtilities.rootDeleteFile(source);
			ArchiDroidUtilities.rootSymlink(target, source);
			ArchiDroidUtilities.rootSysRO();
			ArchiDroidUtilities.reloadINIT(source.getName());
			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			super.onPostExecute(result);
			progressDialog.dismiss();
		}
	}
}
