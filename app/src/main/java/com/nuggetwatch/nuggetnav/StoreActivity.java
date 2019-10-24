package com.nuggetwatch.nuggetnav;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StoreActivity extends AppCompatActivity implements PriceAdapter.ItemClickListener, ReviewAdapter.ReviewClickListener {

    private JSONObject json = null;
    private PriceAdapter priceAdapter;
    private ReviewAdapter reviewAdapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        TextView name = findViewById(R.id.name);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        try {
            json = new JSONObject(getIntent().getStringExtra("json"));
            name.setText(json.getJSONObject("properties").getString("chain"));
            getSupportActionBar().setTitle(json.getJSONObject("properties").getString("chain"));
            RatingBar overall = findViewById(R.id.rating_overall);
            overall.setRating(Integer.valueOf(json.getJSONObject("properties").getString("rating")));
        } catch (Throwable t) {
            Log.e("JSON:: ", "Could not parse malformed JSON: " + getIntent().getStringExtra("json"));
        }

        getPrices();
        getReviews();

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
        Toast.makeText(this, "You clicked " + priceAdapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReviewClick(View view, int position) {
        Toast.makeText(this, "You clicked " + reviewAdapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
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
                RecyclerView recyclerView = findViewById(R.id.priceRecycler);
                LinearLayoutManager layoutManager = new LinearLayoutManager(StoreActivity.this);
                recyclerView.setLayoutManager(layoutManager);
                priceAdapter = new PriceAdapter(StoreActivity.this, response.body());
                priceAdapter.setClickListener(StoreActivity.this);
                recyclerView.setLayoutFrozen(true);
                recyclerView.setAdapter(priceAdapter);

                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                        layoutManager.getOrientation());
                recyclerView.addItemDecoration(dividerItemDecoration);

                if (response.body().size() > 0) {
                    TableRow tableRow = findViewById(R.id.tableRow);
                    tableRow.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<PriceModel>> call, Throwable t) {
                // Todo: Let the user know it failed
            }
        });
    }

    public void getReviews() {

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofitInstance = new Retrofit
                .Builder()
                .baseUrl("https://nuggetwatch.co.nz/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        API apiService = retrofitInstance.create(API.class);

        Call<List<ReviewModel>> apiCall = null;
        try {
            apiCall = apiService.reviews(json.getJSONObject("properties").getString("nicename"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        apiCall.enqueue(new Callback<List<ReviewModel>>() {
            @Override
            public void onResponse(Call<List<ReviewModel>> call, Response<List<ReviewModel>> response) {
                Log.d("JSON:: ", response.body().toString());
                // set up the RecyclerView
                RecyclerView recyclerView = findViewById(R.id.reviewRecycler);
                LinearLayoutManager layoutManager = new LinearLayoutManager(StoreActivity.this);
                recyclerView.setLayoutManager(layoutManager);
                reviewAdapter = new ReviewAdapter(StoreActivity.this, response.body());
                reviewAdapter.setClickListener(StoreActivity.this);
                recyclerView.setAdapter(reviewAdapter);

                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                        layoutManager.getOrientation());
                recyclerView.addItemDecoration(dividerItemDecoration);

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<List<ReviewModel>> call, Throwable t) {
                Log.d("JSON:: ", t.toString());
                // Todo: Let the user know it failed
            }
        });
    }
}
