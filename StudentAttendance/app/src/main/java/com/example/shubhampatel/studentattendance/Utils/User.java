package com.example.shubhampatel.studentattendance.Utils;

import java.io.Serializable;

public class User implements Serializable{

    private String name, email, id, mac, profileimage, present, noofpresent;

    public User(String email, String name, String id, String mac, String profileimage, String noofpresent) {
        this.name = name;
        this.email = email;
        this.id = id;
        this.mac = mac;
        this.profileimage = profileimage;
        this.noofpresent = noofpresent;
    }


    public User(String email, String present) {
        this.email = email;
        this.present = present;
    }

    public User() {


    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getPresent() {
        return present;
    }

    public void setPresent(String present) {
        this.present = present;
    }

    public String getNoofpresent() {
        return noofpresent;
    }

    public void setNoofpresent(String noofpresent) {
        this.noofpresent = noofpresent;
    }
}
