package com.nuggetwatch.nuggetnav;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ChainsModel {

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("rating")
    @Expose
    private int rating;

    @SerializedName("rating_count")
    @Expose
    private int rating_count;

    public String getName() {
        return name;
    }

    public int getID() {
        return id;
    }

    public int getRating() {
        return rating;
    }

    public int getRatingCount() {
        return rating_count;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setID(int id) {
        this.id = id;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setRatingCount(int rating) {
        this.rating_count = rating_count;
    }

}