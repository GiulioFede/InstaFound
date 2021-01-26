package com.instafound.javafx.model;

/*
this class represents the type of data we need to show in the analytics of each campaign (created).
It is a class used both for data on donations, both for followers and finally for the goal achieved.
 */

public class AnalyticalData {

    private double value;
    private String key;

    public AnalyticalData(double value, String key) {
        this.value = value;
        this.key = key;
    }

    public AnalyticalData(){}

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
