package com.instafound.javafx.model;

public class User {

    private String username;
    private String password;
    private String urlImage;
    private String region;
    private String province;
    private int cap;

    public User(String username, String password, String urlImage, String region,
            String province, int cap) {
        this.username = username;
        this.password = password;
        this.urlImage = urlImage;
        this.region = region;
        this.province = province;
        this.cap = cap;
    }

    public void printUser(){

        System.out.println("UTENTE username: "+username+"\n"+
                "password: "+password+"\n"+
                "urlImage: "+urlImage+"\n"+
                "region: "+region+"\n"+
                "province: "+province+"\n"+
                "cap: "+cap+"\n");
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public String getRegion() {
        return region;
    }

    public String getProvince() {
        return province;
    }

    public int getCap() {
        return cap;
    }
}
