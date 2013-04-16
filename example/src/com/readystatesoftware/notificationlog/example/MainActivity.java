package com.readystatesoftware.notificationlog.example;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.readystatesoftware.notificationlog.Log;

public class MainActivity extends Activity {

	private static final String TAG_HELLO = "Hello";
	private static final String TAG_THREAD = "Thread";
	private static final String TAG_TOAST = "Toast";
	private static final String TAG_SPAM = "Spam";
	private static final String TAG_DISABLED = "Disabled";
	
	private MySpamTask task;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button b;
        
        b = (Button) findViewById(R.id.hello_button);
        b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG_HELLO, "verbose message");
				Log.d(TAG_HELLO, "debug message");
				Log.i(TAG_HELLO, "info message");
				Log.w(TAG_HELLO, "warning message");
				Log.e(TAG_HELLO, "error message");
				Log.wtf(TAG_HELLO, "wtf message");
			}
		});
        
        b = (Button) findViewById(R.id.toast_button);
        b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.setToastsEnabled(true);
				Log.i(TAG_TOAST, "toast message");
				Log.setToastsEnabled(false);
			}
		});
        
        b = (Button) findViewById(R.id.disabled_button);
        b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.setNotificationsEnabled(false);
				Log.d(TAG_DISABLED, "this message will not appear in notification");
				Log.setNotificationsEnabled(true);
			}
		});
        
        b = (Button) findViewById(R.id.async_button);
        b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new MyAsyncTask().execute();
			}
		});
        
        b = (Button) findViewById(R.id.spam_button);
        b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (task == null) {
					task = new MySpamTask();
					task.execute();
					((Button) v).setText(R.string.stop_spam);
				} else {
					task.cancel(true);
					task = null;
					((Button) v).setText(R.string.start_spam);
				}
			}
		});
        
    }
   
    @Override
	protected void onDestroy() {
		if (task != null) {
			task.cancel(true);
		}
		super.onDestroy();
	}

	class MyAsyncTask extends AsyncTask<Void, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Void... arg0) {
        	Log.i(TAG_THREAD, "start thread");
        	for(int i=0; i < 10; i++) {
        		Log.w(TAG_THREAD, "time=" + System.currentTimeMillis());
        	}
        	Log.i(TAG_THREAD, "end thread");
            return true;
        }	
    }
    
    class MySpamTask extends AsyncTask<Void, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Void... arg0) {
        	Log.d(TAG_SPAM, "start spam");
        	while (true) {
        		if (isCancelled()) {
        			return true;
        		}
        		int random = (int)(Math.random() * ((Cheeses.sCheeseStrings.length-1) + 1));
	        	Log.i(TAG_SPAM, Cheeses.sCheeseStrings[random]);
	        	try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
        	}
        }
		@Override
		protected void onCancelled(Boolean result) {
			Log.d(TAG_SPAM, "stop spam");
		}
    }
    
}
