package com.instafound.javafx.model;

import java.util.ArrayList;

public class Team {

    private long id;
    private String name;
    private String usernameOrganizer;
    private ArrayList<String> usernameMembers;
    private ArrayList<String> idCampaignsSupported;

    public Team(long id,
                String name,
                String usernameOrganizer,
                ArrayList<String> usernameMembers,
                ArrayList<String> idCampaignsSupported) {

        this.name = name;
        this.id = id;
        this.usernameOrganizer = usernameOrganizer;
        this.usernameMembers = usernameMembers;
        this.idCampaignsSupported = idCampaignsSupported;
    }


    public Team(){}

    public String getUsernameOrganizer() {
        return usernameOrganizer;
    }

    public ArrayList<String> getUsernameMembers() {
        return usernameMembers;
    }

    public ArrayList<String> getIdCampaignsSupported() {
        return idCampaignsSupported;
    }

    public void addNewMember(String username){

        usernameMembers.add(username);
    }

    public void removeMember(String username){

       usernameMembers.remove(usernameMembers.indexOf(username));
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
