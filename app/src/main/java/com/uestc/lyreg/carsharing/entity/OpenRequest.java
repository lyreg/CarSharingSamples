package com.uestc.lyreg.carsharing.entity;

/**
 * Created by Administrator on 2016/5/20.
 *
 * @Author lyreg
 */
public class OpenRequest {

    /**
     * request :
     * cardId :
     */
    private String request;
    private String cardId;

    public OpenRequest(String request, String cardId) {
        this.request = request;
        this.cardId = cardId;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }
}
