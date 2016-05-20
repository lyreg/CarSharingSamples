package com.uestc.lyreg.carsharing.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by Administrator on 2016/5/19.
 *
 * @Author lyreg
 */
public class ServiceFactory {

//    private static OkHttpClient okHttpClient = new OkHttpClient();
    private static OkHttpClient okHttpClient = new OkHttpClient
        .Builder()
        .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build();
    private static Converter.Factory gsonConverterFacotry = GsonConverterFactory.create();
    private static Converter.Factory jacksonConverterFacotry = JacksonConverterFactory.create();
    private static CallAdapter.Factory rxJavaCallAdapterFacatory = RxJavaCallAdapterFactory.create();

    public static <T> T createService(String baseUrl, final Class<T> serviceClass) {
        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(baseUrl)
                .addConverterFactory(jacksonConverterFacotry) // 添加Gson转换器
                .addCallAdapterFactory(rxJavaCallAdapterFacatory) // 添加Rx适配器
                .build();
        return retrofit.create(serviceClass);
    }
}
