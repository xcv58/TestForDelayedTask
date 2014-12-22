package com.xcv58.testfordelayedtask;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by xcv58 on 12/19/14.
 */
public class DelayedTaskService extends Service {
    private class PendingIntentPool {
        private List<PendingIntent> pendingIntentList;
        private Handler handler;
        private ReentrantLock lock;
        private boolean existBunchTask;
        private int delayedTime;

        private final Runnable runnable = new Runnable() {
            public void run() {
                Log.d(TAG, "Acquire lock from runnable");
                lock.lock();
                List<PendingIntent> tmpList = pendingIntentList;
                pendingIntentList = new ArrayList<PendingIntent>();
                existBunchTask = false;
                Log.d(TAG, "Release lock from runnable");
                lock.unlock();
                try {
                    Log.d(TAG, "" + tmpList.size());
                    for (PendingIntent pendingIntent : tmpList) {
//                                Log.d(TAG, pendingIntent)
                        pendingIntent.send();
                    }
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        };

        public PendingIntentPool() {
            pendingIntentList = new ArrayList<PendingIntent>();
            lock = new ReentrantLock();
            existBunchTask = false;
            handler = new Handler();
        }

        public void release() {
            Log.d(TAG, "Acquire lock from release");
            lock.lock();
            if (existBunchTask) {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 0);
            }
            Log.d(TAG, "Release lock from release");
            lock.unlock();}

        public void add(final PendingIntent pendingIntent, int delay) {
            Log.d(TAG, "Acquire lock from add");
            lock.lock();
            pendingIntentList.add(pendingIntent);
            if (!existBunchTask) {
                Log.d(TAG, "No timer, set a timer for " + delay + " seconds.");
                // set a timer for bunch tasks
                handler.postDelayed(runnable, delay);
                existBunchTask = true;
            }
            Log.d(TAG, "Release lock from add");
            lock.unlock();
        }

        public void add(final PendingIntent pendingIntent) {
            this.add(pendingIntent, delayedTime);
        }

        public boolean setDelayedTime(int delay) {
            this.delayedTime = delay;
            return true;
        }
    }

    private HashMap<String, PendingIntentPool> poolMap;
    private NotificationCompat.Builder notificationBuilder;
    private static final int notificationId = 1;
    public static final String TAG = "DelayedTaskService";

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
        poolMap = new HashMap<String, PendingIntentPool>();
        foreground();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground();
    }

    private void foreground() {
        notificationBuilder = new NotificationCompat.
                Builder(getBaseContext())
                .setContentTitle("Delayed Task Service")
                .setSmallIcon(R.drawable.ic_launcher);
        startForeground(notificationId, notificationBuilder.build());
        return;
    }

    private void stopForeground() {
        stopForeground(true);
    }

    public void setDelayedTask(PendingIntent pendingIntent, int delay) {
        String packageName = pendingIntent.getCreatorPackage();
        int uid = pendingIntent.getCreatorUid();
        Toast.makeText(getBaseContext(), packageName + "\t" + uid, Toast.LENGTH_SHORT).show();
        PendingIntentPool pool = getPendingIntentPool(packageName);
        pool.add(pendingIntent, delay);
    }

    public void addDelayedTask(PendingIntent pendingIntent) {
        String packageName = pendingIntent.getCreatorPackage();
        int uid = pendingIntent.getCreatorUid();
        Toast.makeText(getBaseContext(), packageName + "\t" + uid, Toast.LENGTH_SHORT).show();
        PendingIntentPool pool = getPendingIntentPool(packageName);
        pool.add(pendingIntent);
    }


    public boolean setMaximumDelayTime(Context context, int delay) {
        String packageName = context.getPackageName();
        PendingIntentPool pool = getPendingIntentPool(packageName);
        pool.setDelayedTime(delay);
        return true;
    }

    public void releaseAll() {
        for (String packageName : poolMap.keySet()) {
            PendingIntentPool pool = poolMap.get(packageName);
            Log.d(TAG, "Release " + packageName);
            pool.release();
        }
    }

    public PendingIntentPool getPendingIntentPool(String packageName) {
        PendingIntentPool pool = poolMap.get(packageName);
        if (pool == null) {
            pool = new PendingIntentPool();
            poolMap.put(packageName, pool);
        }
        return pool;
    }
}
