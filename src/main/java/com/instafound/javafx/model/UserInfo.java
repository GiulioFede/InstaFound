package com.instafound.javafx.model;

public class UserInfo {

    private String username;
    private String urlProfileImage;
    private String region;
    private String province;
    private long cap;

    public UserInfo(String username, String urlProfileImage, String region, String province, long cap) {
        this.username = username;
        this.urlProfileImage = urlProfileImage;
        this.region = region;
        this.province = province;
        this.cap = cap;
    }

    public UserInfo(){

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUrlProfileImage() {
        return urlProfileImage;
    }

    public void setUrlProfileImage(String urlProfileImage) {
        this.urlProfileImage = urlProfileImage;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public long getCap() {
        return cap;
    }

    public void setCap(long cap) {
        this.cap = cap;
    }
}
