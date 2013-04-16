/*
 * Copyright (C) 2013 readyState Software Ltd
 * Copyright (C) 2006 The Android Open Source Project
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
import java.util.HashSet;
import java.util.List;

import com.readystatesoftware.notificationlog.utils.SharedPreferencesCompat;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

/**
 * API for sending log output.
 *
 * <p>Generally, use the Log.v() Log.d() Log.i() Log.w() and Log.e()
 * methods.
 *
 * <p>The order in terms of verbosity, from least to most is
 * ERROR, WARN, INFO, DEBUG, VERBOSE.  Verbose should never be compiled
 * into an application except during development.  Debug logs are compiled
 * in but stripped at runtime.  Error, warning and info logs are always kept.
 *
 * <p><b>Tip:</b> A good convention is to declare a <code>TAG</code> constant
 * in your class:
 *
 * <pre>private static final String TAG = "MyActivity";</pre>
 *
 * and use that in subsequent calls to the log methods.
 * </p>
 *
 * <p><b>Tip:</b> Don't forget that when you make a call like
 * <pre>Log.v(TAG, "index=" + i);</pre>
 * that when you're building the string to pass into Log.d, the compiler uses a
 * StringBuilder and at least three allocations occur: the StringBuilder
 * itself, the buffer, and the String object.  Realistically, there is also
 * another buffer allocation and copy, and even more pressure on the gc.
 * That means that if your log message is filtered out, you might be doing
 * significant work and incurring significant overhead.
 */
public final class Log {

    /**
     * Priority constant for the println method; use Log.v.
     */
    public static final int VERBOSE = android.util.Log.VERBOSE;

    /**
     * Priority constant for the println method; use Log.d.
     */
    public static final int DEBUG = android.util.Log.DEBUG;

    /**
     * Priority constant for the println method; use Log.i.
     */
    public static final int INFO = android.util.Log.INFO;

    /**
     * Priority constant for the println method; use Log.w.
     */
    public static final int WARN = android.util.Log.WARN;

    /**
     * Priority constant for the println method; use Log.e.
     */
    public static final int ERROR = android.util.Log.ERROR;
    
    /**
     * Priority constant for the println method.
     */
    public static final int ASSERT = android.util.Log.ASSERT;
    
    public static final int WTF = 99;
    
    private static final int MAX_BUFFER_SIZE = 1000;
    private static final int NOTIFICATION_ID = 1138;
    private static final String PREFS_NAME = "preferences_notificationlog";
    private static final String PREF_LEVEL = "level";
    private static final String PREF_FILTER = "filter";
    
    private static final Log sLog = new Log();
    private static boolean sNotificationsEnabled = true;
    private static boolean sToastsEnabled = false;
    
    private Context mContext;
    private Toast mLogToast0;
    private Toast mLogToast1;
    private boolean mLastToast0 = true;
    private Notification mNotification;
    private NotificationManager mNotificationManager;
    private SharedPreferences mPrefs;
    private int mIcon;
    private String mLabel;
    private PendingIntent mViewIntent;
    private PendingIntent mFilterIntent;
    private PendingIntent mLevelIntent;
    private PendingIntent mClearIntent;
    private boolean mActivityIntegrationAvailable;
    
    private int mLevel;
    private HashSet<String> mFilterOptions = new HashSet<String>();
    private String mFilter;
    private ArrayList<LogEntry> mEntries = new ArrayList<LogEntry>(); 
    
    private Log() {
    }
        
    @Override
	protected void finalize() throws Throwable {
    	// attempt to cancel notification
    	if (mNotificationManager != null) {
    		mNotificationManager.cancel(NOTIFICATION_ID);
    	}
		super.finalize();
	}

	public static void initNotifications(Context context) {
    	initNotifications(context, 0);
    }
    
