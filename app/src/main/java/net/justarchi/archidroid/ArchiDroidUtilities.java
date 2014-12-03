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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Debug;
import eu.chainfire.libsuperuser.Shell;

public final class ArchiDroidUtilities {

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	private static final String internalSdCard = "/storage/sdcard0";
	private static final String ArchiDroidInternalDir = internalSdCard + "/ArchiDroid";
	private static final String externalSdCard = "/storage/sdcard1";
	private static final String ArchiDroidExternalDir = externalSdCard + "/ArchiDroid";
	private static final boolean archiLogEnabled = true;
	private static final String githubRepo = "ArchiDroid/ArchiDroid";
	private static final String githubBraches = "https://api.github.com/repos/" + githubRepo + "/branches";
	private static final String githubWiki = "https://github.com/" + githubRepo + "/wiki/Application";
	private static final String linkDonation = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=WYXLLCQ9EA28L&item_name=ArchiDroid&item_number=ArchiDroidApplication";
	private static final String ArchiDroidRootDir = "/data/media/0/ArchiDroid";
	private static String ArchiDroidLinux = ArchiDroidRootDir + "/debian";
	private static boolean isActive = false;
	private static boolean isArchiDroid = false;
	private static boolean isRooted = false;
	// These may change in future
	private static String ArchiDroidDir = Environment.getExternalStorageDirectory() + "/ArchiDroid";
	private static String ArchiDroidTempDir = ArchiDroidDir + "/tmp";
	private static String updateTargetTempFile = ArchiDroidTempDir + "/ArchiDroid-GitHub.zip";
	private static String updateTargetTempRepackedFile = ArchiDroidTempDir + "/ArchiDroid.zip";
	private static String updateTargetFinalFile = ArchiDroidDir + "/ArchiDroid.zip";

	private static boolean ArchiDroidLinuxInstalled = false;
	private static boolean ArchiDroidLinuxMounted = false;

	private static String roArchiDroid = null;
	private static String roArchiDroidDevice = null;
	private static String roArchiDroidRom = null;
	private static String roArchiDroidRomShort = null;
	private static String roArchiDroidVersion = null;
	private static String roArchiDroidVersionType = null;
	private static String roBuildDate = null;
	private static String roBuildDateUTC = null;

	private static ArrayList<BackendSwitch> backendSwitches;
	private static ArrayList<BackendSpinner> backendSpinners;
	private static ArrayList<BackendProcessCheckBox> backendProcessCheckBoxes;

	private static void archiTests() {
	}

	protected static final void initUtilities(final Context context) {
		archiTests();

		Debug.setDebug(false);

		roArchiDroid = getProperty("ro.archidroid");
		isArchiDroid = roArchiDroid != null && roArchiDroid.equals("1");
		roArchiDroidDevice = getProperty("ro.archidroid.device");
		roArchiDroidRom = getProperty("ro.archidroid.rom");
		roArchiDroidRomShort = getProperty("ro.archidroid.rom.short");
		roArchiDroidVersion = getProperty("ro.archidroid.version");
		roArchiDroidVersionType = getProperty("ro.archidroid.version.type");
		roBuildDate = getProperty("ro.build.date");
		roBuildDateUTC = getProperty("ro.build.date.utc");

		refreshRoot();
		refreshConnection(context);

		if (isRooted) {
			refreshArchiDroidLinux();
		}
	}

	protected static final boolean isSystemFullySupported() {
		return isArchiDroid && isRooted;
	}

	protected static final String getLinkDonation() {
		return linkDonation;
	}

	protected static final String getArchiDroidInternalDir() {
		return ArchiDroidInternalDir;
	}

	protected static final String getArchiDroidLinux() {
		return ArchiDroidLinux;
	}

	protected static final void refreshArchiDroidLinux() {
		if (rootIsLinuxInstalled()) {
			setArchiDroidLinuxInstalled(true);
			setArchiDroidLinuxMounted(rootIsLinuxMounted());
		} else {
			setArchiDroidLinuxInstalled(false);
			setArchiDroidLinuxMounted(false);
		}
	}

	protected static final boolean getArchiDroidLinuxInstalled() {
		return ArchiDroidLinuxInstalled;
	}

	protected static final void setArchiDroidLinuxInstalled(final boolean state) {
		ArchiDroidLinuxInstalled = state;
	}

	protected static final boolean getArchiDroidLinuxMounted() {
		return ArchiDroidLinuxMounted;
	}

	protected static final void setArchiDroidLinuxMounted(boolean state) {
		ArchiDroidLinuxMounted = state;
	}

	protected static final boolean rootFileExists(final String path) {
		return rootExecuteWithOutputOneLine("[[ -e " + path + " ]]; echo $?").equals("0");
	}

