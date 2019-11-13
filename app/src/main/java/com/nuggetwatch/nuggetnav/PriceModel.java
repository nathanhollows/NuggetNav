package com.nuggetwatch.nuggetnav;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class PriceModel {

    private float price;
    private int number;
    private float per;
    private static NumberFormat formatter = new DecimalFormat("$0.00");


    public PriceModel() {};

    public PriceModel(float price, int number, float per) {
        this.price = price;
        this.number = number;
        this.per = per;
    }

    public String getPrice() {
        return formatter.format(price);
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getPer() {
        return formatter.format(per);
    }

    public void setPer(float per) {
        this.per = per;
    }
}
