package com.readystatesoftware.notificationlog.example;

import com.readystatesoftware.notificationlog.Log;

import android.app.Application;

public class ExampleApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		if (BuildConfig.DEBUG) {
			Log.initialize(this, R.drawable.ic_stat_log);
		}
	}

}