	protected static final boolean rootIsLinuxInstalled() {
		return rootFileExists(ArchiDroidLinux + "/bin/bash");
	}

	protected static final boolean rootIsLinuxMounted() {
		return rootFileExists(ArchiDroidLinux + "/sys/kernel");
	}

	protected static final int getDefaultBufferSize() {
		return DEFAULT_BUFFER_SIZE;
	}

	protected static final void setBackendSwitches(final ArrayList<BackendSwitch> newBackendSwitches) {
		backendSwitches = newBackendSwitches;
	}

	protected static final void setBackendSpinners(final ArrayList<BackendSpinner> newBackendSpinners) {
		backendSpinners = newBackendSpinners;
	}

	protected static final void setBackendProcessCheckBoxes(final ArrayList<BackendProcessCheckBox> newBackendProcessCheckBoxes) {
		backendProcessCheckBoxes = newBackendProcessCheckBoxes;
	}

	protected static final void refreshBackendProcessCheckBoxes() {
		for (final BackendProcessCheckBox pcb : backendProcessCheckBoxes) {
			pcb.refresh();
		}
	}

	protected static final String getArchiDroidTempDir() {
		return ArchiDroidTempDir;
	}

	protected static final String getUpdateTargetTempFile() {
		return updateTargetTempFile;
	}

	protected static final String getUpdateTargetTempRepackedFile() {
		return updateTargetTempRepackedFile;
	}

	protected static final String getUpdateTargetFinalFile() {
		return updateTargetFinalFile;
	}

	protected static final void refreshRoot() {
		isRooted = Shell.SU.available();
	}

	protected static final boolean isRooted() {
		return isRooted;
	}

