package com.globalapp.futuretaxidriver;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.api.client.json.GenericJson;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.java.cache.CachePolicy;
import com.kinvey.java.cache.InMemoryLRUCache;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.KinveyDeleteResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, LocationListener {
    final int MY_PERMISSIONS_REQUEST = 123;
    static TextView txtTotalDistance, txtMovingTime, txtStoppageTime, txtSpeed, txtTotalMoney, txtTotalTime;
    SharedPreferences sharedPreferences;
    private GoogleMap mMap;
    private Location GPS;
    AQuery aQuery;
    Boolean toggleCounter = false, toggleMain = false;
    static FloatingActionButton fabMain, fabCounter;
    Client mKinveyClient;
    static String CustomerName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("TaxiSharedDriver", Context.MODE_PRIVATE);

        String languageToLoad = sharedPreferences.getString("language", "en");
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_map);


        initViews();


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_MyTrip:
                Intent history = new Intent(getApplicationContext(), HistoryActivity.class);
                startActivity(history);
                break;
            case R.id.nav_ContactUS:
                Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                phoneIntent.setData(Uri.parse("tel:01201513513"));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Change call number

                    return true;
                }
                startActivity(phoneIntent);

                break;
            case R.id.nav_about:
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage(getString(R.string.about_message))
                        .setTitle(getString(R.string.about))
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
            case R.id.nav_setting:
                Intent setting = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(setting);
                break;
            case R.id.nav_current:
                if (sharedPreferences.getString("CustomerName", "").isEmpty()) {
                    Toast.makeText(this, getString(R.string.no_cuurent), Toast.LENGTH_SHORT).show();
                } else {
                    TripActivity.CustomerName = sharedPreferences.getString("CustomerName", "");
                    MapActivity.CustomerName = sharedPreferences.getString("CustomerName", "");
                    TripActivity.CustomerDes = sharedPreferences.getString("CustomerDes", "");
                    TripActivity.ID = sharedPreferences.getString("ID", "");
                    TripActivity.CustomerGeo = sharedPreferences.getString("CustomerGeo", "");
                    Intent current = new Intent(getApplicationContext(), TripActivity.class);
                    startActivity(current);
                }
                break;


        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.Map);
        mapFragment.getMapAsync(this);
        aQuery = new AQuery(this);
        txtSpeed = (TextView) findViewById(R.id.txt_Speed_fees);
        txtTotalDistance = (TextView) findViewById(R.id.txt_Total_Distance);
        txtTotalMoney = (TextView) findViewById(R.id.txt_Total_Money_Fees);
        txtTotalTime = (TextView) findViewById(R.id.txt_Total_Time);
        txtStoppageTime = (TextView) findViewById(R.id.txt_Stopping_Time);
        txtMovingTime = (TextView) findViewById(R.id.txt_Moving_Time);
        fabMain = (FloatingActionButton) findViewById(R.id.fabMain);
        fabCounter = (FloatingActionButton) findViewById(R.id.fabCounter);
        mKinveyClient = new Client.Builder(getApplicationContext()).build();
        mKinveyClient.push().initialize(getApplication(), new KinveyClientCallback() {
            @Override
            public void onSuccess(Object o) {

            }

            @Override
            public void onFailure(Throwable throwable) {
                Toast.makeText(MapActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.0f, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        View hView = navigationView.getHeaderView(0);
        TextView txtUserNameHeader = (TextView) hView.findViewById(R.id.txtUserNameHeader);
        TextView txtMailHeader = (TextView) hView.findViewById(R.id.txtMailHeader);
        txtUserNameHeader.setText(sharedPreferences.getString("full_Name", "Full Name"));
        txtMailHeader.setText(sharedPreferences.getString("carNo", "carNo"));
        String imageURL = sharedPreferences.getString("imageURL", "");
        if (!imageURL.equals("")) {
            aQuery.id(hView.findViewById(R.id.profile_image)).image(imageURL, true, true);

        }
        if (Locations.IS_RUNNING) {
            startLocationService();
        } else {
            stopLocationService();
        }
        if (FeesCalculation.IS_SERVICE_RUNNING) {
            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
            fabCounter.startAnimation(anim);

        } else {
            fabCounter.clearAnimation();
        }
// TODO: 11/1/2017 create counter every 10 min to ask driver if he is online or not  
//        CountDownTimer countDownTimer = new CountDownTimer(1000*60*10,1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//              
//            }
//
//            @Override
//            public void onFinish() {
//
//            }
//        };
//        countDownTimer.start();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {


                } else {


                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
                                    , Manifest.permission.CALL_PHONE},
                            MY_PERMISSIONS_REQUEST);

                }
                return;
            }


        }
        LocationManager Locationmanager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        GPS = Locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (GPS == null) {
            GPS = Locationmanager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }

        try {
            mMap.setMyLocationEnabled(true);
            LatLng center = new LatLng(GPS.getLatitude(), GPS.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 15));
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                mMap.setTrafficEnabled(true);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show();

                } else {

                    Toast.makeText(this, "Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void toggleCounter(View view) {
        if (!toggleCounter) {
            showCounter();
        } else {
            closeCounter();
        }

    }

    private void showCounter() {
        RelativeLayout counter = (RelativeLayout) findViewById(R.id.fees);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.dialog_in);
        counter.startAnimation(animation);
        toggleCounter = true;


    }

    private void closeCounter() {
        RelativeLayout counter = (RelativeLayout) findViewById(R.id.fees);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.dialog_out);
        counter.startAnimation(animation);
        toggleCounter = false;
    }

    public void toggleMain(View view) {
        if (toggleMain) {
            stopLocationService();
        } else {
            startLocationService();
        }
    }

    private void startLocationService() {
        startService(new Intent(getApplicationContext(), Locations.class));
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
        fabMain.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
        fabMain.startAnimation(animation);
        toggleMain = true;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("state", "online");
        editor.apply();
    }

    private void stopLocationService() {
        stopService(new Intent(getApplicationContext(), Locations.class));
        fabMain.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        fabMain.clearAnimation();
        toggleMain = false;
        Client mKinveyClient = new Client.Builder(getApplicationContext()).build();
        AsyncAppData<GenericJson> mylocation = mKinveyClient.appData("locations", GenericJson.class);
        mylocation.delete(mKinveyClient.user().getId(), new KinveyDeleteCallback() {
            @Override
            public void onSuccess(KinveyDeleteResponse kinveyDeleteResponse) {
                Toast.makeText(MapActivity.this, "Stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        });


    }

  static   boolean onTrip = false;

    public void finishTrip(View view) {
        final Button btn = (Button) findViewById(R.id.btnFinishTrip);
        if (onTrip) {
            if (Double.valueOf(txtTotalMoney.getText().toString()) < 6.0) {

                return;
            }

            final String date = new SimpleDateFormat("EEE, d MMM yy, hh:mm aaa", Locale.US).format(new Date());
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(getString(R.string.finish_trip_message))
                    .setTitle(getString(R.string.finish))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            stopService(new Intent(getApplicationContext(), FeesCalculation.class));
                            final ProgressDialog loading = new ProgressDialog(getApplicationContext());
                            loading.setTitle(getString(R.string.trip_finish));
                            loading.setMessage(getString(R.string.please_wait));
                            loading.show();
                            responses("Trip is Finished");

                            GenericJson myInput = new GenericJson();
                            myInput.put("UserName", sharedPreferences.getString("UserName", ""));
                            myInput.put("Money", txtTotalMoney.getText().toString());
                            myInput.put("Time", date);
                            myInput.put("customer_phone", CustomerName);
                            myInput.put("Distance", txtTotalDistance.getText().toString());

                            AsyncAppData<GenericJson> myPayments = mKinveyClient.appData("Payments", GenericJson.class);
                            myPayments.setCache(new InMemoryLRUCache(), CachePolicy.NETWORKFIRST);
                            myPayments.save(myInput, new KinveyClientCallback<GenericJson>() {
                                @Override
                                public void onSuccess(GenericJson genericJson) {

                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.remove("CustomerName");
                                    editor.remove("CustomerDes");
                                    editor.remove("CustomerGeo");
                                    editor.remove("ID");
                                    editor.apply();
                                    loading.dismiss();
                                    btn.setText(getString(R.string.new_trip));
                                    onTrip = true;
                                    Toast.makeText(getApplicationContext(), getString(R.string.data_saved), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Throwable throwable) {

                                }
                            });
                        }
                    })
                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();

        } else {
            startLocationService();
            onTrip = false;
            btn.setText(getString(R.string.finish));
        }
    }

    public void responses(String message) {

        GenericJson myInput = new GenericJson();
        myInput.put("msg", message);
        myInput.put("customer_phone", CustomerName);
        mKinveyClient.customEndpoints(GenericJson.class).callEndpoint("Trip", myInput, new KinveyClientCallback() {

            @Override
            public void onSuccess(Object o) {
                Toast.makeText(getApplicationContext(), "Succeeded", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        });

    }

    public void userLogout(View view) {
        mKinveyClient.user().logout().execute();
        Intent user = new Intent(getApplicationContext(), UserActivity.class);
        stopLocationService();
        startActivity(user);

    }

    public void callUsers(View view) {
        GenericJson myInput = new GenericJson();
        myInput.put("lat", GPS.getLatitude());
        myInput.put("long", GPS.getLongitude());
        mKinveyClient.customEndpoints(GenericJson.class).callEndpoint("callUsers", myInput, new KinveyClientCallback() {

            @Override
            public void onSuccess(Object o) {
                Toast.makeText(getApplicationContext(), "Succeeded", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Throwable throwable) {
                Toast.makeText(MapActivity.this, getString(R.string.user_around), Toast.LENGTH_SHORT).show();

            }
        });


    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng center = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15));
        mMap.setTrafficEnabled(true);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
