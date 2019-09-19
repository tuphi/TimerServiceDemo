package com.anhtu.timerservicedemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private TextView tvTimer;

    private ResponseReceiver receiver = new ResponseReceiver();

    private TimerService mTimerService;

    private LocationService mLocationService;

    private boolean binded = false;

    private boolean locationServiceBinded = false;

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

            String name = componentName.getClassName();

            if (name.endsWith("TimerService")) {
                TimerService.LocalTimeBinder binder = (TimerService.LocalTimeBinder) iBinder;
                mTimerService = binder.getServiceInstance();
                binded = true;
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            String name = componentName.getClassName();

            if (name.endsWith("TimerService")) {
                mTimerService = null;
                binded = false;
            }

        }
    };

    ServiceConnection locationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            String name = componentName.getClassName();

            if (name.endsWith("LocationService")) {
                LocationService.LocationServiceBinder binder = (LocationService.LocationServiceBinder) iBinder;
                mLocationService = binder.getServiceInstance();
                locationServiceBinded = true;
                mLocationService.startUpdatingLocation();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            String name = componentName.getClassName();

            if (name.endsWith("LocationService")) {
                mLocationService = null;
                locationServiceBinded = false;
            }

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        tvTimer = findViewById(R.id.tv_timer);
        checkLocationPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, TimerService.class);
        this.bindService(intent, timerServiceConnection, Context.BIND_AUTO_CREATE);
        binded = true;

        Intent locationServiceIntent = new Intent(this, LocationService.class);
        this.bindService(locationServiceIntent, locationServiceConnection, Context.BIND_AUTO_CREATE);
        locationServiceBinded = true;

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

        if(locationServiceBinded) {
            //Hủy ràng buộc kết nối với dịch vụ
            this.unbindService(locationServiceConnection);
            locationServiceBinded = false;
        }
    }

    public void onClickStartService(View view) {
        Intent serviceIntent = new Intent(this, TimerService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
        Intent intent = new Intent(this, TimerService.class);
        this.bindService(intent, timerServiceConnection, Context.BIND_AUTO_CREATE);
        binded = true;
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
        this.mTimerService.stopTimer();
        if(binded) {
            //Hủy ràng buộc kết nối với dịch vụ
            this.unbindService(timerServiceConnection);
            binded = false;
        }
    }

    public void onClickStartLocationService(View view) {
        Intent serviceIntent = new Intent(this, LocationService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
        /*Intent intent = new Intent(this, TimerService.class);
        this.bindService(intent, timerServiceConnection, Context.BIND_AUTO_CREATE);*/
        this.bindService(serviceIntent, locationServiceConnection, Context.BIND_AUTO_CREATE);
        locationServiceBinded = true;
    }

    public void onClickStopLocationService(View view) {
        Intent serviceIntent = new Intent(this, LocationService.class);
        stopService(serviceIntent);
        if(locationServiceBinded) {
            //Hủy ràng buộc kết nối với dịch vụ
            this.unbindService(locationServiceConnection);
            locationServiceBinded = false;
        }
    }


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(/*R.string.title_location_permission*/"Location")
                        .setMessage(/*R.string.text_location_permission*/"Allow the app to access location!")
                        .setPositiveButton(/*R.string.ok*/"OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        mLocationService.startUpdatingLocation();
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }

}
