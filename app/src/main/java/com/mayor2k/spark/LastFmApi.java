package com.mayor2k.spark;

import com.mayor2k.spark.Interfaces.ApiService;
import com.mayor2k.spark.Models.Artist;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LastFmApi {
    public static String last_fm_api = "dec8600e2d5bb562c490b73fe6cb18a9";
    private final static String base_url = "http://ws.audioscrobbler.com/2.0/";

    private static Retrofit getRetrofitInstance() {
        return new Retrofit.Builder()
                .baseUrl(base_url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static ApiService getApiService(){
        return getRetrofitInstance().create(ApiService.class);
    }
}
