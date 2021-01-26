package com.instafound.javafx.model;

import org.bson.types.ObjectId;

public class CampaignNeo4J {

    private ObjectId _id;
    private String title;
    private String urlImage;

    public CampaignNeo4J(ObjectId id, String title, String urlImage) {
        this._id = id;
        this.title = title;
        this.urlImage = urlImage;
    }

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId id) {
        this._id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }
}
