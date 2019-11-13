package com.nuggetwatch.nuggetnav;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReviewActivity extends AppCompatActivity implements RatingBar.OnRatingBarChangeListener {

    private RatingBar flavourBar;
    private RatingBar mouthfeelBar;
    private RatingBar coatingBar;
    private RatingBar saucesBar;
    private RatingBar overallBar;
    public static final String MY_PREFS_NAME = "Prefs";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        String name = getIntent().getStringExtra("name");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Write a Review");

        TextView nameHeader = findViewById(R.id.name);
        nameHeader.setText(name);

        flavourBar = findViewById(R.id.flavourBar);
        mouthfeelBar = findViewById(R.id.mouthfeelBar);
        coatingBar = findViewById(R.id.coatingBar);
        saucesBar = findViewById(R.id.saucesBar);
        overallBar = findViewById(R.id.overallBar);

        flavourBar.setOnRatingBarChangeListener(this);
        mouthfeelBar.setOnRatingBarChangeListener(this);
        coatingBar.setOnRatingBarChangeListener(this);
        saucesBar.setOnRatingBarChangeListener(this);
        overallBar.setOnRatingBarChangeListener(this);

        Button submit = findViewById(R.id.submitButton);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postReview();
            }
        });
    }

    public void postReview() {
        OkHttpClient client = new OkHttpClient();
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        EditText comments = findViewById(R.id.commentsInput);

        RequestBody formBody = new FormBody.Builder()
                .add("name", prefs.getString("name", ""))
                .add("email", prefs.getString("email", ""))
                .add("chain", getIntent().getStringExtra("nicename"))
                .add("comments", comments.getText().toString())
                .add("flavour", String.valueOf(flavourBar.getRating()))
                .add("mouthfeel", String.valueOf(mouthfeelBar.getRating()))
                .add("coating", String.valueOf(coatingBar.getRating()))
                .add("sauces", String.valueOf(saucesBar.getRating()))
                .add("overall", String.valueOf(overallBar.getRating()))
                .build();

        Request request = new Request.Builder()
                .url("https://nuggetwatch.co.nz/api/reviews/new/")
                .post(formBody)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("CALL::", e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("CALL::", response.body().string());
                Log.d("CALL::", response.message());
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        if (rating < 1f) {
            ratingBar.setRating(1f);
        }
    }
}
