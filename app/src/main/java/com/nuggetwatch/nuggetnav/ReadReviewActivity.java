package com.nuggetwatch.nuggetnav;

import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReadReviewActivity extends AppCompatActivity {

    private ReviewModel review;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_review);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportActionBar().setTitle(getIntent().getStringExtra("name"));

        review = (ReviewModel) getIntent().getSerializableExtra("review");

        try {
            loadReview();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadReview() throws ParseException {
        TextView name = findViewById(R.id.name);
        TextView date = findViewById(R.id.date);
        TextView comments = findViewById(R.id.comments);

        RatingBar flavourBar = findViewById(R.id.flavourBar);
        RatingBar mouthfeelBar = findViewById(R.id.mouthfeelBar);
        RatingBar coatingBar = findViewById(R.id.coatingBar);
        RatingBar saucesBar = findViewById(R.id.saucesBar);
        RatingBar overallBar = findViewById(R.id.overallBar);

        SimpleDateFormat formatIn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat formatOut = new SimpleDateFormat("dd MMMM");
        Date reviewDate = formatIn.parse(review.getDate());

        name.setText(review.getName());
        date.setText(formatOut.format(reviewDate));
        comments.setText(review.getComments());

        flavourBar.setRating(review.getFlavour());
        mouthfeelBar.setRating(review.getMouthfeel());
        coatingBar.setRating(review.getCoating());
        saucesBar.setRating(review.getSauces());
        overallBar.setRating(review.getOverall());

        if (review.getSauces() == 0) {
            saucesBar.setVisibility(View.GONE);
            findViewById(R.id.sauces).setVisibility(View.GONE);
        }
    }
}
