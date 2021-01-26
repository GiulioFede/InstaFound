package com.instafound.javafx.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class DonorRankModel {

    public SimpleStringProperty username;
    public SimpleDoubleProperty totalAmountDonated;

    public DonorRankModel(String username, double totalAmountDonated) {
        this.username = new SimpleStringProperty(username);
        this.totalAmountDonated = new SimpleDoubleProperty(totalAmountDonated);
    }

    public String getUsername() {
        return username.get();
    }

    public double getTotalAmountDonated() {
        return totalAmountDonated.get();
    }

    public SimpleDoubleProperty totalAmountDonatedProperty() {
        return totalAmountDonated;
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public void setTotalAmountDonated(double totalAmountDonated) {
        this.totalAmountDonated.set(totalAmountDonated);
    }
}
