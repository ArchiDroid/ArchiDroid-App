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

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import java.util.ArrayList;

public final class ArchiDroidFragmentLinux extends Fragment {

	private final ArrayList<DialogFragment> dialogList = new ArrayList<>();
	private final String commandInstall = "ARCHIDROID_LINUX --install";
	private final String commandUninstall = "ARCHIDROID_LINUX --uninstall";
	private final String commandMount = "ARCHIDROID_LINUX --mount";
	private final String commandUnmount = "ARCHIDROID_LINUX --unmount";
	private final String commandLaunchShell = "ARCHIDROID_LINUX --shell";
	private boolean isActive = true;
	private Button buttonInstall;
	private Button buttonUninstall;
	private Button buttonMount;
	private Button buttonUnmount;

	private CheckBox checkBoxInstalled;
	private CheckBox checkBoxMounted;

	private Button buttonLaunchShell;

	public ArchiDroidFragmentLinux() {
	}

	@Override
	public final void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);

		isActive = false;
	}

	@Override
	public final void onResume() {
		super.onResume();

		isActive = true;
		while (!dialogList.isEmpty())
			dialogList.remove(0).show(getFragmentManager(), null);
	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_linux, container, false);
	}

	@Override
	public final void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		buttonInstall = (Button) getView().findViewById(R.id.buttonInstall);
		buttonInstall.setOnClickListener(new Button.OnClickListener() {
			@Override
			public final void onClick(final View v) {
				new installLinux(v.getContext(), ArchiDroidUtilities.getArchiDroidLinux()).execute();
			}
		});

		buttonUninstall = (Button) getView().findViewById(R.id.buttonUninstall);
		buttonUninstall.setOnClickListener(new Button.OnClickListener() {
			@Override
			public final void onClick(final View v) {
				new uninstallLinux(v.getContext(), ArchiDroidUtilities.getArchiDroidLinux()).execute();
			}
		});

		buttonMount = (Button) getView().findViewById(R.id.buttonMount);
		buttonMount.setOnClickListener(new Button.OnClickListener() {
			@Override
			public final void onClick(final View v) {
				new mountLinux(v.getContext(), ArchiDroidUtilities.getArchiDroidLinux()).execute();
			}
		});

		buttonUnmount = (Button) getView().findViewById(R.id.buttonUnmount);
		buttonUnmount.setOnClickListener(new Button.OnClickListener() {
			@Override
			public final void onClick(final View v) {
				new unmountLinux(v.getContext(), ArchiDroidUtilities.getArchiDroidLinux()).execute();
			}
		});

		checkBoxInstalled = (CheckBox) getView().findViewById(R.id.checkBoxInstalled);
		checkBoxMounted = (CheckBox) getView().findViewById(R.id.checkBoxMounted);

		buttonLaunchShell = (Button) getView().findViewById(R.id.buttonLaunchShell);
		buttonLaunchShell.setOnClickListener(new Button.OnClickListener() {
			@Override
			public final void onClick(final View v) {
				new runShell(v.getContext(), ArchiDroidUtilities.getArchiDroidLinux()).execute();
			}
		});

		if (!ArchiDroidUtilities.isSystemFullySupported()) {
			buttonInstall.setEnabled(false);
			buttonUninstall.setEnabled(false);
			buttonMount.setEnabled(false);
			buttonUnmount.setEnabled(false);
			buttonLaunchShell.setEnabled(false);
		} else {
			refreshLinuxStatus();
		}

	}

	private void refreshLinuxStatus() {
		if (ArchiDroidUtilities.getArchiDroidLinuxInstalled()) {
			checkBoxInstalled.setChecked(true);
			checkBoxInstalled.setTextColor(Color.GREEN);
			buttonInstall.setEnabled(false);
			buttonUninstall.setEnabled(true);
			if (ArchiDroidUtilities.getArchiDroidLinuxMounted()) {
				checkBoxMounted.setChecked(true);
				checkBoxMounted.setTextColor(Color.GREEN);
				buttonLaunchShell.setEnabled(true);
				buttonMount.setEnabled(false);
				buttonUnmount.setEnabled(true);
			} else {
				checkBoxMounted.setChecked(false);
				checkBoxMounted.setTextColor(Color.RED);
				buttonLaunchShell.setEnabled(false);
				buttonMount.setEnabled(true);
				buttonUnmount.setEnabled(false);
			}
		} else {
			checkBoxInstalled.setChecked(false);
			checkBoxInstalled.setTextColor(Color.RED);
			checkBoxMounted.setChecked(false);
			checkBoxMounted.setTextColor(Color.RED);
			buttonLaunchShell.setEnabled(false);
			buttonInstall.setEnabled(true);
			buttonUninstall.setEnabled(false);
			buttonMount.setEnabled(false);
			buttonUnmount.setEnabled(false);
		}
	}

	private class installLinux extends AsyncTask<Void, Void, String> {

		private final Context context;
		private final String linuxPath;

		private ProgressDialog progressDialog;

		public installLinux(final Context context, final String linuxPath) {
			this.context = context;
			this.linuxPath = linuxPath;
		}

		@Override
		protected final void onPreExecute() {
			super.onPreExecute();
			progressDialog = ArchiDroidUtilities.getProgressDialog(context, "Installing ArchiDroid Linux in " + linuxPath + "...");
			progressDialog.show();
		}

		@Override
		protected final String doInBackground(final Void... args) {
			final String result = ArchiDroidUtilities.rootExecuteWithOutput(commandInstall + " " + linuxPath);
			ArchiDroidUtilities.refreshArchiDroidLinux();
			return result;
		}

		@Override
		protected final void onPostExecute(final String result) {
			super.onPostExecute(result);
			refreshLinuxStatus();
			progressDialog.dismiss();

			final DialogOK dialog = new DialogOK();
			if (result.equals("")) {
				dialog.setArgs("ArchiDroid Linux", "Success!");
			} else {
				dialog.setArgs("ArchiDroid Linux", "ARCHIDROID_LINUX: " + result);
			}
			if (isActive) {
				dialog.show(getFragmentManager(), null);
			} else {
				dialogList.add(dialog);
			}
		}
	}

	private class uninstallLinux extends AsyncTask<Void, Void, String> {

		private final Context context;
		private final String linuxPath;

		private ProgressDialog progressDialog;

		public uninstallLinux(final Context context, final String linuxPath) {
			this.context = context;
			this.linuxPath = linuxPath;
		}

		@Override
		protected final void onPreExecute() {
			super.onPreExecute();
			progressDialog = ArchiDroidUtilities.getProgressDialog(context, "Uninstalling ArchiDroid Linux from " + linuxPath + "...");
			progressDialog.show();
		}

		@Override
		protected final String doInBackground(final Void... args) {
			final String result = ArchiDroidUtilities.rootExecuteWithOutput(commandUninstall + " " + linuxPath);
			ArchiDroidUtilities.refreshArchiDroidLinux();
			return result;
		}

		@Override
		protected final void onPostExecute(final String result) {
			super.onPostExecute(result);
			refreshLinuxStatus();
			progressDialog.dismiss();

			final DialogOK dialog = new DialogOK();
			if (result.equals("")) {
				dialog.setArgs("ArchiDroid Linux", "Success!");
			} else {
				dialog.setArgs("ArchiDroid Linux", "ARCHIDROID_LINUX: " + result);
			}
			if (isActive) {
				dialog.show(getFragmentManager(), null);
			} else {
				dialogList.add(dialog);
			}
		}
	}

	private class mountLinux extends AsyncTask<Void, Void, String> {

		private final Context context;
		private final String linuxPath;

		private ProgressDialog progressDialog;

		public mountLinux(final Context context, final String linuxPath) {
			this.context = context;
			this.linuxPath = linuxPath;
		}

		@Override
		protected final void onPreExecute() {
			super.onPreExecute();
			progressDialog = ArchiDroidUtilities.getProgressDialog(context, "Mounting ArchiDroid Linux in " + linuxPath + "...");
			progressDialog.show();
		}

		@Override
		protected final String doInBackground(final Void... args) {
			final String result = ArchiDroidUtilities.rootExecuteWithOutput(commandMount + " " + linuxPath);
			ArchiDroidUtilities.refreshArchiDroidLinux();
			return result;
		}

		@Override
		protected final void onPostExecute(final String result) {
			super.onPostExecute(result);
			refreshLinuxStatus();
			progressDialog.dismiss();

			final DialogOK dialog = new DialogOK();
			if (result.equals("")) {
				dialog.setArgs("ArchiDroid Linux", "Success!");
			} else {
				dialog.setArgs("ArchiDroid Linux", "ARCHIDROID_LINUX: " + result);
			}
			if (isActive) {
				dialog.show(getFragmentManager(), null);
			} else {
				dialogList.add(dialog);
			}
		}
	}

	private class unmountLinux extends AsyncTask<Void, Void, String> {

		private final Context context;
		private final String linuxPath;

		private ProgressDialog progressDialog;

		public unmountLinux(final Context context, final String linuxPath) {
			this.context = context;
			this.linuxPath = linuxPath;
		}

		@Override
		protected final void onPreExecute() {
			super.onPreExecute();
			progressDialog = ArchiDroidUtilities.getProgressDialog(context, "Unmounting ArchiDroid Linux from " + linuxPath + "...");
			progressDialog.show();
		}

		@Override
		protected final String doInBackground(final Void... args) {
			final String result = ArchiDroidUtilities.rootExecuteWithOutput(commandUnmount + " " + linuxPath);
			ArchiDroidUtilities.refreshArchiDroidLinux();
			return result;
		}

		@Override
		protected final void onPostExecute(final String result) {
			super.onPostExecute(result);
			refreshLinuxStatus();
			progressDialog.dismiss();

			final DialogOK dialog = new DialogOK();
			if (result.equals("")) {
				dialog.setArgs("ArchiDroid Linux", "Success!");
			} else {
				dialog.setArgs("ArchiDroid Linux", "ARCHIDROID_LINUX: " + result);
			}
			if (isActive) {
				dialog.show(getFragmentManager(), null);
			} else {
				dialogList.add(dialog);
			}
		}
	}

	private class runShell extends AsyncTask<Void, Void, Void> {

		private final Context context;
		private final String linuxPath;

		private ProgressDialog progressDialog;

		public runShell(final Context context, final String linuxPath) {
			this.context = context;
			this.linuxPath = linuxPath;
		}

		@Override
		protected final void onPreExecute() {
			super.onPreExecute();
			progressDialog = ArchiDroidUtilities.getProgressDialog(context, "Executing shell in " + linuxPath + "...");
			progressDialog.show();
		}

		@Override
		protected final Void doInBackground(final Void... args) {
			final Intent i = new Intent("jackpal.androidterm.RUN_SCRIPT");
			i.addCategory(Intent.CATEGORY_DEFAULT);
			i.putExtra("jackpal.androidterm.iInitialCommand", "su -c " + "\"" + commandLaunchShell + " " + linuxPath + "\"");

			//TODO: Remove workaround for https://github.com/jackpal/Android-Terminal-Emulator/issues/353
			ArchiDroidUtilities.rootSysRW();
			ArchiDroidUtilities.rootRenameFile("/system/xbin/resize", "/system/xbin/resize.old");

			startActivity(i);

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			ArchiDroidUtilities.rootRenameFile("/system/xbin/resize.old", "/system/xbin/resize");
			ArchiDroidUtilities.rootSysRO();

			return null;
		}

		@Override
		protected final void onPostExecute(final Void result) {
			super.onPostExecute(result);
			progressDialog.dismiss();
		}
	}
}