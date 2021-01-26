package com.instafound.javafx.model;

import java.util.ArrayList;

public class Campaign {

    private String title;
    private String campaignImage;
    private String startDate;
    private double countRequired;
    private double countAchived;
    private int countDonations;
    private String organizer;
    private ArrayList<String> membersOfTeam;
    private String description;
    private String beneficiary;
    private String category;
    private ArrayList<String>  listOfDonors;
    private ArrayList<String>  timestampDonations;
    private ArrayList<Double>  amountsDonated;

    public Campaign(String title,
                    String campaignImage,
                    String startDate,
                    double countRequired,
                    double countAchived,
                    int countDonations,
                    String organizer,
                    ArrayList<String> membersOfTeam,
                    String description,
                    String beneficiary,
                    String category,
                    ArrayList<String> listOfDonors,
                    ArrayList<String> timestampDonations,
                    ArrayList<Double> amountsDonated) {

        this.title = title;
        this.campaignImage = campaignImage;
        this.startDate = startDate;
        this.countRequired = countRequired;
        this.countAchived = countAchived;
        this.countDonations = countDonations;
        this.organizer = organizer;
        this.membersOfTeam = membersOfTeam;
        this.description = description;
        this.beneficiary = beneficiary;
        this.category = category;
        this.listOfDonors = listOfDonors;
        this.timestampDonations = timestampDonations;
        this.amountsDonated = amountsDonated;
    }

    public Campaign(){}

    public void printCampaign(){

        System.out.println("CAMPAGNA: "+title+"\n"+
                "urlImage: "+campaignImage+"\n"+
                "startDate: "+startDate+"\n"+
                "countRequired: "+countRequired+"\n"+
                "countAchived: "+countAchived+"\n"+
                "countDonations: "+countDonations+"\n"+
                "organizer: "+organizer+"\n"+
                "membersOfTeam: "+membersOfTeam.toString()+"\n"+
                "beneficiary: "+beneficiary+"\n"+
                "description: "+description+"\n"+
                "category: "+category+"\n"+
                "list of donors: "+listOfDonors.toString()+"\n"+
                "amounts donated: "+amountsDonated.toString()+"\n"+
                "timestamp donations: "+timestampDonations.toString()+"\n"+
                "sizeArray(donatori,donazioni,timestamp): "+listOfDonors.size()+","+amountsDonated.size()+","+timestampDonations.size()+"\n"+
                "__________________________________________________\n");
    }

    public String getTitle() {
        return title;
    }

    public String getCampaignImage() {
        return campaignImage;
    }

    public String getStartDate() {
        return startDate;
    }

    public double getCountRequired() {
        return countRequired;
    }

    public double getCountAchived() {
        return countAchived;
    }

    public int getCountDonations() {
        return countDonations;
    }

    public String getOrganizer() {
        return organizer;
    }

    public ArrayList<String> getMembersOfTeam() {
        return membersOfTeam;
    }

    public String getDescription() {
        return description;
    }

    public String getBeneficiary() {
        return beneficiary;
    }

    public String getCategory() {
        return category;
    }

    public ArrayList<String> getListOfDonors() {
        return listOfDonors;
    }

    public ArrayList<String> getTimestampDonations() {
        return timestampDonations;
    }

    public ArrayList<Double> getAmountsDonated() {
        return amountsDonated;
    }
}
