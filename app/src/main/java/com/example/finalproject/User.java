package com.example.finalproject;

public class User {

    private String username;
    private String uid;

    public User(String username, String uid) {
        this.username = username;
        this.uid = uid;
    }

    public User(){}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
