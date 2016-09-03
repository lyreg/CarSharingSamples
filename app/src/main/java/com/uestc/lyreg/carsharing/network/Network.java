package com.uestc.lyreg.carsharing.network;

import com.uestc.lyreg.carsharing.network.api.CarSharingService;

/**
 * Created by Administrator on 2016/5/19.
 *
 * @Author lyreg
 */
public class Network {

//    private static String BASE_URL = "http://localhost:8008/";
    private static String BASE_URL = "http://115.28.252.84:80/";

    private static CarSharingService carSharingApi;

    public static CarSharingService getCarSharingApi() {
        if(carSharingApi == null) {
            carSharingApi = ServiceFactory.createService(BASE_URL, CarSharingService.class);
        }

        return carSharingApi;
    }
}
