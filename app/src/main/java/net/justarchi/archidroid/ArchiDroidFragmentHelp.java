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

import android.app.Fragment;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public final class ArchiDroidFragmentHelp extends Fragment {

	private ProgressDialog progressDialog;

	private WebView webViewHelp;
	private Button buttonHelpLoad;

	public ArchiDroidFragmentHelp() {
	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_help, container, false);
	}

	@Override
	public final void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		webViewHelp = (WebView) getView().findViewById(R.id.webViewHelp);
		webViewHelp.setBackgroundColor(Color.TRANSPARENT);
		//webViewHelp.getSettings().setJavaScriptEnabled(true); // enable javascript
		webViewHelp.setWebViewClient(new WebViewClient() {
			private int running = 0;

			@Override
			public final boolean shouldOverrideUrlLoading(final WebView view, final String urlNewString) {
				//if (urlNewString.equals(ArchiDroidUtilities.getGithubWiki())) {
				running++;
				webViewHelp.loadUrl(urlNewString);
				//}
				return true;
			}

			@Override
			public final void onPageStarted(final WebView view, final String url, final Bitmap favicon) {
				running = Math.max(running, 1);
			}

			@Override
			public final void onPageFinished(final WebView view, final String url) {
				if (--running == 0) {
					if (progressDialog != null && progressDialog.isShowing()) {
						progressDialog.hide();
					}
				}
			}
		});

		buttonHelpLoad = (Button) getView().findViewById(R.id.buttonLaunchShell);
		buttonHelpLoad.setOnClickListener(new Button.OnClickListener() {
			@Override
			public final void onClick(final View v) {
				if (!ArchiDroidUtilities.isConnected(v.getContext())) {
					ArchiDroidUtilities.showShortToast(v.getContext(), "No connection available!");
					return;
				}
				progressDialog = ArchiDroidUtilities.getProgressDialog(getActivity(), "Loading...");
				progressDialog.show();
				webViewHelp.loadUrl(ArchiDroidUtilities.getGithubWiki());
			}
		});
	}
}