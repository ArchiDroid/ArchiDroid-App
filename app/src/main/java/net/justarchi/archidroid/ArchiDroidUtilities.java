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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import eu.chainfire.libsuperuser.Debug;
import eu.chainfire.libsuperuser.Shell;

public final class ArchiDroidUtilities {

	private static boolean isArchiDroid = false;
	private static boolean isRooted = false;

	private static boolean ArchiDroidLinuxInstalled = false;
	private static boolean ArchiDroidLinuxMounted = false;

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	private static final String githubRepo = "ArchiDroid/ArchiDroid";
	private static final String githubBraches = "https://api.github.com/repos/" + githubRepo + "/branches";
	private static final String githubWiki = "https://github.com/" + githubRepo + "/wiki/Application";

	private static final String linkDonation = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=WYXLLCQ9EA28L&item_name=ArchiDroid&item_number=ArchiDroidApplication";

	private static final String internalSdCard = "/storage/sdcard0";
	private static final String ArchiDroidInternalDir = internalSdCard + "/ArchiDroid";

	// Data dirs can only be accessed through root
	private static final String ArchiDroidDataDir = "/data/media/0/ArchiDroid";
	private static final String ArchiDroidLinux = ArchiDroidDataDir + "/debian";

	private static final String ArchiDroidSystemDir = "/system/archidroid";
	private static final String ArchiDroidTmpfsDir = ArchiDroidSystemDir + "/tmpfs";

	// These may change in future
	private static final String ArchiDroidDir = Environment.getExternalStorageDirectory() + "/ArchiDroid";
	private static final String ArchiDroidEventsDir = ArchiDroidDir + "/System/Events";

	private static final String ArchiDroidTempDir = ArchiDroidDir + "/tmp";
	private static final String updateTargetTempFile = ArchiDroidTempDir + "/ArchiDroid-GitHub.zip";
	private static final String updateTargetTempRepackedFile = ArchiDroidTempDir + "/ArchiDroid.zip";
	private static final String updateTargetFinalFile = ArchiDroidDir + "/ArchiDroid.zip";

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

	//private static void archiTests() {
	//}

	protected static final void initUtilities(final Context context) {
		//archiTests();

		Debug.setDebug(false);

		readProperties();
		refreshRoot();
		refreshConnection(context);

		if (isRooted) {
			refreshArchiDroidLinux();
		}
	}

