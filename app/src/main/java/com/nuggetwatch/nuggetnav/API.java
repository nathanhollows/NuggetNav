package com.nuggetwatch.nuggetnav;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface API {

    void API();

    @GET("/api/nearest/coords/{lat}/{lng}/")
    Call<NearestModel> nearest(
            @Path("lat") String lat,
            @Path("lng") String lng
    );

    @GET("/api/prices/chain/{nicename}")
    Call<List<PriceModel>> prices(
            @Path("nicename") String nicename
    );

    @GET("/api/reviews/chain/{nicename}")
    Call<List<ReviewModel>> reviews(
            @Path("nicename") String nicename
    );
}