    public static void initNotifications(Context context, int icon) {
    	sLog.mContext = context;
    	sLog.mLogToast0 = new Toast(context);
    	sLog.mLogToast1 = new Toast(context);
    	sLog.mIcon = (icon == 0) ? context.getApplicationInfo().icon : icon;
    	sLog.mLabel = context.getString(context.getApplicationInfo().labelRes);
    			
    	sLog.mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    	sLog.mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    	
    	sLog.mLevel = sLog.mPrefs.getInt(PREF_LEVEL, VERBOSE);
    	sLog.mFilter = sLog.mPrefs.getString(PREF_FILTER, null);
    	
    	Intent intent = new Intent(context, LogActivity.class);
    	sLog.mViewIntent = PendingIntent.getActivity(context, 0, intent, 0);
    	
    	Intent filterIntent = new Intent(context, LogActivity.class);
    	filterIntent.putExtra(LogActivity.ARG_ACTION, LogActivity.ACTION_FILTER);
    	sLog.mFilterIntent = PendingIntent.getActivity(context, 4, filterIntent, 0);
    	
    	Intent levelIntent = new Intent(context, LogActivity.class);
    	levelIntent.putExtra(LogActivity.ARG_ACTION, LogActivity.ACTION_LEVEL);
    	sLog.mLevelIntent = PendingIntent.getActivity(context, 2, levelIntent, 0);
    	
    	Intent clearIntent = new Intent(context, LogActivity.class);
    	clearIntent.putExtra(LogActivity.ARG_ACTION, LogActivity.ACTION_CLEAR);
    	sLog.mClearIntent = PendingIntent.getActivity(context, 3, clearIntent, 0);
    	
    	sLog.mActivityIntegrationAvailable = isActivityAvailable(context, LogActivity.class.getName());
    }
    
    public static void setNotificationsEnabled(boolean enable) {
    	sNotificationsEnabled = enable;
    }
    
    public static void setToastsEnabled(boolean enable) {
    	sToastsEnabled = enable;
    }
    
   public static String getNotificationFilter() {
	   return sLog.mFilter;
   }
    
    public static void setNotifactionFilter(String tag) {
    	if (sLog.mContext != null) {
    		sLog.mFilter = tag;
    		sLog.updateNotification();
    		Editor edit = sLog.mPrefs.edit();
    		edit.putString(PREF_FILTER, tag);
    		SharedPreferencesCompat.apply(edit);
    	}
    }
    
    public static int getNotifactionLevel() {
    	return sLog.mLevel;
    }
    
    public static void setNotifactionLevel(int level) {
    	if (sLog.mContext != null) {
    		sLog.mLevel = level;
    		sLog.updateNotification();
    		Editor edit = sLog.mPrefs.edit();
    		edit.putInt(PREF_LEVEL, level);
    		SharedPreferencesCompat.apply(edit);
    	}
    }
    
    public static ArrayList<LogEntry> getLogBuffer() {
    	return new ArrayList<LogEntry>(sLog.mEntries);
    }
    
    public static ArrayList<String> getFilterOptions() {
    	return new ArrayList<String>(sLog.mFilterOptions);
    }
    
    public static void clearLogbuffer() {
    	if (sLog.mContext != null) {
    		sLog.mEntries = new ArrayList<LogEntry>();
    		sLog.mNotificationManager.cancel(NOTIFICATION_ID);
    	}
    }
    
    private void addToEntryBuffer(int level, String tag, String msg) {
    	LogEntry e = new LogEntry(level, System.currentTimeMillis(), tag, msg);
    	mEntries.add(0, e);
    	if (mEntries.size() > MAX_BUFFER_SIZE) {
    		mEntries.remove(mEntries.size() - 1);
    	}
    	mFilterOptions.add(tag);
    }
    
    private synchronized void doNotify(int level, String tag, String msg) {
    	if (sNotificationsEnabled) {
    		addToEntryBuffer(level, tag, msg);
    		updateNotification();
    	}
    	if (sToastsEnabled) {
    		doToast(msg);
    	}
    }

