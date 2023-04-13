package com.ksr.foodscanner.Models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String name, email, password;

    private ArrayList<String> restriction;

    public User() {
    }

    public User(String name, String email, String password,
                ArrayList<String> restriction) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.restriction = restriction;
    }

    public ArrayList<String> getRestriction() {
        return restriction;
    }

    public void setRestriction(ArrayList<String> restriction) {
        this.restriction = restriction;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
