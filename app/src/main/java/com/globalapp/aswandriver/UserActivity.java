package com.globalapp.aswandriver;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.FileMetaData;

import java.util.Locale;

public class UserActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    Client mKinveyClient;

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
        setContentView(R.layout.activity_user);
    }

    public void btn_Login(View view) {

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle(getString(R.string.logging_in));
        dialog.setMessage(getString(R.string.please_wait));
        dialog.show();

        final EditText txtEmail = (EditText) findViewById(R.id.txtID);
        final EditText txtPassword = (EditText) findViewById(R.id.txtPassword);
        mKinveyClient = new Client.Builder(this.getApplicationContext()).build();
        final Intent myintet = new Intent(this, MapActivity.class);
        mKinveyClient.user().logout().execute();

        mKinveyClient.user().login(txtEmail.getText().toString(), txtPassword.getText().toString(), new KinveyUserCallback() {


            @Override
            public void onSuccess(User user) {

                Toast.makeText(getApplicationContext(), getString(R.string.welcome) + " " + user.get("full_Name").toString(), Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("UserName", user.getUsername());
                editor.putString("full_Name", user.get("full_Name").toString());
                editor.putString("PhoneNumber", user.get("Phone_Number").toString());
                editor.putString("carColor", user.get("car_color").toString());
                editor.putString("carModel", user.get("car_model").toString());
                editor.putString("carNo", user.get("car").toString());
                editor.apply();
                dialog.dismiss();
                getUserImage();
                startActivity(myintet);
                finish();

            }

            @Override
            public void onFailure(Throwable throwable) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void getUserImage() {

        mKinveyClient.file().downloadMetaData("avr-" + mKinveyClient.user().getId(), new KinveyClientCallback<FileMetaData>() {
            @Override
            public void onSuccess(FileMetaData fileMetaData) {


                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("imageURL", fileMetaData.getDownloadURL());
                editor.apply();
            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        });

    }
}
