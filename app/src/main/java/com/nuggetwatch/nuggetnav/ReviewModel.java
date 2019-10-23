package com.nuggetwatch.nuggetnav;

public class ReviewModel {

    private String webid;
    private String comments;
    private String name;

    private int flavour;
    private int mouthfeel;
    private int coating;
    private int sauces;
    private int overall;

    private String date;
    private float score;

    public ReviewModel() {};

    public ReviewModel(String webid, String comments, String name,
                       int flavour, int mouthfeel, int coating, int sauces, int overall,
                       String datetime, float score) {

        this.webid = webid;
        this.comments = comments;
        this.name = name;
        this.flavour = flavour;
        this.mouthfeel = mouthfeel;
        this.coating = coating;
        this.sauces = sauces;
        this.overall = overall;
        this.date = datetime;
        this.score = score;

    }

    public String getWebid() {
        return webid;
    }

    public void setWebid(String webid) {
        this.webid = webid;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFlavour() {
        return flavour;
    }

    public void setFlavour(int flavour) {
        this.flavour = flavour;
    }

    public int getMouthfeel() {
        return mouthfeel;
    }

    public void setMouthfeel(int mouthfeel) {
        this.mouthfeel = mouthfeel;
    }

    public int getCoating() {
        return coating;
    }

    public void setCoating(int coating) {
        this.coating = coating;
    }

    public int getSauces() {
        return sauces;
    }

    public void setSauces(int sauces) {
        this.sauces = sauces;
    }

    public int getOverall() {
        return overall;
    }

    public void setOverall(int overall) {
        this.overall = overall;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String datetime) {
        this.date = date;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}
