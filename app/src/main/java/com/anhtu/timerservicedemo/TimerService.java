package com.anhtu.timerservicedemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import static com.anhtu.timerservicedemo.TimerServiceApplication.CHANNEL_ID;

public class TimerService extends Service {

    private static final String LOG_TAG = TimerService.class.getSimpleName();

    public static final String ACTION_TIMER_STATE ="com.anhtu.timerservicedemo.action.ACTION_TIMER_STATE";

    private final IBinder binder = new LocalTimeBinder();

    public class LocalTimeBinder extends Binder {

        public TimerService getServiceInstance() {
            return TimerService.this;
        }

    }

    private long currentElapsedTime;

    private NotificationManager notificationManager;

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOG_TAG,"onBind");
        return this.binder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(LOG_TAG, "onRebind");
        super.onRebind(intent);
        // Tạo một đối tượng Intent (Một đối tượng phát sóng).
        Intent broadcastIntent = new Intent();

        // Sét tên hành động (Action) của Intent này.
        // Một Intent có thể thực hiện nhiều hành động khác nhau.
        // (Có thể coi là nhiều nhiệm vụ).
        broadcastIntent.setAction(TimerService.ACTION_TIMER_STATE);
        broadcastIntent.putExtra("current_time", (int) currentElapsedTime);

        // Send broadcast
        // Phát sóng gửi đi.
        sendBroadcast(broadcastIntent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(LOG_TAG, "onUnbind");
        return true;
    }

    private Notification notification;

    private NotificationCompat.Builder notificationBuilder;

    private CountUpTimer mCountUpTimer;

    public TimerService() {
    }

    /*@Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }*/

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "onCreate");

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mCountUpTimer = new CountUpTimer(1000) {

            @Override
            public void onTick(long elapsedTime) {

                Log.i(LOG_TAG, "current time: " + elapsedTime);

                currentElapsedTime = elapsedTime;

                // Tạo một đối tượng Intent (Một đối tượng phát sóng).
                Intent broadcastIntent = new Intent();

                // Sét tên hành động (Action) của Intent này.
                // Một Intent có thể thực hiện nhiều hành động khác nhau.
                // (Có thể coi là nhiều nhiệm vụ).
                broadcastIntent.setAction(TimerService.ACTION_TIMER_STATE);
                broadcastIntent.putExtra("current_time", (int) elapsedTime);

                notificationBuilder
                        .setContentText(CountUpTimer.displayTime(elapsedTime/10));

                notificationManager.notify(1, notificationBuilder.build());

                // Send broadcast
                // Phát sóng gửi đi.
                sendBroadcast(broadcastIntent);
            }
        };


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "onStartCommand");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Timer Service")
                .setContentText("")
                .setSmallIcon(R.drawable.ic_child_friendly_black_24dp)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                ;

        notification = notificationBuilder.build();

        startForeground(1, notification);
        return START_NOT_STICKY;

    }


    public void startTimer() {
        mCountUpTimer.start();
    }

    public void pauseTimer() {
        mCountUpTimer.pause();
    }

    public void resumeTimer() {
        mCountUpTimer.resume();
    }

    public void stopTimer() {
        mCountUpTimer.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
    }
}
