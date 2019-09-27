package com.nuggetwatch.nuggetnav;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NearestModel {

    @SerializedName("chain")
    @Expose
    private String chain;
    @SerializedName("lat")
    @Expose
    private Double lat;
    @SerializedName("lng")
    @Expose
    private Double lng;
    @SerializedName("distance")
    @Expose
    private String distance;

    public String getChain() {
        return chain;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public String getdistance() {
        return distance;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "Movie {" +
                "actors='" + chain + '\'' +
                ", title='" + lat + '\'' +
                ", year='" + lng + '\'' +
                '}';
    }

}