	private void updateNotification() {
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
			.setSmallIcon(mIcon)
			.setContentTitle(mLabel);
		
		if (mActivityIntegrationAvailable) {
			mBuilder.addAction (0, "Filter", mFilterIntent)
				.addAction (0, "Level", mLevelIntent)
				.addAction (0, "Clear", mClearIntent)
				.setContentIntent(mViewIntent);
		}

		NotificationCompat.InboxStyle inboxStyle =
				new NotificationCompat.InboxStyle();

		int count = 0;
		for (int i=0; i < mEntries.size(); i++) {
			if ((mLevel == VERBOSE || mLevel == mEntries.get(i).getLevel()) && (mFilter == null || mFilter.equals(mEntries.get(i).getTag()))) {
				if (count <  10) {
					if (count == 0) mBuilder.setContentText(mEntries.get(i).getText());
					inboxStyle.addLine(mEntries.get(i).getText());
				}
				count++;
			}
		}
		mBuilder.setNumber(count);
		mBuilder.setStyle(inboxStyle);
		
		// issue the notification
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
		
	}
    
    private void doToast(String msg) {
    	try {
    		if (mLastToast0) {
    			mLogToast0.cancel();
    			mLogToast1 = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
    			mLogToast1.show();
    			mLastToast0 = false;
    		} else {
    			mLogToast1.cancel();
    			mLogToast0 = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
    			mLogToast0.show();
    			mLastToast0 = true;
    		}
    	} catch (RuntimeException re) {
    		// we cannot toast while off the main thread
    	}
    }
    
    private static boolean isActivityAvailable(Context context, String className) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent();
        final String packageName = context.getApplicationInfo().packageName;
        intent.setClassName(packageName, className);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /**
     * Send a {@link #VERBOSE} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int v(String tag, String msg) {
    	if (sLog.mContext != null) {
    		sLog.doNotify(VERBOSE, tag, msg);
    	}
        return android.util.Log.v(tag, msg);
    }

    /**
     * Send a {@link #VERBOSE} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int v(String tag, String msg, Throwable tr) {
    	if (sLog.mContext != null) {
    		sLog.doNotify(VERBOSE, tag, msg);
    	}
    	return android.util.Log.v(tag, msg, tr);
    }

    /**
     * Send a {@link #DEBUG} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int d(String tag, String msg) {
    	if (sLog.mContext != null) {
    		sLog.doNotify(DEBUG, tag, msg);
    	}
    	return android.util.Log.d(tag, msg);
    }

    /**
     * Send a {@link #DEBUG} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int d(String tag, String msg, Throwable tr) {
    	if (sLog.mContext != null) {
    		sLog.doNotify(DEBUG, tag, msg);
    	}
    	return android.util.Log.d(tag, msg, tr);
    }

    /**
     * Send an {@link #INFO} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int i(String tag, String msg) {
    	if (sLog.mContext != null) {
    		sLog.doNotify(INFO, tag, msg);
    	}
    	return android.util.Log.i(tag, msg);
    }

    /**
     * Send a {@link #INFO} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int i(String tag, String msg, Throwable tr) {
    	if (sLog.mContext != null) {
    		sLog.doNotify(INFO, tag, msg);
    	}
    	return android.util.Log.i(tag, msg, tr);
    }

    /**
     * Send a {@link #WARN} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int w(String tag, String msg) {
    	if (sLog.mContext != null) {
    		sLog.doNotify(WARN, tag, msg);
    	}
    	return android.util.Log.w(tag, msg);
    }

    /**
     * Send a {@link #WARN} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int w(String tag, String msg, Throwable tr) {
    	if (sLog.mContext != null) {
    		sLog.doNotify(WARN, tag, msg);
    	}
    	return android.util.Log.w(tag, msg, tr);
    }

    /**
     * Checks to see whether or not a log for the specified tag is loggable at the specified level.
     *
     *  The default level of any tag is set to INFO. This means that any level above and including
     *  INFO will be logged. Before you make any calls to a logging method you should check to see
     *  if your tag should be logged. You can change the default level by setting a system property:
     *      'setprop log.tag.&lt;YOUR_LOG_TAG> &lt;LEVEL>'
     *  Where level is either VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT, or SUPPRESS. SUPPRESS will
     *  turn off all logging for your tag. You can also create a local.prop file that with the
     *  following in it:
     *      'log.tag.&lt;YOUR_LOG_TAG>=&lt;LEVEL>'
     *  and place that in /data/local.prop.
     *
     * @param tag The tag to check.
     * @param level The level to check.
     * @return Whether or not that this is allowed to be logged.
     * @throws IllegalArgumentException is thrown if the tag.length() > 23.
     */
    public static native boolean isLoggable(String tag, int level);

