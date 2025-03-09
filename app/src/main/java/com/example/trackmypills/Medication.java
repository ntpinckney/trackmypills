package com.example.trackmypills;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalTime;

@Entity(tableName = "medications") //Establishes RoomDB table
public class Medication {
    @PrimaryKey(autoGenerate = true) // Generates a new key for each entry
    private int id;
    private String name; // Name of medication
    private int medAmt; // Maximum quantity of medication
    private AdminType adminType; // Type of administration (pills, mL, puffs)
    private LocalTime startTime; // Starting time of medication

    // Constructor
    public Medication(String name, int medAmt, AdminType adminType, LocalTime startTime) {
        this.name = name;
        this.medAmt = medAmt;
        this.adminType = adminType;
        this.startTime = startTime;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMedAmt() {
        return medAmt;
    }

    public void setMedAmt(int medAmt) {
        this.medAmt = medAmt;
    }

    public AdminType getAdminType() {
        return adminType;
    }

    public void setAdminType(AdminType adminType) {
        this.adminType = adminType;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
}
