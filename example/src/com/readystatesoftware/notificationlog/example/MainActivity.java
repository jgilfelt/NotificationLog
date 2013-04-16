package com.readystatesoftware.notificationlog.example;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.readystatesoftware.notificationlog.Log;

public class MainActivity extends Activity {

	private static final String TAG = "Example";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button b = (Button) findViewById(R.id.hello_button);
        b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(TAG, "start loop");
				Log.setToastsEnabled(true);
		        for(int i=0; i < 5; i++) {
		        	Log.d(TAG, "i=" + i);
		        }
		        Log.setToastsEnabled(false);
		        Log.i(TAG, "end loop " + System.currentTimeMillis());
			}
		});
        
        b = (Button) findViewById(R.id.async_button);
        b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.setToastsEnabled(true);
				Log.e(TAG, "start async");
				new MyAsyncTask().execute();
			}
		});
        
    }
    
    class MyAsyncTask extends AsyncTask<Void, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Void... arg0) {
        	for(int k=0; k < 5; k++) {
	        	Log.w(TAG, "k=" + k + " Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
	        }
        	Log.v(TAG, "end async");
            return true;
        }	
    }
    
}