    /*
     * Send a {@link #WARN} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param tr An exception to log
     */
    public static int w(String tag, Throwable tr) {
    	return android.util.Log.w(tag, tr);
    }

    /**
     * Send an {@link #ERROR} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int e(String tag, String msg) {
    	if (sLog.mContext != null) {
    		sLog.doNotify(ERROR, tag, msg);
    	}
    	return android.util.Log.e(tag, msg);
    }

    /**
     * Send a {@link #ERROR} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int e(String tag, String msg, Throwable tr) {
    	if (sLog.mContext != null) {
    		sLog.doNotify(ERROR, tag, msg);
    	}
    	return android.util.Log.e(tag, msg, tr);
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen.
     * The error will always be logged at level ASSERT with the call stack.
     * Depending on system configuration, a report may be added to the
     * {@link android.os.DropBoxManager} and/or the process may be terminated
     * immediately with an error dialog.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    @TargetApi(Build.VERSION_CODES.FROYO) 
    public static int wtf(String tag, String msg) {
    	if (sLog.mContext != null) {
    		sLog.doNotify(WTF, tag, msg);
    	}
    	return android.util.Log.wtf(tag, msg);
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen.
     * Similar to {@link #wtf(String, String)}, with an exception to log.
     * @param tag Used to identify the source of a log message.
     * @param tr An exception to log.
     */
    @TargetApi(Build.VERSION_CODES.FROYO) 
    public static int wtf(String tag, Throwable tr) {
    	return android.util.Log.wtf(tag, tr);
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen.
     * Similar to {@link #wtf(String, Throwable)}, with a message as well.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr An exception to log.  May be null.
     */
    @TargetApi(Build.VERSION_CODES.FROYO) 
    public static int wtf(String tag, String msg, Throwable tr) {
    	if (sLog.mContext != null) {
    		sLog.doNotify(WTF, tag, msg);
    	}
    	return android.util.Log.wtf(tag, msg, tr);
    }

    /**
     * Sets the terrible failure handler, for testing.
     *
     * @return the old handler
     *
     * @hide
     */
    @TargetApi(Build.VERSION_CODES.FROYO) 
    public static Object setWtfHandler(Object handler) {
    	return null;
    }

    /**
     * Handy function to get a loggable stack trace from a Throwable
     * @param tr An exception to log
     */
    public static String getStackTraceString(Throwable tr) {
    	return android.util.Log.getStackTraceString(tr);
    }

    /**
     * Low-level logging call.
     * @param priority The priority/type of this log message
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return The number of bytes written.
     */
    public static int println(int priority, String tag, String msg) {
        return android.util.Log.println(priority, tag, msg);
    }

    /** @hide */ public static final int LOG_ID_MAIN = 0;
    /** @hide */ public static final int LOG_ID_RADIO = 1;
    /** @hide */ public static final int LOG_ID_EVENTS = 2;
    /** @hide */ public static final int LOG_ID_SYSTEM = 3;

    /** @hide */ public static native int println_native(int bufID,
            int priority, String tag, String msg);
}