	protected static final String getSymlinkTargetName(final File symlink) {
		String target;
		try {
			target = symlink.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		String[] result = target.split("/");
		target = result[result.length - 1];
		return target;
	}

	protected static final List<String> rootExecuteWait(final String[] commands) {
		return Shell.SU.run(commands);
	}

	protected static final List<String> rootExecuteWait(final String command) {
		return rootExecuteWait(new String[]{command});
	}

	protected static final String rootExecuteWithOutput(final String[] commands) {
		final StringBuilder sb = new StringBuilder();
		for (String s : Shell.SU.run(commands)) {
			sb.append(s);
			sb.append("\t");
		}
		return sb.toString();
	}

	protected static final String rootExecuteWithOutput(final String command) {
		return rootExecuteWithOutput(new String[]{command});
	}

	protected static final String rootExecuteWithOutputOneLine(final String[] commands) {
		return Shell.SU.run(commands).get(0);
	}

	protected static final String rootExecuteWithOutputOneLine(final String command) {
		return rootExecuteWithOutputOneLine(new String[]{command});
	}

	protected static final void rootDeleteFile(final String file) {
		rootExecuteWait("rm -f " + file);
	}

	protected static final void rootRenameFile(final String from, final String to) {
		rootExecuteWait("mv " + from + " " + to);
	}

	protected static final void rootDeleteFile(final File file) {
		rootDeleteFile(file.getPath());
	}

	protected static final void rootSymlink(final String target, final String source) {
		rootExecuteWait("ln -s " + target + " " + source);
	}

	protected static final void rootSymlink(final String target, final File source) {
		rootSymlink(target, source.getPath());
	}

	protected static final void rootWriteToBackendFile(final File file, final String string) {
		rootExecuteWait("echo " + string + " > " + file.getAbsolutePath());
	}

	protected static final void rootSysRO() {
		rootExecuteWait("sysro || mount -o remount,ro /system");
	}

	protected static final void rootSysRW() {
		rootExecuteWait("sysrw || mount -o remount,rw /system");
	}

	protected static final void reloadINIT(final String service) {
		rootExecuteWait("ARCHIDROID_INIT RELOAD " + service.toUpperCase());
	}

	protected static final String readFileOneLine(final File file) {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			return bufferedReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected static final String readFile(final File file) {
		final StringBuilder sb = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	protected static final String readFile(final String path) {
		return readFile(new File(path));
	}

	protected static final String getArchiDroidDevice() {
		return roArchiDroidDevice;
	}

	protected static final String getBuildDate() {
		return roBuildDate;
	}

	protected static final String getBuildDateUTC() {
		return roBuildDateUTC;
	}

	protected static final String getArchiDroidRom() {
		return roArchiDroidRom;
	}

	protected static final String getArchiDroidRomShort() {
		return roArchiDroidRomShort;
	}

	protected static final String getArchiDroidVersion() {
		return roArchiDroidVersion;
	}

	protected static final String getArchiDroidVersionType() {
		return roArchiDroidVersionType;
	}

	protected static final void error(final String string) {
		Log.e("ArchiError: ", string);
	}

	protected static final void log(final String string) {
		if (archiLogEnabled) {
			Log.v("ArchiLog: ", string);
		}
	}

	protected static final boolean versionBiggerThan(final String bigger, final String smaller) {
		for (int i = 0; i < bigger.length(); i++) {
			if (i == smaller.length()) {
				// Bigger is longer e.g. 3.0[a] vs. 3.0[ ]
				return true;
			} else if (Character.getNumericValue(bigger.charAt(i)) > Character.getNumericValue(smaller.charAt(i))) {
				//  Bigger char is in fact bigger than smaller, e.g. 2.[2] vs. 2.[1]
				return true;
			} else if (Character.getNumericValue(bigger.charAt(i)) < Character.getNumericValue(smaller.charAt(i))) {
				// Smaller is in fact bigger than bigger, e.g. 2.[1] vs. 2.[2]
				return false;
			}
		}
		// They're equal, e.g. 3.[0] vs. 3.[0] or smaller is longer, e.g. 3.[0] vs 3.[0]a
		return false;
	}

	protected static final boolean processIsRunning(final String process) {
		final Process p;
		try {
			p = Runtime.getRuntime().exec("pidof " + process);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		BufferedReader input = null;
		try {
			input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			return (input.readLine()) != null;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected static final boolean isArchiDroid() {
		return isArchiDroid;
	}

	protected static final boolean isActive() {
		return isActive;
	}

	protected static final void onResume() {
		isActive = true;
	}

	protected static final void onPause() {
		isActive = false;
	}

	protected static final void connected(final Context context) {
		if (isActive) {
			ArchiDroidUtilities.showShortToast(context, "Connected!");
		}
	}

	protected static final void disconnected(final Context context) {
		if (isActive) {
			ArchiDroidUtilities.showShortToast(context, "Lost connection!");
		}
	}

	protected static final String getGithubBranches() {
		return githubBraches;
	}

	protected static final String getGithubRepo() {
		return githubRepo;
	}

	protected static final String getGithubWiki() {
		return githubWiki;
	}

	protected static final boolean isConnected(final Context context) {
		return NetworkReceiver.isConnectedNow(context);
	}

	protected static final void refreshConnection(final Context context) {
		NetworkReceiver.refreshConnection(context);
	}

	protected static final void showShortToast(final Context context, final String string) {
		Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
	}

	protected static final void showLongToast(final Context context, final String string) {
		Toast.makeText(context, string, Toast.LENGTH_LONG).show();
	}

	protected static final ProgressDialog getProgressDialog(final Context context, final String string) {
		final ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.setMessage(string);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setProgress(0);
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
		return progressDialog;
	}

	protected static final ProgressDialog getProgressDialogWithProgress(final Context context, final String string) {
		final ProgressDialog progressDialog = getProgressDialog(context, string);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setIndeterminate(false);
		return progressDialog;
	}

	protected static final String getProperty(final String property) {
		final String getprop;
		try {
			Class androidOS = Class.forName("android.os.SystemProperties");
			Method method = androidOS.getDeclaredMethod("get", String.class);
			getprop = (String) method.invoke(null, property);
			return getprop;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected static final String getJSONFromUrl(final String url) {
		final DefaultHttpClient httpClient = new DefaultHttpClient();
		final HttpGet httpGet = new HttpGet(url);

		final HttpResponse httpResponse;
		try {
			httpResponse = httpClient.execute(httpGet);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		final HttpEntity httpEntity = httpResponse.getEntity();

		final StringBuilder sb = new StringBuilder();
		String line;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(httpEntity.getContent(), "UTF-8"), 8);
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	protected static final ArrayList<JSONObject> getJSONObjectsFromJSON(final String json) {
		final ArrayList<JSONObject> jAL = new ArrayList<>();
		final JSONArray jArray;
		try {
			jArray = new JSONArray(json);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		for (int i = 0; i < jArray.length(); i++) {
			try {
				jAL.add(jArray.getJSONObject(i));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return jAL;
	}

	protected static final ArrayList<String> getStringFromJSONObjects(final String string, final ArrayList<JSONObject> jAL) {
		final ArrayList<String> sAL = new ArrayList<>();
		for (JSONObject jObj : jAL) {
			try {
				sAL.add(jObj.getString(string));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return sAL;
	}

	protected static final boolean deleteRecursive(final File fileOrDirectory) {
		if (fileOrDirectory.isDirectory()) {
			for (final File child : fileOrDirectory.listFiles()) {
				deleteRecursive(child);
			}
		}

		return fileOrDirectory.delete();
	}
}