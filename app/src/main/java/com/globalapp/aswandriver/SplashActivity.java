package com.globalapp.aswandriver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

import com.kinvey.android.Client;

public class SplashActivity extends Activity {
    boolean statusOfGPS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        int SPLASH_DISPLAY_LENGTH = 3000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (statusOfGPS) {
                    justGo();
                } else {
                    Intent intent = new Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    private void justGo() {


        final Client mKinveyClient = new Client.Builder(getApplicationContext()).build();

        if (mKinveyClient.user().isUserLoggedIn()) {
            Intent mainIntent = new Intent(SplashActivity.this, MapActivity.class);
            SplashActivity.this.startActivity(mainIntent);
            SplashActivity.this.finish();
        } else {
            Intent mainIntent = new Intent(SplashActivity.this, UserActivity.class);
            SplashActivity.this.startActivity(mainIntent);
            SplashActivity.this.finish();
        }
    }


}
