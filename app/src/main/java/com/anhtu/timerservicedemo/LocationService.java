package com.anhtu.timerservicedemo;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import static android.content.ContentValues.TAG;
import static com.anhtu.timerservicedemo.TimerServiceApplication.CHANNEL_ID;

public class LocationService extends Service implements LocationListener, GpsStatus.Listener  {

    private static final String LOG_TAG = LocationService.class.getSimpleName();

    private final LocationServiceBinder binder = new LocationServiceBinder();

    private Notification notification;

    private LocationManager locationManager;

    private boolean isLocationManagerUpdatingLocation;

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocationServiceBinder extends Binder {
        public LocationService getServiceInstance() {
            return LocationService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "onStartCommand");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service")
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

    @Override
    public void onRebind(Intent intent) {
        Log.d(LOG_TAG, "onRebind ");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "onUnbind ");

        return true;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy ");


    }

    public void startUpdatingLocation() {

        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setPowerRequirement(Criteria.POWER_HIGH);
            criteria.setAltitudeRequired(false);
            criteria.setSpeedRequired(false);
            criteria.setCostAllowed(true);
            criteria.setBearingRequired(false);

            //API level 9 and up
            criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
            criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

            Integer gpsFreqInMillis = 1000;
            Integer gpsFreqInDistance = 1;  // in meters

            locationManager.addGpsStatusListener(this);

            locationManager.requestLocationUpdates(gpsFreqInMillis, gpsFreqInDistance, criteria, this, null);

        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, e.getLocalizedMessage());
        } catch (SecurityException e) {
            Log.e(LOG_TAG, e.getLocalizedMessage());
        } catch (RuntimeException e) {
            Log.e(LOG_TAG, e.getLocalizedMessage());
        }
    }

    //This is where we detect the app is being killed, thus stop service.
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(LOG_TAG, "onTaskRemoved ");

        if(this.isLocationManagerUpdatingLocation == true){
            this.stopUpdatingLocation();
            isLocationManagerUpdatingLocation = false;
        }

        stopSelf();
    }


    public void stopUpdatingLocation(){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.removeUpdates(this);
    }

    @Override
    public void onGpsStatusChanged(int i) {

    }

    @Override
    public void onLocationChanged(final Location newLocation) {
        Log.d(LOG_TAG, "(" + newLocation.getLatitude() + "," + newLocation.getLongitude() + ")");
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        if (s.equals(LocationManager.GPS_PROVIDER)) {
            if (i == LocationProvider.OUT_OF_SERVICE) {
                notifyLocationProviderStatusUpdated(false);
            } else {
                notifyLocationProviderStatusUpdated(true);
            }
        }
    }

    private void notifyLocationProviderStatusUpdated(boolean isLocationProviderAvailable) {
        //Broadcast location provider status change here
    }

    @Override
    public void onProviderEnabled(String s) {
        if (s.equals(LocationManager.GPS_PROVIDER)) {
            notifyLocationProviderStatusUpdated(true);
        }
    }

    @Override
    public void onProviderDisabled(String s) {
        if (s.equals(LocationManager.GPS_PROVIDER)) {
            notifyLocationProviderStatusUpdated(false);
        }
    }


    public LocationManager getLocationManager() {
        return locationManager;
    }
}
