package com.xcv58.testfordelayedtask;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Collections;


public class MainActivity extends Activity {
    public final static String EXTRA_MESSAGE = "com.xcv58.testfordelayedtask.MESSAGE";
    public final static String TAG = "TestForDelayedTask";

    private DelayedTaskService mService;
    private boolean mBound;
    private int requestCode;
    private final static int DELAYED_TIME = 5000;
    private static boolean delayedTimeSet;


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
//            Log.d(TAG, "ServiceConnection");
            DelayedTaskService.LocalBinder binder = (DelayedTaskService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, DelayedTaskService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        setContentView(R.layout.activity_main);
        requestCode = 1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void release(View view) {
        Log.d(TAG, "Release button pressed.");
        if (mBound) {
            Log.d(TAG, "Release button pressed. Service bounded");
            mService.releaseAll();
        }
    }

    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
        TextView textView = (TextView) findViewById(R.id.text_view);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        EditText timeText = (EditText) findViewById(R.id.delayed_time);

        String timeString = timeText.getText().toString();

        int delay = timeString.length() > 0 ? Integer.parseInt(timeString) : 0;

        String origin = textView.getText().toString();
        String message = editText.getText().toString();
        textView.setText(origin + "\n" + message + "\t" + delay);

        editText.setText("");
        timeText.setText("");

        if (mBound) {
            Intent intent = new Intent(this, DisplayMessageActivity.class);
            intent.putExtra(EXTRA_MESSAGE, message + " " + System.currentTimeMillis());

            PendingIntent pintent = PendingIntent.getActivity(getBaseContext(), requestCode++, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            this.initDelay(delay * 1000);
            mService.addDelayedTask(pintent);
//            mService.setDelayedTask(pintent, delay);
        }

//        textView.setText(editText.getText().toString());

//        Intent intent = new Intent(this, DisplayMessageActivity.class);
//        String message = editText.getText().toString();
//        intent.putExtra(EXTRA_MESSAGE, message);

//        Log.d("TEST", " " + delay);
//        PendingIntent pintent = PendingIntent.getActivity(getBaseContext(), 1,intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        joulerPolicy.setDelayedTask(pintent, delay);
    }

    private boolean initDelay(int delay) {
        if (delayedTimeSet) {
            return false;
        }
        if (delay < 0 ) {
            Log.e(TAG, "Wrong delay time.");
            return false;
        }
        delayedTimeSet = true;
        return mService.setMaximumDelayTime(getApplicationContext(), delay);
    }

    private boolean initDelay() {
        return this.initDelay(DELAYED_TIME);
    }

}
