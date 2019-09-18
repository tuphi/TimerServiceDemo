package com.anhtu.timerservicedemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private TextView tvTimer;

    private ResponseReceiver receiver = new ResponseReceiver();

    private TimerService mTimerService;

    private boolean binded = false;

    private class ResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Kiểm tra nhiệm vụ của Intent gửi đến.
            if(intent.getAction().equals(TimerService.ACTION_TIMER_STATE)) {
                int value = intent.getIntExtra("current_time", -1);
                Log.i(LOG_TAG, String.valueOf(value));

                new ShowTimeTask().execute(value);
            }
        }
    }

    ServiceConnection timerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            TimerService.LocalTimeBinder binder = (TimerService.LocalTimeBinder) iBinder;
            mTimerService = binder.getServiceInstance();
            binded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            binded = false;
        }
    };

    // Class làm nhiệm vụ hiển thị giá trị cho ProgressBar.
    private class ShowTimeTask extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected Integer doInBackground(Integer... args) {

            return args[0];
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            tvTimer.setText(result + " s");

        }
    }

    private class TimerStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        tvTimer = findViewById(R.id.tv_timer);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, TimerService.class);
        this.bindService(intent, timerServiceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Đăng ký bộ thu sóng với Activity.
        registerReceiver(receiver, new IntentFilter(
                TimerService.ACTION_TIMER_STATE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Hủy đăng ký bộ thu sóng với Activity.
        unregisterReceiver(receiver);

        if(binded) {
            //Hủy ràng buộc kết nối với dịch vụ
            this.unbindService(timerServiceConnection);
            binded = false;
        }
    }

    public void onClickStartService(View view) {
        Intent serviceIntent = new Intent(this, TimerService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void onClickStart(View view) {
        this.mTimerService.startTimer();
    }

    public void onClickPause(View view) {
        this.mTimerService.pauseTimer();
    }

    public void onClickResume(View view) {
        this.mTimerService.resumeTimer();
    }

    public void onClickStop(View view) {
        this.mTimerService.stopTimer();
    }

    public void onClickStopService(View view) {
        Intent serviceIntent = new Intent(this, TimerService.class);
        stopService(serviceIntent);
    }

}
