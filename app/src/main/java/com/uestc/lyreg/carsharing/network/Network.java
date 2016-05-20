package com.uestc.lyreg.carsharing.network;

import com.uestc.lyreg.carsharing.network.api.CarSharingService;

/**
 * Created by Administrator on 2016/5/19.
 *
 * @Author lyreg
 */
public class Network {

    private static String BASE_URL = "http://localhost:8008/";

    private static CarSharingService carSharingApi;

    public static CarSharingService getCarSharingApi() {
        if(carSharingApi == null) {
            ServiceFactory.createService(BASE_URL, CarSharingService.class);
        }

        return carSharingApi;
    }
}
