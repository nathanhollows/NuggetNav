package com.nuggetwatch.nuggetnav;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReadReviewActivity extends AppCompatActivity {

    private ReviewModel review;
    public static final String MY_PREFS_NAME = "NuggetNavThumbs";
    private SharedPreferences prefs;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_review);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle(getIntent().getStringExtra("name"));

        review = (ReviewModel) getIntent().getSerializableExtra("review");

        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

        loadReview();

        // // Hide thumbs for now
        LinearLayout thumbs = findViewById(R.id.thumbs);
        thumbs.setVisibility(View.GONE);
        // loadThumbs();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadReview() {
        TextView name = findViewById(R.id.name);
        TextView date = findViewById(R.id.date);
        TextView comments = findViewById(R.id.comments);

        RatingBar flavourBar = findViewById(R.id.flavourBar);
        RatingBar mouthfeelBar = findViewById(R.id.mouthfeelBar);
        RatingBar coatingBar = findViewById(R.id.coatingBar);
        RatingBar saucesBar = findViewById(R.id.saucesBar);
        RatingBar overallBar = findViewById(R.id.overallBar);

        try {
            SimpleDateFormat formatIn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat formatOut = new SimpleDateFormat("dd MMMM yy");
            Date reviewDate = formatIn.parse(review.getDate());
            assert reviewDate != null;
            date.setText(formatOut.format(reviewDate));
        } catch (ParseException e) {
            date.setVisibility(View.GONE);
        }

        name.setText(review.getName());
        comments.setText(review.getComments());

        flavourBar.setRating(review.getFlavour());
        mouthfeelBar.setRating(review.getMouthfeel());
        coatingBar.setRating(review.getCoating());
        saucesBar.setRating(review.getSauces());
        overallBar.setRating(review.getOverall());

        if (review.getSauces() == 0) {
            LinearLayout sauces = findViewById(R.id.saucesContainer);
            sauces.setVisibility(View.GONE);
        }
    }

    private void loadThumbs() {

        final ImageView thumbUp = findViewById(R.id.thumbUp);
        final ImageView thumbDown = findViewById(R.id.thumbDown);

        thumbUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thumbUp.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
                thumbDown.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_ATOP);

                prefs.edit().putInt(review.getWebid(), 1).apply();
            }
        });

        thumbDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thumbDown.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
                thumbUp.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_ATOP);

                prefs.edit().putInt(review.getWebid(), 0).apply();
            }
        });

        int result = prefs.getInt(review.getWebid(), -1);

        if (result == 1) {
            thumbUp.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
            thumbDown.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_ATOP);
        } else if (result == 0) {
            thumbDown.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
            thumbUp.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_ATOP);
        }
    }
}
