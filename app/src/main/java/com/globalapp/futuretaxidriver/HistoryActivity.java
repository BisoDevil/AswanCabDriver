package com.globalapp.futuretaxidriver;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.offline.SqlLiteOfflineStore;
import com.kinvey.java.Query;
import com.kinvey.java.cache.CachePolicy;
import com.kinvey.java.cache.InMemoryLRUCache;
import com.kinvey.java.offline.OfflinePolicy;
import com.kinvey.java.query.AbstractQuery;

import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;

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
        setContentView(R.layout.activity_history);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Client mKinveyClient = new Client.Builder(this.getApplicationContext()).build();
        Query query = mKinveyClient.query();

        query.equals("UserName", sharedPreferences.getString("UserName", ""));
        query.addSort("UserName", AbstractQuery.SortOrder.DESC);
        AsyncAppData<GenericJson> payments = mKinveyClient.appData("Payments", GenericJson.class);
        payments.setCache(new InMemoryLRUCache(), CachePolicy.NETWORKFIRST);
        payments.setOffline(OfflinePolicy.ONLINE_FIRST, new SqlLiteOfflineStore<GenericJson>(getApplicationContext()));
        payments.get(query, new KinveyListCallback<GenericJson>() {
            @Override
            public void onSuccess(GenericJson[] genericJsons) {
                SetupListView(genericJsons);
            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void SetupListView(final GenericJson[] genericJsons) {

        ListView list = (ListView) findViewById(R.id.listHistory);
        list.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return genericJsons.length;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater linflater = getLayoutInflater();
                View view1 = linflater.inflate(R.layout.history_row, null);

                TextView tripDistance = (TextView) view1.findViewById(R.id.txtRowDistance);
                TextView tripMoney = (TextView) view1.findViewById(R.id.txtRowMoney);
                TextView tripTime = (TextView) view1.findViewById(R.id.txtRowTime);

                tripMoney.setText(genericJsons[position].get("Money").toString());
                tripTime.setText(genericJsons[position].get("Time").toString());
                tripDistance.setText(genericJsons[position].get("Distance").toString());


                return view1;
            }
        });


    }
}
