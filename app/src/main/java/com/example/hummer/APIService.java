package com.example.hummer;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface APIService {
    @POST("/")
    @FormUrlEncoded
    Call<JsonObject> savePost(@Field("url") String url,
                              @Field("return") String returnva,
                              @Field("api_token") String api_token);
}
