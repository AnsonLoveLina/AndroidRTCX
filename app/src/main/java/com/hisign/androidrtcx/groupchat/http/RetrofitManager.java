package com.hisign.androidrtcx.groupchat.http;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitManager {
    private final HttpLoggingInterceptor httpLoggingInterceptor;
    private final Retrofit retrofit;

    public static Retrofit getRetrofitByAddress(String address) {
        RetrofitManager retrofitManager = new RetrofitManager(address);
        return retrofitManager.retrofit;
    }

    public RetrofitManager(String baseUrl) {
        httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(buildOkHttpClinet())
                .build();

    }

    private OkHttpClient buildOkHttpClinet() {
        return new OkHttpClient.Builder()
                .writeTimeout(3000, TimeUnit.MILLISECONDS)
                .connectTimeout(3000, TimeUnit.MILLISECONDS)
                .readTimeout(3000, TimeUnit.MILLISECONDS)
                .addInterceptor(httpLoggingInterceptor)
                .build();
    }

    public <T> T create(Class<T> clazz) {
        return retrofit.create(clazz);
    }
}
