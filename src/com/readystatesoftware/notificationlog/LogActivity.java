/*
 * Copyright (C) 2013 readyState Software Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.readystatesoftware.notificationlog;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LogActivity extends Activity {

	public static final String ARG_ACTION = "com.readystatesoftware.notificationlog.ARG_ACTION";
	
	public static final int ACTION_VIEW = 1;
	public static final int ACTION_FILTER = 2;
	public static final int ACTION_LEVEL = 3;
	public static final int ACTION_CLEAR = 4;
	
	WebView wv;
	int action;
	int selected;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		wv = new WebView(this);
		setContentView(wv, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		setTitle("Log");
		wv.setWebViewClient(new Callback());
		
		action = getIntent().getIntExtra(ARG_ACTION, ACTION_VIEW);
		init(true);
		
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		action = intent.getIntExtra(ARG_ACTION, ACTION_VIEW);
		init(false);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        MenuItem item = menu.add(0, ACTION_FILTER, 0, "Filter");
        if (Build.VERSION.SDK_INT >= 11) {
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
        
        item = menu.add(0, ACTION_LEVEL, 0, "Level");
        if (Build.VERSION.SDK_INT >= 11) {
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
        
        item = menu.add(0, ACTION_CLEAR, 0, "Clear");
        if (Build.VERSION.SDK_INT >= 11) {
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
        
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		action = item.getItemId();
		init(false);
		return true;
	}

	private void init(boolean newInstance) {
		
		switch (action) {
		case ACTION_VIEW:
			updateLogDisplay();
			break;
		case ACTION_CLEAR:
			Log.clearLogbuffer();
			finish();
			break;
		case ACTION_LEVEL:
			showLevelDialog(newInstance);
			break;
		case ACTION_FILTER:
			showFilterDialog(newInstance);
			break;
		default:
			break;
		}
		
	}

	private void updateLogDisplay() {
		
		ArrayList<LogEntry> data = Log.getLogBuffer();
		
		if (data != null) {
			StringBuilder body = new StringBuilder();
			body.append("<html><head>");
			body.append("</head><body><pre>");
			for (int i=data.size()-1; i >= 0; i--) {
				LogEntry logEntry = data.get(i);
				if (Log.getNotifactionLevel() == Log.VERBOSE || Log.getNotifactionLevel() == logEntry.getLevel()) {
					if (Log.getNotificationFilter() == null || Log.getNotificationFilter().equals(logEntry.getTag())) {
						body.append(logEntry);
					}
				}
			}
			body.append("</pre></body></html>");
			wv.loadDataWithBaseURL("file:///android_asset/", body.toString(), "text/html", "utf-8", null);
			
		}
		
	}

	private void showFilterDialog(final boolean finishOnOk) {
		
		ArrayList<String> t = Log.getFilterOptions();
		
		int j = 0;
		selected = 0;
		for (String val : t) {
			if (val.equals(Log.getNotificationFilter())) {
				selected = j+1;
			}
			j++;
		}
		
		t.add(0, "None");
		final String[] tags = t.toArray(new String[t.size()]);
		
		AlertDialog dlg = new AlertDialog.Builder(this)
		.setTitle("Tag Filter")
		.setSingleChoiceItems(tags, selected, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		    	selected = which;
		    }
		})
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        	if (selected == 0) {
		        		Log.setNotifactionFilter(null);
		        	} else {
		        		Log.setNotifactionFilter(tags[selected]);
		        	}
		        	if (finishOnOk) {
		        		finish();
		        	} else {
		        		updateLogDisplay();
		        	}
		        }
		    })
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        	if (finishOnOk) {
		        		finish();
		        	}
		        }
		    })
		.create();
		
		dlg.show();
		
	}

	private void showLevelDialog(final boolean finishOnOk) {
		
		final String[] items = { "Verbose", "Debug", "Info", "Warn", "Error", "Assert" };
		final int[] values = { Log.VERBOSE, Log.DEBUG, Log.INFO, Log.WARN, Log.ERROR, Log.ASSERT };
		
		int i = 0;
		for (int val : values) {
			if (val == Log.getNotifactionLevel()) {
				selected = i;
			}
			i++;
		}
		
		AlertDialog dlg = new AlertDialog.Builder(this)
		.setTitle("Log Level")
		.setSingleChoiceItems(items, selected, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		    	selected = which;
		    }
		})
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        	Log.setNotifactionLevel(values[selected]);
		        	if (finishOnOk) {
		        		finish();
		        	} else {
		        		updateLogDisplay();
		        	}
		        }
		    })
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        	if (finishOnOk) {
		        		finish();
		        	}
		        }
		    })
		.create();
		
		dlg.show();
		
	}
	
	private class Callback extends WebViewClient {

		private Handler mScrollHandler = new Handler();
		
		private Runnable mScrollRunner = new Runnable() {
			public void run() {
				wv.pageDown(true);
			}
		};
		
		@Override
		public void onPageFinished(WebView view, String url) {
			mScrollHandler.removeCallbacks(mScrollRunner);
			mScrollHandler.postDelayed(mScrollRunner, 300);
		}
			
	}

}
