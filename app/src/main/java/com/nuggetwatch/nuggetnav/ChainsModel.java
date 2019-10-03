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

    public String getName() {
        return name;
    }

    public int getID() {
        return id;
    }

    public int getRating() {
        return rating;
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

    @Override
    public String toString() {
        return "Movie {" +
                "actors='" + name + '\'' +
                '}';
    }

}