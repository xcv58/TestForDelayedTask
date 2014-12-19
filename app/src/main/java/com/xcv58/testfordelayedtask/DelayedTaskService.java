package com.xcv58.testfordelayedtask;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

/**
 * Created by xcv58 on 12/19/14.
 */
public class DelayedTaskService extends Service {
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        DelayedTaskService getService() {
            return DelayedTaskService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getBaseContext(), "onStartCommand", Toast.LENGTH_SHORT).show();
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setDelayedTask(PendingIntent pendingIntent, int delay) {
        String packageName = pendingIntent.getCreatorPackage();
        int uid = pendingIntent.getCreatorUid();
        Toast.makeText(getBaseContext(), packageName + "\t" + uid, Toast.LENGTH_SHORT).show();
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
}
