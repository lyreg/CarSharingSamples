package com.uestc.lyreg.carsharing.entity;

/**
 * Created by Administrator on 2016/5/20.
 *
 * @Author lyreg
 */
public class DistRequest {


    /**
     * rand2 :
     * sessionkey :
     * hash :
     */

    private String rand2;
    private String sessionkey;
    private String hash;

    public DistRequest(String rand2, String sessionkey, String hash) {
        this.rand2 = rand2;
        this.sessionkey = sessionkey;
        this.hash = hash;
    }

    public String getRand2() {
        return rand2;
    }

    public void setRand2(String rand2) {
        this.rand2 = rand2;
    }

    public String getSessionkey() {
        return sessionkey;
    }

    public void setSessionkey(String sessionkey) {
        this.sessionkey = sessionkey;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
