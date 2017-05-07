package com.globalapp.aswandriver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class SettingActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;

    TextView txtLan;
    String[] lan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("TaxiSharedDriver", Context.MODE_PRIVATE);

        String languageToLoad = sharedPreferences.getString("language", "en"); // your language
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_setting);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lan = new String[]{getString(R.string.english), getString(R.string.arabic)};

        txtLan = (TextView) findViewById(R.id.txtLanguage);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();

        txtLan.setText(lan[sharedPreferences.getInt("lanCaption", 0)]);

    }

    public void setLanguage(View view) {

        final String[] loca = {"en", "ar"};
        AlertDialog.Builder Dsetting = new AlertDialog.Builder(this);
        Dsetting.setTitle(getString(R.string.choose_lan))
                .setItems(lan, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        txtLan.setText(lan[i]);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("language", loca[i]);
                        editor.putInt("lanCaption", i);
                        editor.apply();
                        Toast.makeText(SettingActivity.this, getString(R.string.restart), Toast.LENGTH_SHORT).show();

                    }
                })
                .show();
    }

}
