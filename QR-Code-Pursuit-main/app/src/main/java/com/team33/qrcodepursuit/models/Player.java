package com.team33.qrcodepursuit.models;


//THIS IS JUST A PLACEHOLDER
public class Player {
    private String username;
    private String bio;
    private String region;

    public Player(String username, String bio, String region){
        this.username = username;
        this.bio = bio;
        this.region = region;
    }

    public String getUsername(){
        return this.username;
    }
    public String getBio(){
        return this.bio;
    }
    public String getRegion(){
        return this.region;
    }
}
