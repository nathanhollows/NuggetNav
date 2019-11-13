package com.nuggetwatch.nuggetnav;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StoreActivity extends AppCompatActivity implements ReviewAdapter.ReviewClickListener {

    private JSONObject json = null;
    private PriceAdapter priceAdapter;
    private ReviewAdapter reviewAdapter;
    private ProgressBar progressBar;
    private LinearLayout pricesLayout, reviewsLayout;
    private String name;
    private static final String MY_PREFS_NAME = "Prefs";
    private SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        TextView name = findViewById(R.id.name);
        progressBar = findViewById(R.id.progressBar);

        try {
            json = new JSONObject(getIntent().getStringExtra("json"));
             this.name = json.getJSONObject("properties").getString("chain");
            name.setText(this.name);
            getSupportActionBar().setTitle(json.getJSONObject("properties").getString("chain"));
            RatingBar overall = findViewById(R.id.rating_overall);
            overall.setRating(Integer.valueOf(json.getJSONObject("properties").getString("rating")));
        } catch (Throwable t) {
            Log.e("JSON:: ", "Could not parse malformed JSON: " + getIntent().getStringExtra("json"));
        }

        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

        FloatingActionButton fab = findViewById(R.id.write_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeReviewHandover();
            }
        });

        reviewsLayout = findViewById(R.id.reviewsLayout);
        pricesLayout = findViewById(R.id.pricesLayout);
        reviewsLayout.setVisibility(View.GONE);
        pricesLayout.setVisibility(View.GONE);

        getPrices();
        getReviews();
    }

    private void writeReviewHandover() {
        Intent intent;

        if (prefs.contains("name")){
            intent = new Intent(StoreActivity.this, ReviewActivity.class);
        } else {
            intent = new Intent(StoreActivity.this, RegisterActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("name", name);
        intent.putExtra("nicename", getIntent().getStringExtra("nicename"));
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.store_menu, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.styles.
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
            intent.putExtra("name", name);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onReviewClick(View view, int position) {
        Intent i = new Intent(this, ReadReviewActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        i.putExtra("review", reviewAdapter.getItem(position));
        i.putExtra("name", this.name);
        startActivity(i);
    }

    public void getPrices() {

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        int cacheSize = 10 * 1024 * 1024; // 10MB

        File httpCacheDirectory = new File(this.getCacheDir(), "http-cache");
        Cache cache = new Cache(httpCacheDirectory, cacheSize);
        OkHttpClient builder = new OkHttpClient.Builder()
                .addNetworkInterceptor(new CacheInterceptor())
                .cache(cache)
                .build();

        Retrofit retrofitInstance = new Retrofit
                .Builder()
                .baseUrl("https://nuggetwatch.co.nz/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(builder)
                .build();

        API apiService = retrofitInstance.create(API.class);

        Call<List<PriceModel>> apiCall = null;
        try {
            apiCall = apiService.prices(json.getJSONObject("properties").getString("nicename"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final TextView priceMessage = findViewById(R.id.priceMessage);

        apiCall.enqueue(new Callback<List<PriceModel>>() {
            @Override
            public void onResponse(Call<List<PriceModel>> call, Response<List<PriceModel>> response) {
                // set up the RecyclerView
                RecyclerView recyclerView = findViewById(R.id.priceRecycler);
                LinearLayoutManager layoutManager = new LinearLayoutManager(StoreActivity.this);
                recyclerView.setLayoutManager(layoutManager);
                priceAdapter = new PriceAdapter(StoreActivity.this, response.body());
                recyclerView.setLayoutFrozen(true);
                recyclerView.setAdapter(priceAdapter);

                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                        layoutManager.getOrientation());
                recyclerView.addItemDecoration(dividerItemDecoration);

                if (response.body().size() > 0) {
                    pricesLayout.setVisibility(View.VISIBLE);
                    priceMessage.setText(R.string.badPrices);
                } else {
                    pricesLayout.setVisibility(View.VISIBLE);
                    priceMessage.setText(R.string.missingPrices);

                    TableRow priceTableHeader = findViewById(R.id.tableRow);
                    priceTableHeader.setVisibility(View.GONE);
                    View divider = findViewById(R.id.divider);
                    divider.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<PriceModel>> call, Throwable t) {
            }
        });
    }

    public void getReviews() {

        int cacheSize = 10 * 1024 * 1024; // 10MB

        File httpCacheDirectory = new File(this.getCacheDir(), "http-cache");
        Cache cache = new Cache(httpCacheDirectory, cacheSize);
        OkHttpClient builder = new OkHttpClient.Builder()
                .addNetworkInterceptor(new CacheInterceptor())
                .cache(cache)
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofitInstance = new Retrofit
                .Builder()
                .baseUrl("https://nuggetwatch.co.nz/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(builder)
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

                if (response.body().size() > 0) {
                    reviewsLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<ReviewModel>> call, Throwable t) {
                Log.d("JSON:: ", t.toString());
                // Todo: Let the user know it failed
            }
        });
    }
}
