package com.globalapp.futuretaxidriver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.KinveyDeleteResponse;

import java.util.Locale;

public class TripActivity extends AppCompatActivity {

    static String CustomerName = "", CustomerDes = "", ID = "", CustomerGeo = "";
    TextView txtCustomerName, txtCustomerDes, txtCoutDown;
    SharedPreferences sharedPreferences;
    FloatingActionButton fabYes, fabNo;
    int secs = 0, mins = 0;
    boolean is_pressed = false;
    MediaPlayer mp;

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
        setContentView(R.layout.activity_trip);
        txtCustomerDes = (TextView) findViewById(R.id.txtCustomerDes);
        txtCustomerName = (TextView) findViewById(R.id.txtCustomerName);

        fabYes = (FloatingActionButton) findViewById(R.id.fabAcceptOrder);
        fabNo = (FloatingActionButton) findViewById(R.id.fabCancelOrder);
        txtCoutDown = (TextView) findViewById(R.id.txtCountDown);
        mp = MediaPlayer.create(this, R.raw.heytaxi);

        mp.start();

    }

    @Override
    protected void onStart() {
        super.onStart();
        txtCustomerName.setText(CustomerName);
        txtCustomerDes.setText(CustomerDes);
    }

    @Override
    public void onBackPressed() {

    }

    public void startTrip(View view) {
        response("Start Trip");
        startService(new Intent(this, FeesCalculation.class));
        Client mKinveyClient = new Client.Builder(getApplicationContext()).build();
        AsyncAppData<GenericJson> mylocation = mKinveyClient.appData("locations", GenericJson.class);
        mylocation.delete(mKinveyClient.user().getId(), new KinveyDeleteCallback() {
            @Override
            public void onSuccess(KinveyDeleteResponse kinveyDeleteResponse) {
                Toast.makeText(TripActivity.this, "Stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        });
        finish();
    }

    public void cancel(View view) {
        if (!is_pressed) {
            finish();
        } else {
            response("Sorry, I have to go ");
        }
    }

    public void answer(View view) {
        if (!is_pressed) {
            acceptOrder("I'm on my way");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("state");
            editor.putString("state","offline");
            editor.apply();
            MapActivity.onTrip = true;


        } else {
            response("I arrived");
            Button btn = (Button) findViewById(R.id.btnStratTrip);
            btn.setVisibility(View.VISIBLE);
            txtCoutDown.setVisibility(View.VISIBLE);
            CountDownTimer timer = new CountDownTimer(300000, 1000) {
                @Override
                public void onTick(long l) {
                    secs = (int) (l / 1000);
                    mins = secs / 60;
                    mins = mins % 60;
                    secs = secs % 60;

                    String time = String.format(Locale.US, "%02d", mins) + ":" + String.format(Locale.US, "%02d", secs);
                    txtCoutDown.setText(time);

                }

                @Override
                public void onFinish() {
                    txtCoutDown.setVisibility(View.INVISIBLE);
                    fabNo.setEnabled(true);

                }
            };
            timer.start();
        }

    }


    public void acceptOrder(String message) {

        Client mKinveyClient = new Client.Builder(getApplicationContext()).build();
        GenericJson myInput = new GenericJson();
        myInput.put("msg", message);
        myInput.put("id", ID);
        myInput.put("customer_phone", CustomerName);
        myInput.put("carNo", sharedPreferences.getString("carNo", ""));
        myInput.put("driver_name", sharedPreferences.getString("full_Name", ""));
        myInput.put("driver_phone", sharedPreferences.getString("PhoneNumber", ""));
        myInput.put("carModel", sharedPreferences.getString("carModel", ""));
        myInput.put("carColor", sharedPreferences.getString("carColor", ""));
        mKinveyClient.customEndpoints(GenericJson.class).callEndpoint("Driver", myInput, new KinveyClientCallback() {


            @Override
            public void onSuccess(Object o) {
                Toast.makeText(TripActivity.this, "Succeeded", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("state");
                editor.putString("state","offline");
                editor.apply();
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + CustomerGeo));
                startActivity(i);
                showNextStep();
            }

            @Override
            public void onFailure(Throwable throwable) {

                showDialog(getString(R.string.failed_order));
            }
        });


    }

    private void showNextStep() {
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.dialog_in);

        fabYes.setImageResource(R.drawable.ic_thumb);

        fabNo.setImageResource(R.drawable.ic_go);
        fabYes.startAnimation(animation);
        fabNo.startAnimation(animation);

        fabNo.setEnabled(false);

        is_pressed = true;


    }

    private void response(String message) {
        Client mKinveyClient = new Client.Builder(getApplicationContext()).build();
        GenericJson myInput = new GenericJson();
        myInput.put("msg", message);
        myInput.put("id", ID);
        myInput.put("customer_phone", CustomerName);
        mKinveyClient.customEndpoints(GenericJson.class).callEndpoint("Trip", myInput, new KinveyClientCallback() {

            @Override
            public void onSuccess(Object o) {
                Toast.makeText(TripActivity.this, "Succeeded", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        });


    }

    public void callUser(View view) {
        Intent phoneIntent = new Intent(Intent.ACTION_CALL);
        phoneIntent.setData(Uri.parse("tel:" + txtCustomerName.getText().toString()));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        startActivity(phoneIntent);
    }

    private void showDialog(String text) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(text)
                .setTitle(getString(R.string.app_name))
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .show();
    }
}
