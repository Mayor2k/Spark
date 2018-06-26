package com.mayor2k.spark.Interfaces;
import com.mayor2k.spark.Models.LastFmModels.LastFmModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("?method=artist.getinfo&api_key=dec8600e2d5bb562c490b73fe6cb18a9&format=json")
    Call<LastFmModel> getArtistImage(@Query("artist") String userId);
}
