package com.nuggetwatch.nuggetnav;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

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
    private EditText comments;
    private String nicename;
    private static int result = -1;
    public static final String MY_PREFS_NAME = "Prefs";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        String name = getIntent().getStringExtra("name");
        nicename = getIntent().getStringExtra("nicename");

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

        comments = findViewById(R.id.commentsInput);

        Button submit = findViewById(R.id.submitButton);
        submit.setBackgroundColor(getResources().getColor(R.color.gray));
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ReviewActivity.this, "Please rate everything", Toast.LENGTH_SHORT).show();
            }
        });

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

        if (prefs.contains(nicename + "-flavourBar")) {
            flavourBar.setRating(prefs.getInt(nicename + "-flavourBar", 0));
        }
        if (prefs.contains(nicename + "-mouthfeelBar")) {
            mouthfeelBar.setRating(prefs.getInt(nicename + "-mouthfeelBar", 0));
        }
        if (prefs.contains(nicename + "-coatingBar")) {
            coatingBar.setRating(prefs.getInt(nicename + "-coatingBar", 0));
        }
        if (prefs.contains(nicename + "-saucesBar")) {
            saucesBar.setRating(prefs.getInt(nicename + "-saucesBar", 0));
        }
        if (prefs.contains(nicename + "-overallBar")) {
            overallBar.setRating(prefs.getInt(nicename + "-overallBar", 0));
        }
        if (prefs.contains(nicename + "-comments")) {
            comments.setText(prefs.getString(nicename + "-comments", ""));
        }

    }

    public void postReview() {
        OkHttpClient client = new OkHttpClient();
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

        RequestBody formBody = new FormBody.Builder()
                .add("name", prefs.getString("name", ""))
                .add("email", prefs.getString("email", ""))
                .add("chain", nicename)
                .add("comments", comments.getText().toString())
                .add("flavour", String.valueOf(((int) flavourBar.getRating())))
                .add("mouthfeel", String.valueOf((int) mouthfeelBar.getRating()))
                .add("coating", String.valueOf((int) coatingBar.getRating()))
                .add("sauces", String.valueOf((int) saucesBar.getRating()))
                .add("overall", String.valueOf((int) overallBar.getRating()))
                .build();

        Request request = new Request.Builder()
                .url("https://nuggetwatch.co.nz/api/reviews/new/")
                .post(formBody)
                .build();

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                result = 0;
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.body() != null) {
                    result = 1;
                } else {
                    result = 0;
                }
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        setReturnValue();
        this.finish();


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

        // If all ratings (except sauces) have been chosen then enable form submit
        if (flavourBar.getRating() >= 1 &&
                mouthfeelBar.getRating() >= 1 &&
                coatingBar.getRating() >= 1 &&
                overallBar.getRating() >= 1) {
            activateSubmit();
        }

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putInt(nicename + "-" + getResources().getResourceEntryName(ratingBar.getId()),
                (int) ratingBar.getRating()).apply();

    }

    private void activateSubmit() {
        Button submit = findViewById(R.id.submitButton);

        submit.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postReview();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        saveComments();
    }

    @Override
    public void onDestroy() {
        int response;
        super.onDestroy();
        saveComments();

    }

    private void saveComments() {
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(nicename + "-comments",
                comments.getText().toString()).apply();
    }

    private void setReturnValue() {
        int response = 1;
        switch (result) {
            // Review was not able to be sent (error)
            case 0:
                break;
            // Review was sent (no error)
            case 1: response = ReviewActivity.RESULT_OK;
                break;
            // Review has now been sent (no error)
            default: response = ReviewActivity.RESULT_CANCELED;
        }

        Intent returnIntent = new Intent();
        setResult(response, returnIntent);
        finish();
    }
}
