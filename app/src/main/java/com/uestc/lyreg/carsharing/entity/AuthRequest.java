package com.uestc.lyreg.carsharing.entity;

/**
 * Created by Administrator on 2016/5/20.
 *
 * @Author lyreg
 */
public class AuthRequest {


    /**
     * rand1 :
     * serialNum :
     * request :
     */

    private String rand1;
    private String serialNum;
    private String request;

    public AuthRequest(String rand1, String serialNum, String request) {
        this.rand1 = rand1;
        this.serialNum = serialNum;
        this.request = request;
    }

    public String getRand1() {
        return rand1;
    }

    public void setRand1(String rand1) {
        this.rand1 = rand1;
    }

    public String getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(String serialNum) {
        this.serialNum = serialNum;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }
}
