package com.uestc.lyreg.carsharing.entity;

/**
 * Created by Administrator on 2016/5/20.
 *
 * @Author lyreg
 */
public class RegRequest {

    /**
     * sessionkey :
     * username :
     * password :
     * hash :
     * csr :
     */

    private String sessionkey;
    private String username;
    private String password;
    private String hash;
    private String csr;

    public RegRequest(String sessionkey, String username,
                      String password,   String hash,     String csr ) {
        this.sessionkey = sessionkey;
        this.username   = username;
        this.password   = password;
        this.hash       = hash;
        this.csr        = csr;
    }

    public String getSessionkey() {
        return sessionkey;
    }

    public void setSessionkey(String sessionkey) {
        this.sessionkey = sessionkey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getCsr() {
        return csr;
    }

    public void setCsr(String csr) {
        this.csr = csr;
    }
}
