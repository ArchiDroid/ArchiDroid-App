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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public final class ArchiDroidFragmentUpdate extends Fragment {

	private boolean isActive = true;

	private final ArrayList<DialogFragment> dialogList=new ArrayList<>();

	private PowerManager.WakeLock mWakeLock;

	private Button buttonUpdate;
	private Button buttonDownload;

	private Spinner spinnerBranches;
	private Spinner spinnerDownloadModes;

	private TextView textRemoteVersion;
	private TextView textCurrentVersion;

	private TextView textRemoteBuildDate;
	private TextView textCurrentBuildDate;

	private TextView textRemoteVersionType;
	private TextView textCurrentVersionType;

	private TextView textRemoteRom;
	private TextView textCurrentRom;

	private TextView textRemoteDevice;
	private TextView textCurrentDevice;

	private String roArchiDroidDeviceRemote = null;
	private String roArchiDroidRomRemote = null;
	private String roArchiDroidRomShortRemote = null;
	private String roArchiDroidVersionRemote = null;
	private String roArchiDroidVersionTypeRemote = null;
	private String roBuildDateRemote = null;
    private String roBuildDateUTCRemote = null;

	public ArchiDroidFragmentUpdate() {
	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_update, container, false);
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
	public final void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		buttonUpdate = (Button) getView().findViewById(R.id.buttonUpdate);
		buttonUpdate.setOnClickListener(new CustomOnClickListener());

		buttonDownload = (Button) getView().findViewById(R.id.buttonDownload);
		buttonDownload.setOnClickListener(new CustomOnClickListener());

		spinnerBranches = (Spinner) getView().findViewById(R.id.spinnerBranches);
		final ArrayList<String> spinnerBranchesArray = new ArrayList<>();
		spinnerBranchesArray.add(getString(R.string.stringFetchFirst));
		final ArrayAdapter<String> spinnerBranchesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, spinnerBranchesArray);
		spinnerBranchesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerBranches.setAdapter(spinnerBranchesAdapter);
		spinnerBranches.setEnabled(false);

		spinnerDownloadModes = (Spinner) getView().findViewById(R.id.spinnerDownloadModes);
		final ArrayList<String> spinnerDownloadModesArray = new ArrayList<>();
		spinnerDownloadModesArray.add(getString(R.string.stringModeDirect));
		spinnerDownloadModesArray.add(getString(R.string.stringModeGit));
		final ArrayAdapter<String> spinnerDownloadModesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, spinnerDownloadModesArray);
		spinnerDownloadModesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerDownloadModes.setAdapter(spinnerDownloadModesAdapter);
		spinnerDownloadModes.setEnabled(false);


		textRemoteVersion = (TextView) getView().findViewById(R.id.textRemoteVersion);
		textCurrentVersion = (TextView) getView().findViewById(R.id.textCurrentVersion);
		textCurrentVersion.setText(getString(R.string.textVersion) + " " + ArchiDroidUtilities.getArchiDroidVersion());

		textRemoteBuildDate = (TextView) getView().findViewById(R.id.textRemoteBuildDate);
		textCurrentBuildDate = (TextView) getView().findViewById(R.id.textCurrentBuildDate);
		textCurrentBuildDate.setText(getString(R.string.textBuildDate) + " " + ArchiDroidUtilities.getBuildDate());

		textRemoteVersionType = (TextView) getView().findViewById(R.id.textRemoteVariant);
		textCurrentVersionType = (TextView) getView().findViewById(R.id.textCurrentVariant);
		textCurrentVersionType.setText(getString(R.string.textVersionType) + " " + ArchiDroidUtilities.getArchiDroidVersionType());

		textRemoteRom = (TextView) getView().findViewById(R.id.textRemoteRom);
		textCurrentRom = (TextView) getView().findViewById(R.id.textCurrentRom);
		textCurrentRom.setText(getString(R.string.textRom) + " " + ArchiDroidUtilities.getArchiDroidRom());

		textRemoteDevice = (TextView) getView().findViewById(R.id.textRemoteDevice);
		textCurrentDevice = (TextView) getView().findViewById(R.id.textCurrentDevice);
		textCurrentDevice.setText(getString(R.string.textDevice) + " " + ArchiDroidUtilities.getArchiDroidDevice());
	}

	public final class CustomOnClickListener implements View.OnClickListener {

		@Override
		public final void onClick(final View v) {
			switch (v.getId()) {
				case R.id.buttonUpdate:
					if (!ArchiDroidUtilities.isConnected(v.getContext())) {
						ArchiDroidUtilities.showShortToast(v.getContext(), "No connection available!");
						break;
					}
					new loadBranches(v.getContext()).execute();
					break;
				case R.id.buttonDownload:
					if (!ArchiDroidUtilities.isConnected(v.getContext())) {
						ArchiDroidUtilities.showShortToast(v.getContext(), "No connection available!");
						break;
					}

					// Make sure there are no zombie wakelocks
					if (mWakeLock != null && mWakeLock.isHeld()) {
						mWakeLock.release();
					}

					// Keep CPU online while we download and parse zip
					final PowerManager pm = (PowerManager) v.getContext().getSystemService(Context.POWER_SERVICE);
					mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StayAwake");
					mWakeLock.acquire();

					if (spinnerDownloadModes.getSelectedItem().toString().equals(getString(R.string.stringModeDirect))) {
						new downloadFileDirect(v.getContext()).execute("https://github.com/" + ArchiDroidUtilities.getGithubRepo() + "/archive/" + spinnerBranches.getSelectedItem().toString() + ".zip");
					} else if (spinnerDownloadModes.getSelectedItem().toString().equals(getString(R.string.stringModeGit))) {
						if (ArchiDroidUtilities.getArchiDroidLinuxMounted()) {
							new downloadFileGit(v.getContext(), spinnerBranches.getSelectedItem().toString()).execute();
						} else {
							mWakeLock.release();
							final DialogOK dialog = new DialogOK();
							dialog.setArgs("WARNING", "Git mode couldn't be used because ArchiDroid's Linux has not been mounted yet. Check out the help to get more info.");
							if (isActive) {
								dialog.show(getFragmentManager(), null);
							} else {
								dialogList.add(dialog);
							}
						}
					}
					break;
			}
		}
	}

	public final class CustomOnItemSelectedListener implements Spinner.OnItemSelectedListener {
		@Override
		public final void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
			switch (parent.getId()) {
				case R.id.spinnerBranches:
					if (!ArchiDroidUtilities.isConnected(view.getContext())) {
						ArchiDroidUtilities.showShortToast(view.getContext(), "No connection available!");
						break;
					}
					new parseRemoteBuildProp(view.getContext()).execute("https://github.com/" + ArchiDroidUtilities.getGithubRepo() + "/raw/" + spinnerBranches.getSelectedItem().toString() + "/system/build.prop");
					break;
			}
		}

		@Override
		public final void onNothingSelected(final AdapterView<?> parent) {

		}
	}

	private final class loadBranches extends AsyncTask<String, Void, ArrayList<String>> {

		private final Context context;

		private ProgressDialog progressDialog;

		public loadBranches(final Context context) {
			this.context = context;
		}

		@Override
		protected final void onPreExecute() {
			super.onPreExecute();
			progressDialog = ArchiDroidUtilities.getProgressDialog(context, "Fetching ArchiDroid branches...");
			progressDialog.show();
		}

		@Override
		protected final ArrayList<String> doInBackground(final String... urls) {

			final ArrayList<String> branches = ArchiDroidUtilities.getStringFromJSONObjects("name", ArchiDroidUtilities.getJSONObjectsFromJSON(ArchiDroidUtilities.getJSONFromUrl(ArchiDroidUtilities.getGithubBranches())));
			final ArrayList<String> myAvailableBranches = new ArrayList<>();
			if (!ArchiDroidUtilities.getArchiDroidDevice().equals("")) { // If we know devices's codename, filter branches only designed for this device
				for (final String branch : branches) {
					final String[] tokens = branch.split("-");
					if (tokens.length >= 3 && tokens[0].equals(ArchiDroidUtilities.getArchiDroidDevice())) {
						myAvailableBranches.add(branch);
					}
				}
			} else {
				for (final String branch : branches) {
					final String[] tokens = branch.split("-");
					if (tokens.length >= 3) {
						myAvailableBranches.add(branch);
					}
				}
			}
			return myAvailableBranches;
		}

		@Override
		protected final void onPostExecute(final ArrayList<String> result) {
			super.onPostExecute(result);
			spinnerBranches.setOnItemSelectedListener(new CustomOnItemSelectedListener());
			final ArrayAdapter<String> branchesAdapter = new ArrayAdapter<>(ArchiDroidFragmentUpdate.this.getActivity(), android.R.layout.simple_spinner_item, result);
			branchesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinnerBranches.setAdapter(branchesAdapter);
			spinnerBranches.setEnabled(true);

			if (ArchiDroidUtilities.isSystemFullySupported()) {
				spinnerDownloadModes.setEnabled(true);
			}

			progressDialog.dismiss();
			if (!ArchiDroidUtilities.getArchiDroidDevice().equals("") && !ArchiDroidUtilities.getArchiDroidRomShort().equals("") && !ArchiDroidUtilities.getArchiDroidVersionType().equals("")) {
				spinnerBranches.setSelection(branchesAdapter.getPosition(ArchiDroidUtilities.getArchiDroidDevice() + "-" + ArchiDroidUtilities.getArchiDroidRomShort() + "-" + ArchiDroidUtilities.getArchiDroidVersionType()));
			}
		}
	}

	private class parseRemoteBuildProp extends AsyncTask<String, Void, Boolean> {

		private final Context context;

		private ProgressDialog progressDialog;

		public parseRemoteBuildProp(Context context) {
			this.context = context;
		}

		@Override
		protected final void onPreExecute() {
			super.onPreExecute();
			progressDialog = ArchiDroidUtilities.getProgressDialog(context, "Parsing remote build.prop...");
			progressDialog.show();
		}

		@Override
		protected final Boolean doInBackground(final String... arg) {

			ReadableByteChannel rbc = null;
			try {
				new URL(arg[0]).openStream();
				rbc = Channels.newChannel(new URL(arg[0]).openStream());
				final Scanner scanner = new Scanner(rbc);

				int allFields = 7;
				String currentLine;
				while (scanner.hasNextLine()) {
					currentLine = scanner.nextLine();
					//ArchiDroidUtilities.log(currentLine);

					if (currentLine.startsWith("ro.archidroid.device=")) {
						roArchiDroidDeviceRemote = currentLine.substring(currentLine.indexOf('=') + 1);
						allFields--;
						if (allFields <= 0) {
							break;
						}
					} else if (currentLine.startsWith("ro.archidroid.rom=")) {
						roArchiDroidRomRemote = currentLine.substring(currentLine.indexOf('=') + 1);
						allFields--;
						if (allFields <= 0) {
							break;
						}
					} else if (currentLine.startsWith("ro.archidroid.rom.short=")) {
						roArchiDroidRomShortRemote = currentLine.substring(currentLine.indexOf('=') + 1);
						allFields--;
						if (allFields <= 0) {
							break;
						}
					} else if (currentLine.startsWith("ro.archidroid.version=")) {
						roArchiDroidVersionRemote = currentLine.substring(currentLine.indexOf('=') + 1);
						allFields--;
						if (allFields <= 0) {
							break;
						}
					} else if (currentLine.startsWith("ro.archidroid.version.type=")) {
						roArchiDroidVersionTypeRemote = currentLine.substring(currentLine.indexOf('=') + 1);
						allFields--;
						if (allFields <= 0) {
							break;
						}
					} else if (currentLine.startsWith("ro.build.date=")) {
						roBuildDateRemote = currentLine.substring(currentLine.indexOf('=') + 1);
						allFields--;
						if (allFields <= 0) {
							break;
						}
					} else if (currentLine.startsWith("ro.build.date.utc=")) {
                        roBuildDateUTCRemote = currentLine.substring(currentLine.indexOf('=') + 1);
                        allFields--;
                        if (allFields <= 0) {
                            break;
                        }
                    }
				}
				return true;
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

		@Override
		protected final void onPostExecute(final Boolean result) {
			super.onPostExecute(result);

			textRemoteVersion.setText(getString(R.string.textVersion) + " " + roArchiDroidVersionRemote);
			textRemoteVersion.setTextColor(Color.GREEN);
			if (!ArchiDroidUtilities.getArchiDroidVersion().equals("")) {
				if (ArchiDroidUtilities.versionBiggerThan(roArchiDroidVersionRemote, ArchiDroidUtilities.getArchiDroidVersion())) {
					textCurrentVersion.setTextColor(Color.RED);
				} else {
					textCurrentVersion.setTextColor(Color.GREEN);
				}
			}

			textRemoteBuildDate.setText(getString(R.string.textBuildDate) + " " + roBuildDateRemote);
			textRemoteBuildDate.setTextColor(Color.GREEN);
			if (!ArchiDroidUtilities.getBuildDate().equals("")) {
				if (ArchiDroidUtilities.versionBiggerThan(roBuildDateUTCRemote, ArchiDroidUtilities.getBuildDateUTC())) {
					textCurrentBuildDate.setTextColor(Color.RED);
				} else {
					textCurrentBuildDate.setTextColor(Color.GREEN);
				}
			}

			textRemoteVersionType.setText(getString(R.string.textVersionType) + " " + roArchiDroidVersionTypeRemote);
			textRemoteVersionType.setTextColor(Color.GREEN);
			if (!ArchiDroidUtilities.getArchiDroidVersionType().equals("")) {
				if (!roArchiDroidVersionTypeRemote.equals(ArchiDroidUtilities.getArchiDroidVersionType())) {
					textCurrentVersionType.setTextColor(Color.RED);
				} else {
					textCurrentVersionType.setTextColor(Color.GREEN);
				}
			}

			textRemoteRom.setText(getString(R.string.textRom) + " " + roArchiDroidRomRemote);
			textRemoteRom.setTextColor(Color.GREEN);
			if (!ArchiDroidUtilities.getArchiDroidRom().equals("")) {
				if (!roArchiDroidRomRemote.equals(ArchiDroidUtilities.getArchiDroidRom())) {
					textCurrentRom.setTextColor(Color.RED);
				} else {
					textCurrentRom.setTextColor(Color.GREEN);
				}
			}

			textRemoteDevice.setText(getString(R.string.textDevice) + " " + roArchiDroidDeviceRemote);
			textRemoteDevice.setTextColor(Color.GREEN);
			if (!ArchiDroidUtilities.getArchiDroidDevice().equals("")) {
				if (!roArchiDroidDeviceRemote.equals(ArchiDroidUtilities.getArchiDroidDevice())) {
					textCurrentDevice.setTextColor(Color.RED);
				} else {
					textCurrentDevice.setTextColor(Color.GREEN);
				}
			}

			buttonDownload.setEnabled(true);
			progressDialog.dismiss();
		}
	}

	private class downloadFileDirect extends AsyncTask<String, Integer, Boolean> {

		private final Context context;

		private ProgressDialog progressDialog;

		public downloadFileDirect(final Context context) {
			this.context = context;
		}

		@Override
		protected final void onPreExecute() {
			super.onPreExecute();
			progressDialog = ArchiDroidUtilities.getProgressDialogWithProgress(context, "[1/2] Downloading ArchiDroid ZIP from GitHub...");
			progressDialog.show();
		}

		@Override
		protected final void onProgressUpdate(final Integer... progress) {
			super.onProgressUpdate(progress);
			if (progress[0] < 0) {
				progressDialog.setIndeterminate(true); // GitHub told us to fuck off #yolo
				progressDialog.setMessage("[1/2] Downloading ArchiDroid ZIP from GitHub... Server didn't send Content-Length, progress bar is not available, stay patient, it's working...");
			} else {
				progressDialog.setProgress(progress[0]);
			}
		}

		@Override
		protected final Boolean doInBackground(final String... sUrl) {

			new File(ArchiDroidUtilities.getArchiDroidTempDir()).mkdirs();
			ArchiDroidUtilities.deleteRecursive(new File(ArchiDroidUtilities.getUpdateTargetTempFile()));

			InputStream input = null;
			OutputStream output = null;
			HttpURLConnection connection = null;
			try {
				final URL url = new URL(sUrl[0]);
				connection = (HttpURLConnection) url.openConnection();
				connection.connect();

				// expect HTTP 200 OK, so we don't mistakenly save error report
				// instead of the file
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					return false;
				}

				// this will be useful to display download percentage
				// might be -1: server did not report the length
				final int fileLength = connection.getContentLength();

				// download the file
				input = connection.getInputStream();
				output = new FileOutputStream(ArchiDroidUtilities.getUpdateTargetTempFile());

				final byte data[] = new byte[ArchiDroidUtilities.getDefaultBufferSize()];
				int count;
				if (fileLength > 0) {
					progressDialog.setMax(fileLength);
					int total = 0;
					while ((count = input.read(data)) >= 0) {
						total += count;
						publishProgress(total);
						output.write(data, 0, count);
					}
				} else {
					while ((count = input.read(data)) >= 0) {
						publishProgress(-1);
						output.write(data, 0, count);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			} finally {
				try {
					if (output != null)
						output.close();
					if (input != null)
						input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (connection != null)
					connection.disconnect();
			}
			return true;
		}


		@Override
		protected final void onPostExecute(final Boolean result) {
			progressDialog.dismiss();
			if (result) {
				ArchiDroidUtilities.showLongToast(context, "Success!");
			} else {
				mWakeLock.release();
				ArchiDroidUtilities.deleteRecursive(new File(ArchiDroidUtilities.getUpdateTargetTempFile()));
				ArchiDroidUtilities.showLongToast(context, "Failed!");
				return;
			}
			new parseZipFile(getActivity(), ArchiDroidUtilities.getUpdateTargetTempFile(), ArchiDroidUtilities.getUpdateTargetTempRepackedFile()).execute();
		}
	}

	private class parseZipFile extends AsyncTask<Void, Integer, Boolean> {

		private final Context context;
		private final String pathToInputZip;
		private final String pathToOutputZip;

		private ProgressDialog progressDialog;

		public parseZipFile(final Context context, final String pathToInputZip, final String pathToOutputZip) {
			this.context = context;
			this.pathToInputZip = pathToInputZip;
			this.pathToOutputZip = pathToOutputZip;
		}

		@Override
		protected final void onPreExecute() {
			super.onPreExecute();
			progressDialog = ArchiDroidUtilities.getProgressDialogWithProgress(context, "[2/2] Parsing ArchiDroid zip, this may take a while, please wait...");
			progressDialog.show();
		}

		@Override
		protected final void onProgressUpdate(final Integer... progress) {
			progressDialog.setProgress(progress[0]);
		}

		@Override
		protected final Boolean doInBackground(final Void... urls) {

			// Declare all unreliable streams
			ZipOutputStream outputZip = null;
			BufferedInputStream is = null;

			try {
				final File outputFile = new File(pathToOutputZip);
				ArchiDroidUtilities.deleteRecursive(outputFile);
				final ZipFile inputZip = new ZipFile(pathToInputZip);
				final Enumeration<? extends ZipEntry> enu = inputZip.entries();
				int counter = 0;
				progressDialog.setMax(inputZip.size());

				outputZip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));

				// Compression
				// Github should already compress our zip
				/*outputZip.setMethod(ZipOutputStream.DEFLATED);
				outputZip.setLevel(1);*/

				while (enu.hasMoreElements()) {
					// Handle progress
					counter++;
					publishProgress(counter);

					// Get next zipEntry - could be file or directory
					final ZipEntry zipEntry = enu.nextElement();
					String nameOfFileInZip = zipEntry.getName();

					// If it's a directory, continue, we don't care about that unless files are inside
					if (nameOfFileInZip.endsWith("/")) {
						continue;
					}

					// Fix path to get rid of initial RepoName-BranchName structure added by GitHub
					// Otherwise recovery will have no idea what is it
					final String[] splitNameOfFileInZip = nameOfFileInZip.split("/");
					nameOfFileInZip = splitNameOfFileInZip[1];
					for (int x = 2; x < splitNameOfFileInZip.length; x++) {
						nameOfFileInZip += "/" + splitNameOfFileInZip[x];
					}

					// Add our zipEntry to output zip
					outputZip.putNextEntry(new ZipEntry(nameOfFileInZip));

					// Now read the content of the original zipEntry and pass it to our new entry
					is = new BufferedInputStream(inputZip.getInputStream(zipEntry));
					final byte[] bytes = new byte[ArchiDroidUtilities.getDefaultBufferSize()];
					int count;
					while ((count = is.read(bytes)) != -1) {
						outputZip.write(bytes, 0, count);
					}

					// Close our temporary stream and tell outputZip that we're done with this entry
					is.close();
					outputZip.closeEntry();
				}
				// Tell outputZip that we're done with the whole file
				outputZip.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			} finally {
				try {
					if (is != null)
						is.close();
					if (outputZip != null)
						outputZip.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return true;
		}

		@Override
		protected final void onPostExecute(final Boolean result) {
			super.onPostExecute(result);
			mWakeLock.release();
			progressDialog.dismiss();

			final File preFinalFile = new File(pathToOutputZip);

			if (result) {
				ArchiDroidUtilities.deleteRecursive(new File(pathToInputZip));
				ArchiDroidUtilities.showLongToast(context, "Success!");
			} else {
				ArchiDroidUtilities.deleteRecursive(preFinalFile);
				ArchiDroidUtilities.showLongToast(context, "Failed!");
				return;
			}

			final File finalFile = new File(ArchiDroidUtilities.getUpdateTargetFinalFile());

			ArchiDroidUtilities.deleteRecursive(finalFile);
			preFinalFile.renameTo(finalFile);

			DialogOK dialog = new DialogOK();
			dialog.setArgs("Success!", "It looks like everything ended successfully! You can find your flashable zip in " + finalFile.getPath());
			if (isActive) {
				dialog.show(getFragmentManager(), null);
			} else {
				dialogList.add(dialog);
			}
		}
	}

	private class downloadFileGit extends AsyncTask<Void, String, Boolean> {

		private final Context context;
		private final String ArchiDroidGithubBranch;

		private ProgressDialog progressDialog;
		private String progressString = "Executing git...";

		private PipedOutputStream mPOut;
		private PipedInputStream mPIn;
		private LineNumberReader mReader;
		private Process mProcess;

		public downloadFileGit(final Context context, final String ArchiDroidGithubBranch) {
			this.context = context;
			this.ArchiDroidGithubBranch = ArchiDroidGithubBranch;
		}

		@Override
		protected final void onPreExecute() {
			super.onPreExecute();
			progressDialog = ArchiDroidUtilities.getProgressDialog(context, progressString);
			progressDialog.show();
			mPOut = new PipedOutputStream();
			try {
				mPIn = new PipedInputStream(mPOut);
				mReader = new LineNumberReader(new InputStreamReader(mPIn));
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		@Override
		protected final Boolean doInBackground(final Void... params) {
			final String targetDir = ArchiDroidUtilities.getArchiDroidInternalDir() +  "/GitHub/" + ArchiDroidGithubBranch;
			final String commandLinux = "ARCHIDROID_LINUX " + ArchiDroidUtilities.getArchiDroidLinux() + " --command ";
			final String commandClone = commandLinux + "\"git clone https://github.com/" + ArchiDroidUtilities.getGithubRepo() + " --depth 1 --progress --branch " + ArchiDroidGithubBranch + " " + targetDir + "\"";
			final String commandSync = commandLinux + "\"cd " + targetDir + " && git pull --progress origin " + ArchiDroidGithubBranch + "\"";

			int errorCode = 1;

			try {
				final ProcessBuilder pb;
				if (ArchiDroidUtilities.rootFileExists(targetDir)) {
					progressString = "Syncing GitHub repo...";
					pb = new ProcessBuilder("su", "-c", commandSync);
				} else {
					progressString = "Cloning GitHub repo...";
					pb = new ProcessBuilder("su", "-c", commandClone);
				}
				pb.redirectErrorStream(true);
				mProcess = pb.start();

				InputStream in = mProcess.getInputStream();
				OutputStream out = mProcess.getOutputStream();
				byte[] buffer = new byte[1024];

				int count;
				while ((count = in.read(buffer)) != -1) {
					mPOut.write(buffer, 0, count);
					publishProgress();
				}

				out.close();
				in.close();
				mPOut.close();
				mPIn.close();

				errorCode = mProcess.waitFor();
			} catch (Exception e) {
				e.printStackTrace();
				//mProcess.destroy();
				return false;
			} finally {
				try {
					mPOut.close();
					mPIn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			return errorCode == 0;
		}
		@Override
		protected final void onProgressUpdate(final String... values) {
			try {
				while (mReader.ready()) {
					progressDialog.setMessage(progressString + "\n" + mReader.readLine());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected final void onPostExecute(final Boolean result) {
			super.onPostExecute(result);
			progressDialog.dismiss();

			if (result) {
				ArchiDroidUtilities.showLongToast(context, "Success!");
			} else {
				ArchiDroidUtilities.showLongToast(context, "Failed!");
			}
			new zipGitDirectory(context, ArchiDroidGithubBranch, ArchiDroidUtilities.getArchiDroidInternalDir() + "/tmp/ArchiDroid.zip").execute();
		}
	}

	private class zipGitDirectory extends AsyncTask<Void, Void, Boolean> {

		private final Context context;
		private final String ArchiDroidGithubBranch;
		private final String outputFile;

		private ProgressDialog progressDialog;
		private final String progressString = "Creating zip...";

		private Process mProcess;

		public zipGitDirectory(final Context context, final String ArchiDroidGithubBranch, final String outputFile) {
			this.context = context;
			this.ArchiDroidGithubBranch = ArchiDroidGithubBranch;
			this.outputFile = outputFile;
		}

		@Override
		protected final void onPreExecute() {
			progressDialog = ArchiDroidUtilities.getProgressDialog(context, progressString);
			progressDialog.show();
		}

		@Override
		protected final Boolean doInBackground(final Void... params) {
			final String targetDir = ArchiDroidUtilities.getArchiDroidInternalDir() +  "/GitHub/" + ArchiDroidGithubBranch;
			final String commandLinux = "ARCHIDROID_LINUX " + ArchiDroidUtilities.getArchiDroidLinux() + " --command ";
			final String commandZip = commandLinux + "\"cd " + targetDir + " && 7za a -bd -y -tzip -mx1 -xr\\!.git -xr\\!__build " + outputFile + " . >/dev/null 2>&1\"";
			ArchiDroidUtilities.log(commandZip);

			int errorCode = 1;

			try {
				final ProcessBuilder pb;
				if (ArchiDroidUtilities.rootFileExists(targetDir)) {
					ArchiDroidUtilities.rootDeleteFile(outputFile);
					pb = new ProcessBuilder("su", "-c", commandZip);
				} else {
					return false;
				}
				pb.redirectErrorStream(true);
				mProcess = pb.start();
				errorCode = mProcess.waitFor();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}

			if (errorCode == 0) {
				ArchiDroidUtilities.rootRenameFile(outputFile, ArchiDroidUtilities.getArchiDroidInternalDir() + "/ArchiDroid.zip");
				return true;
			} else {
				return false;
			}
		}

		@Override
		protected final void onPostExecute(final Boolean result) {
			super.onPostExecute(result);
			mWakeLock.release();
			progressDialog.dismiss();

			final DialogOK dialog = new DialogOK();
			if (result) {
				dialog.setArgs("Success!", "You can find your flashable zip in " + ArchiDroidUtilities.getArchiDroidInternalDir() + "/ArchiDroid.zip");
			} else {
				dialog.setArgs("Error!", "Something went wrong... :(");
			}
			if (isActive) {
				dialog.show(getFragmentManager(), null);
			} else {
				dialogList.add(dialog);
			}

		}
	}
}