	protected static final void readProperties() {
		if (roArchiDroid == null) {
			roArchiDroid = getProperty("ro.archidroid");
			isArchiDroid = roArchiDroid != null && roArchiDroid.equals("1");
			roArchiDroidDevice = getProperty("ro.archidroid.device");
			roArchiDroidRom = getProperty("ro.archidroid.rom");
			roArchiDroidRomShort = getProperty("ro.archidroid.rom.short");
			roArchiDroidVersion = getProperty("ro.archidroid.version");
			roArchiDroidVersionType = getProperty("ro.archidroid.version.type");
			roBuildDate = getProperty("ro.build.date");
			roBuildDateUTC = getProperty("ro.build.date.utc");
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

	protected static final String getArchiDroidTmpfsDir() {
		return ArchiDroidTmpfsDir;
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

	protected static final String getCurrentBranch() {
		if (!roArchiDroidDevice.equals("") && !roArchiDroidRomShort.equals("") && !roArchiDroidVersionType.equals("")) {
			return roArchiDroidDevice.toLowerCase() + "-" + roArchiDroidRomShort.toLowerCase() + "-" + roArchiDroidVersionType.toLowerCase();
		} else {
			return "";
		}
	}

	protected static final boolean isNewArchiDroidAvailable() {
		readProperties(); // Because this function may be called before first run
		final String currentBranch = getCurrentBranch();
		if (currentBranch.equals("")) {
			return false;
		}

		ReadableByteChannel rbc = null;
		try {
			rbc = Channels.newChannel(new URL("https://github.com/" + ArchiDroidUtilities.getGithubRepo() + "/raw/" + currentBranch + "/system/build.prop").openStream());
			final Scanner scanner = new Scanner(rbc);

			String roArchiDroidVersionRemote = "";
			String roBuildDateUTCRemote = "";

			int missingFields = 2;
			String currentLine;
			while (missingFields > 0 && scanner.hasNextLine()) {
				currentLine = scanner.nextLine();
				//ArchiDroidUtilities.log(currentLine);

				if (currentLine.startsWith("ro.archidroid.version=")) {
					roArchiDroidVersionRemote = currentLine.substring(currentLine.indexOf('=') + 1);
					missingFields--;
				} else if (currentLine.startsWith("ro.build.date.utc=")) {
					roBuildDateUTCRemote = currentLine.substring(currentLine.indexOf('=') + 1);
					missingFields--;
				}
			}
			if (compareVersions(roArchiDroidVersionRemote, roArchiDroidVersion) == 1 || compareVersions(roBuildDateUTCRemote, roBuildDateUTC) == 1) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (rbc != null) {
				try {
					rbc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
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
		//log("rootExecuteWait: " + command);
		return rootExecuteWait(new String[]{command});
	}

	protected static final String rootExecuteWithOutput(final String[] commands) {
		final StringBuilder sb = new StringBuilder();
		for (final String s : Shell.SU.run(commands)) {
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

	protected static final void showNotification(final Context context, final String title, final String message) {
		final int uniqueNumber = 1337; // TODO
		NotificationManager mNotificationManager = (NotificationManager)
				context.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				new Intent(context, Main.class), 0);

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(context)
						.setSmallIcon(R.mipmap.ic_launcher)
						.setContentTitle(title)
						.setStyle(new NotificationCompat.BigTextStyle()
								.bigText(message))
						.setContentText(message);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(uniqueNumber, mBuilder.build());
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

	protected static final boolean writeFile(final File file, final String content) {
		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(file));
			bufferedWriter.write(content);
			bufferedWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (bufferedWriter != null) {
				try {
					bufferedWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		return true;
	}

	protected static final File writeTempFile(final File directory, final String extension, final String content) {
		if (directory.isDirectory()) {
			File tempFile = null;
			try {
				tempFile = File.createTempFile("ArchiDroid", extension, directory);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			writeFile(tempFile, content);
			return tempFile;
		} else {
			error("writeTempFile(): Directory " + directory + " doesn't exist!");
			return null;
		}
	}

	private static File lastEventFile = null;
	protected static final void sendEvent(final String event) {
		if (new File(ArchiDroidEventsDir).exists()) {
			if (lastEventFile != null && lastEventFile.exists()) {
				error("sendEvent(): Tried to send event, but it looks like nothing is listening for them!");
				return;
			}
			final File eventFile = writeTempFile(new File(ArchiDroidEventsDir), ".EVENT", event);
			if (eventFile != null) {
				lastEventFile = eventFile;
			} else {
				error("sendEvent(): writeTempFile() returned null file!");
			}
		} else {
			error("sendEvent(): ArchiDroidEventsDir " + ArchiDroidEventsDir + " doesn't exist!");
		}
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

	/**
	 * Compares two version-like strings, e.g. 3.0a 3.1.1b
	 *
	 * @param bigger  The version which should be bigger
	 * @param smaller The version which should be smaller
	 * @return 1 if bigger > smaller, -1 if smaller > bigger, 0 otherwise (equal)
	 */
	protected static final int compareVersions(final String bigger, final String smaller) {
		int biggerLength = bigger.length();
		int smallerLength = smaller.length();
		for (int i = 0; i < biggerLength; i++) {
			if (i >= smallerLength) { // Bigger is longer e.g. 3.0[a] vs. 3.0[ ]
				//log(bigger + " wins with " + smaller);
				return 1;
			} else {
				int biggerChar = Character.getNumericValue(bigger.charAt(i));
				int smallerChar = Character.getNumericValue(smaller.charAt(i));
				if (biggerChar > smallerChar) { //  Bigger char is in fact bigger than smaller, e.g. 2.[2].0 vs. 2.[1].1
					//log(bigger + " wins with " + smaller);
					return 1;
				} else if (smallerChar > biggerChar) { // Smaller is in fact bigger than bigger, e.g. 2.[1].1 vs. 2.[2].0
					//log(smaller + " wins with " + bigger);
					return -1;
				}
			}
		}
		if (biggerLength == smallerLength) { // They're absolutely equal
			//log(bigger + " draws with " + smaller);
			return 0;
		} else if (smallerLength > biggerLength) { // Smaller is longer e.g. 3.0[ ] vs. 3.0[a]
			//log(smaller + " wins with " + bigger);
			return -1;
		} else { // Should never happen
			error("compareVersions(): Unhandled scenario: " + bigger + " " + smaller);
			return 0;
		}
	}

	/**
	 * Compares two versionType-like strings, e.g. STABLE and EXPERIMENTAL
	 *
	 * @param remote  The version which is available on the remote server
	 * @param current The version which we have right now
	 * @return 1 if remote > current (stable vs. exp) -1 if current > remote (exp vs. stable), 0 otherwise (equal)
	 */
	protected static final int compareVersionTypes(final String remote, final String current) {
		if (current.equalsIgnoreCase("STABLE")) {
			if (remote.equalsIgnoreCase("EXPERIMENTAL")) {
				return -1;
			} else if (remote.equalsIgnoreCase("STABLE")) {
				return 0;
			} else { // Should never happen, unknown version type?
				error("compareVersionTypes(): Got unknown remote version type: " + remote);
				return -1;
			}
		} else if (current.equalsIgnoreCase("EXPERIMENTAL")) {
			if (remote.equalsIgnoreCase("STABLE")) {
				return 1;
			} else if (remote.equalsIgnoreCase("EXPERIMENTAL")) {
				return 0;
			} else { // Should never happen, unknown version type?
				error("compareVersionTypes(): Got unknown remote version type: " + remote);
				return -1;
			}
		} else { // Should never happen, unknown version type?
			error("compareVersionTypes(): Got unknown current version type: " + current);
			return -1;
		}
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
		try {
			final Class androidOS = Class.forName("android.os.SystemProperties");
			final Method method = androidOS.getDeclaredMethod("get", String.class);
			return (String) method.invoke(null, property);
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