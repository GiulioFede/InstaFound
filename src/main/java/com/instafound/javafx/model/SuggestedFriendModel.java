package com.instafound.javafx.model;

public class SuggestedFriendModel extends UserInfo {

    private int inCommon;

    public SuggestedFriendModel(String username, String urlProfileImage, String region, String province, long cap, int inCommon) {
        super(username, urlProfileImage, region, province, cap);
        this.inCommon = inCommon;
    }

    public SuggestedFriendModel(int inCommon) {
        this.inCommon = inCommon;
    }

    public int getInCommon() {
        return inCommon;
    }
}
