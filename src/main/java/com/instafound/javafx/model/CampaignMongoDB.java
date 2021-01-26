package com.instafound.javafx.model;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class CampaignMongoDB {

    private ObjectId _id;
    private String title;
    private String urlImage;
    private String startDate;
    private double amountRequired;
    private double amountAchived;
    private int countDonations;
    private int countFollowers;
    private String organizer;
    private ArrayList<String> membersOfTeam;
    private String description;
    private String beneficiary;
    private String category;
    private List<Donor> donations;
    private int maxLenght;

    public CampaignMongoDB(ObjectId _id,
                           String title,
                           String urlImage,
                           String startDate,
                           double amountRequired,
                           double amountAchived,
                           int countDonations,
                           int countFollowers,
                           String organizer,
                           ArrayList<String> membersOfTeam,
                           String description,
                           String beneficiary,
                           String category,
                           List<Donor> donations,
                           int maxLenght) {

        this._id = _id;
        this.title = title;
        this.urlImage = urlImage;
        this.startDate = startDate;
        this.amountRequired = amountRequired;
        this.amountAchived = amountAchived;
        this.countDonations = countDonations;
        this.countFollowers = countFollowers;
        this.organizer = organizer;
        this.membersOfTeam = membersOfTeam;
        this.description = description;
        this.beneficiary = beneficiary;
        this.category = category;
        this.donations = donations;
        this.maxLenght = maxLenght;
    }

    public CampaignMongoDB(ObjectId _id,
                           String title,
                           String urlImage,
                           String startDate,
                           double amountRequired,
                           double amountAchived,
                           int countDonations,
                           int countFollowers,
                           String organizer,
                           ArrayList<String> membersOfTeam,
                           String description,
                           String beneficiary,
                           String category,
                           List<Donor> donations) {

        this._id = _id;
        this.title = title;
        this.urlImage = urlImage;
        this.startDate = startDate;
        this.amountRequired = amountRequired;
        this.amountAchived = amountAchived;
        this.countDonations = countDonations;
        this.countFollowers = countFollowers;
        this.organizer = organizer;
        this.membersOfTeam = membersOfTeam;
        this.description = description;
        this.beneficiary = beneficiary;
        this.category = category;
        this.donations = donations;
    }

    public CampaignMongoDB(){}

    public void printCampaign(){

        System.out.println("CAMPAGNA: "+title+"\n"+
                "id: "+ _id +"\n"+
                "urlImage: "+ urlImage +"\n"+
                "startDate: "+startDate+"\n"+
                "countRequired: "+ amountRequired +"\n"+
                "countAchived: "+ amountAchived +"\n"+
                "countDonations: "+countDonations+"\n"+
                "countFollowers: "+countFollowers+"\n"+
                "organizer: "+organizer+"\n"+
                "membersOfTeam: "+membersOfTeam.toString()+"\n"+
                "description: "+description+"\n"+
                "category: "+category+"\n"+
                "list of donors: "+((donations ==null)?("null"): donations.toString())+"\n"+
                "__________________________________________________\n");
    }

    public void set_id(ObjectId _id){ this._id = _id; }

    public ObjectId get_id(){ return _id; }

    public String getTitle() {
        return title;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public String getStartDate() {
        return startDate;
    }

    public double getAmountRequired() {
        return amountRequired;
    }

    public double getAmountAchived() {
        return amountAchived;
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

    public List<Donor> getDonations() {
        return donations;
    }

    public int getMaxLenght(){
        return maxLenght;
    }

    public void setAmountAchived(double amountAchived) {
        this.amountAchived = amountAchived;
    }
}
