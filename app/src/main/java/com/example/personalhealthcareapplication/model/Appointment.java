package com.example.personalhealthcareapplication.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Appointment {

    private String id; // Firestore document ID
    private String doctorName;
    private String date;
    private String time;

    // Default constructor (required for Firestore)
    public Appointment() {
    }

    // Constructor with parameters
    public Appointment(String doctorName, String date, String time) {
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    public long getAppointmentTimeInMillis() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
        try {
            String dateTime = date + " " + time;
            return dateFormat.parse(dateTime).getTime(); // Convert to milliseconds
        } catch (ParseException e) {
            e.printStackTrace();
            return -1; // Return -1 if parsing fails
        }
    }
}
