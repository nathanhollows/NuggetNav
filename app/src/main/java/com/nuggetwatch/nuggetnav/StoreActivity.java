package com.nuggetwatch.nuggetnav;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.geojson.Feature;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StoreActivity extends AppCompatActivity implements PriceAdapter.ItemClickListener {

    private JSONObject json = null;
    private PriceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        TextView name = findViewById(R.id.name);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        try {
            json = new JSONObject(getIntent().getStringExtra("json"));
            name.setText(json.getJSONObject("properties").getString("chain"));
            getSupportActionBar().setTitle(json.getJSONObject("properties").getString("chain"));
        } catch (Throwable tx) {
            Log.e("JSON:: ", "Could not parse malformed JSON: " + getIntent().getStringExtra("json"));
        }

        getPrices();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.store_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_directions) {
            Intent intent = new Intent(StoreActivity.this, CompassActivity.class);
            try {
                intent.putExtra("latitude", json.getJSONObject("geometry").getJSONArray("coordinates").get(1).toString());
                intent.putExtra("longitude", json.getJSONObject("geometry").getJSONArray("coordinates").get(0).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }

    public void getPrices() {

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofitInstance = new Retrofit
                .Builder()
                .baseUrl("https://nuggetwatch.co.nz/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        API apiService = retrofitInstance.create(API.class);

        Call<List<PriceModel>> apiCall = null;
        try {
            apiCall = apiService.prices(json.getJSONObject("properties").getString("nicename"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        apiCall.enqueue(new Callback<List<PriceModel>>() {
            @Override
            public void onResponse(Call<List<PriceModel>> call, Response<List<PriceModel>> response) {
                // set up the RecyclerView
                RecyclerView recyclerView = findViewById(R.id.recyclerView);
                LinearLayoutManager layoutManager = new LinearLayoutManager(StoreActivity.this);
                recyclerView.setLayoutManager(layoutManager);
                adapter = new PriceAdapter(StoreActivity.this, response.body());
                adapter.setClickListener(StoreActivity.this);
                recyclerView.setAdapter(adapter);

                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                        layoutManager.getOrientation());
                recyclerView.addItemDecoration(dividerItemDecoration);
            }

            @Override
            public void onFailure(Call<List<PriceModel>> call, Throwable t) {
                // Todo: Let the user know it failed
            }
        });
    }
}
