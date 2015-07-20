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

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public final class ArchiDroidFragmentBackend extends Fragment {

	public ArchiDroidFragmentBackend() {
	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_backend, container, false);
	}

	@Override
	public final void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (ArchiDroidUtilities.isSystemFullySupported()) {
			ArchiDroidUtilities.setBackendSwitches(parseSwitches(getActivity(), "/system/archidroid/dev/switches", true));
			ArchiDroidUtilities.setBackendSpinners(parseSpinners(getActivity(), "/system/archidroid/dev/spinners"));
			ArchiDroidUtilities.setBackendProcessCheckBoxes(parseProcessCheckBoxes(getActivity(), "/system/archidroid/dev/pcbs"));
		}
	}

	private ImageView getDivider(final Context context) {
		final ImageView divider = new ImageView(context);
		divider.setBackgroundColor(Color.WHITE);
		final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(25, 25, 25, 25);
		divider.setLayoutParams(lp);
		return divider;
	}

	private ArrayList<BackendProcessCheckBox> parseProcessCheckBoxes(final Context context, final String path) {
		final LinearLayout layout = (LinearLayout) getView().findViewById(R.id.layoutBackend);
		final ArrayList<BackendProcessCheckBox> pcbs = new ArrayList<>();
		final File mainFile = new File(path);
		final int maxLength = 3;
		int currentLength = 0;
		LinearLayout row = null;
		if (mainFile.isDirectory()) {
			for (final File file : mainFile.listFiles()) {
				if (file.isFile()) {
					if (currentLength <= 0) {
						row = new LinearLayout(context);
						row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
						row.setGravity(Gravity.CENTER);
						currentLength = maxLength;
						layout.addView(row);
					}
					currentLength--;

					final BackendProcessCheckBox pcb = new BackendProcessCheckBox(context, file);
					pcbs.add(pcb);

					row.addView(pcb);
				}
			}

			row = new LinearLayout(context);
			row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			row.setGravity(Gravity.CENTER);
			layout.addView(row);
			final Button refreshPCBs = new Button(context);
			row.addView(refreshPCBs);
			refreshPCBs.setText(getString(R.string.buttonRefreshProcesses));
			refreshPCBs.setOnClickListener(new Button.OnClickListener() {
				@Override
				public final void onClick(final View v) {
					for (final BackendProcessCheckBox pcb : pcbs) {
						pcb.refresh();
					}
				}
			});
		}
		return pcbs;
	}

	private ArrayList<BackendSpinner> parseSpinners(final Context context, final String path) {
		final LinearLayout layout = (LinearLayout) getView().findViewById(R.id.layoutBackend);
		final ArrayList<BackendSpinner> spinners = new ArrayList<>();
		final File mainFile = new File(path);
		if (mainFile.isDirectory()) {
			for (final File file : mainFile.listFiles()) {
				if (file.isFile()) {
					final LinearLayout row = new LinearLayout(context);
					row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

					final BackendSpinner s = new BackendSpinner(context, file);
					spinners.add(s);
					s.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

					final TextView tv = new TextView(context);
					tv.setText(file.getName() + ": ");
					tv.setTextSize(18);

					row.addView(tv);
					row.addView(s);
					layout.addView(row);

					final String target = ArchiDroidUtilities.getSymlinkTargetName(file);
					final File child = new File(path + "/" + "_" + file.getName());
					if (child.isDirectory()) {
						int position = -1;
						int finalPosition = -1;
						final ArrayList<String> spinnerArray = new ArrayList<>();
						for (final File f : child.listFiles()) {
							position++;
							final String fName = f.getName();
							spinnerArray.add(fName);
							if (finalPosition == -1 && fName.equals(target)) {
								finalPosition = position;
							}
						}
						final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, spinnerArray);
						spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						s.setAdapter(spinnerAdapter);
						s.setSelection(finalPosition);
					}
				}
			}
		}
		return spinners;
	}

	private ArrayList<BackendSwitch> parseSwitches(final Context context, final String path, final boolean isParentEnabled) {
		final LinearLayout layout = (LinearLayout) getView().findViewById(R.id.layoutBackend);
		final ArrayList<BackendSwitch> switches = new ArrayList<>();
		final File mainFile = new File(path);
		if (mainFile.isDirectory()) {
			for (final File file : mainFile.listFiles()) {
				if (file.isFile()) {
					final LinearLayout row = new LinearLayout(context);
					row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

					final BackendSwitch s = new BackendSwitch(context, file);
					switches.add(s);
					s.setText(file.getName());
					s.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
					s.setEnabled(isParentEnabled);

					row.addView(s);
					row.addView(getDivider(context));
					layout.addView(row);

					final File child = new File(path + "/" + "_" + file.getName());
					if (child.isDirectory()) {
						final ArrayList<BackendSwitch> childs;
						if (isParentEnabled) {
							childs = parseSwitches(context, child.getAbsolutePath(), s.isEnabled()); // recursive, yolo
						} else {
							childs = parseSwitches(context, child.getAbsolutePath(), false); // recursive, yolo
						}
						s.addChilds(childs);
					}
				}
			}
		}
		return switches;
	}
}