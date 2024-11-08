package com.example.personalhealthcareapplication.model;

import java.util.List;
public class User {
    public String name;
    public String mobile;
    public String email;
    public String dob;
    public String weight;
    public String height;
    public List<String> medicalConditions;

    public User () {}; // Empty constructor

    public User(String name, String mobile, String email, String dob, String weight, String height, List<String> medicalConditions) {
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.dob = dob;
        this.weight = weight;
        this.height = height;
        this.medicalConditions = medicalConditions;
    }

}
