package com.example.hummer;

public class ApiUtils {
    private ApiUtils() {}

    public static final String BASE_URL = "https://api.audd.io/";

    public static APIService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
}
