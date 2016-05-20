package com.uestc.lyreg.carsharing.network.api;


import com.uestc.lyreg.carsharing.entity.AuthRequest;
import com.uestc.lyreg.carsharing.entity.AuthResponse;
import com.uestc.lyreg.carsharing.entity.DistRequest;
import com.uestc.lyreg.carsharing.entity.OpenRequest;
import com.uestc.lyreg.carsharing.entity.RegRequest;
import com.uestc.lyreg.carsharing.entity.RegResponse;

import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by Administrator on 2016/5/19.
 *
 * @Author lyreg
 */
public interface CarSharingService {

    @POST("v1/user/registration")
    Observable<RegResponse> registration(@Body RegRequest request);

    @POST("v1/user/authRequest")
    Observable<AuthResponse> auth(@Body AuthRequest request);

    @POST("v1/user/distribution")
    Observable<ResponseBody> distribution(@Body DistRequest request);

    @POST("v1/user/open")
    Observable<ResponseBody> opendoor(@Body OpenRequest request);
}
