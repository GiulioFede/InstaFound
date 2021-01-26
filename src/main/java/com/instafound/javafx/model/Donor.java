package com.instafound.javafx.model;

public class Donor {

    private String donor;
    private double amount;
    private String timestamp;

    public Donor(String username, double amountDonated, String timestamp) {
        this.donor = username;
        this.amount = amountDonated;
        this.timestamp = timestamp;
    }

    //costructor for ranking analitycs
    public Donor(String donor, double amount) {
        this.donor = donor;
        this.amount = amount;
    }

    public String getDonor() {
        return donor;
    }

    public void setDonor(String donor) {
        this.donor = donor;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
