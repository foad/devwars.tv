package com.bezman.request.model;

/**
 * Created by Terence on 7/7/2015.
 */
public class EarnedBet {

    public String twitchUsername;

    public int pointsEarned;

    public EarnedBet(String twitchUsername, int pointsEarned) {
        this.twitchUsername = twitchUsername;
        this.pointsEarned = pointsEarned;
    }

    public String getTwitchUsername() {
        return twitchUsername;
    }

    public void setTwitchUsername(String twitchUsername) {
        this.twitchUsername = twitchUsername;
    }

    public int getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(int pointsEarned) {
        this.pointsEarned = pointsEarned;
    }
}
