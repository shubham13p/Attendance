package com.example.shubhampatel.professorattendance;

class User {

    String email, present;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPresent() {
        return present;
    }

    public void setPresent(String present) {
        this.present = present;
    }

    public User(String email, String present) {
        this.email = email;
        this.present = present;

    }
}