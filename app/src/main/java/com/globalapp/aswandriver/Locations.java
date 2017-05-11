package com.globalapp.aswandriver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.java.core.KinveyClientCallback;

import java.util.Arrays;

public class Locations extends Service implements LocationListener {
    SharedPreferences sharedPreferences;
    public static boolean IS_RUNNING = false;

    @Override
    public void onCreate() {
        super.onCreate();
        // Location sensor
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        sharedPreferences = getSharedPreferences("TaxiSharedDriver", Context.MODE_PRIVATE);
        Toast.makeText(this, "Started", Toast.LENGTH_SHORT).show();
        IS_RUNNING = true;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
// TODO: 1/4/2017 Hnadle stopping service
        Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show();
        IS_RUNNING = false;
    }

    @Override
    public void onLocationChanged(final android.location.Location location) {
        Thread thread = new Thread() {
            public void run() {
                try {
                    Client mKinveyClient = new Client.Builder(getApplicationContext()).build();
                    GenericJson appdata = new GenericJson();
                    appdata.put("_id", mKinveyClient.user().getId());
                    appdata.put("long", location.getLongitude());
                    appdata.put("lat", location.getLatitude());
                    appdata.put("state", sharedPreferences.getString("state", "offline"));
                    appdata.put("_geoloc", Arrays.asList(location.getLongitude(), location.getLatitude()));
                    AsyncAppData<GenericJson> mylocation = mKinveyClient.appData("locations", GenericJson.class);


                    mylocation.save(appdata, new KinveyClientCallback<GenericJson>() {
                        @Override
                        public void onSuccess(GenericJson genericJson) {

                        }

                        @Override
                        public void onFailure(Throwable throwable) {

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        };
        thread.start();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
