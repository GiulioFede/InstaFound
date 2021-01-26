package com.instafound.javafx.model;

//rappresenta il tipo di risultato che fornisce GeoGenerator
public class GeoData {

    private String region;
    private String province;
    private int cap;

    public GeoData(String region, String province, int cap) {
        this.region = region;
        this.province = province;
        this.cap = cap;
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
