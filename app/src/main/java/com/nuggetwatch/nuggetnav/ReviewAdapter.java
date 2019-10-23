package com.nuggetwatch.nuggetnav;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<ReviewModel> mData;
    private LayoutInflater mInflater;
    private ReviewClickListener mClickListener;

    // data is passed into the constructor
    ReviewAdapter(Context context, List<ReviewModel> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.review, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ReviewModel review = mData.get(position);
        holder.name.setText(review.getName());
        holder.comments.setText(review.getComments());
        holder.flavour.setRating(review.getFlavour());
        holder.mouthfeel.setRating(review.getMouthfeel());
        holder.coating.setRating(review.getCoating());
        holder.sauces.setRating(review.getSauces());
        holder.overall.setRating(review.getOverall());
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        TextView comments;
        RatingBar flavour;
        RatingBar mouthfeel;
        RatingBar coating;
        RatingBar sauces;
        RatingBar overall;


        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            comments = itemView.findViewById(R.id.comments);
            flavour = itemView.findViewById(R.id.flavourBar);
            mouthfeel = itemView.findViewById(R.id.mouthfeelBar);
            coating = itemView.findViewById(R.id.coatingBar);
            sauces = itemView.findViewById(R.id.saucesBar);
            overall = itemView.findViewById(R.id.overallBar);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onReviewClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id).toString();
    }

    // allows clicks events to be caught
    void setClickListener(ReviewClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ReviewClickListener {
        void onReviewClick(View view, int position);
    }
}