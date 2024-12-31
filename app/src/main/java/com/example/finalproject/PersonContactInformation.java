package com.example.finalproject;

public class PersonContactInformation {

    private String id;
    private String name;
    private String phn_number;
    private String image_path;
    private String _id;

    public PersonContactInformation() {
    }


    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhn_number() {
        return phn_number;
    }

    public void setPhn_number(String phn_number) {
        this.phn_number = phn_number;
    }

    public String getImage_path(){return this.image_path;}

    public void setImage_path(String image_path){this.image_path = image_path;}
}
