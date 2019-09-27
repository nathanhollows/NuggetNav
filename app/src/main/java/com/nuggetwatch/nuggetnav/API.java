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
}
