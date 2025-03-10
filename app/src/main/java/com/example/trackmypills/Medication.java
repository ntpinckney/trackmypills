package com.example.trackmypills;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.time.LocalTime;

@Entity(tableName = "medications") //Establishes RoomDB table
@TypeConverters(Converters.class)
public class Medication {
    @PrimaryKey(autoGenerate = true) // Generates a new key for each entry
    private int id;
    private String name; // Name of medication
    private int medAmt; // Maximum quantity of medication
    private AdminType adminType; // Type of administration (pills, mL, puffs)
    private LocalTime startTime; // Starting time of medication
    private ReminderTime reminderTime;

    // Constructor
    public Medication(String name, int medAmt, AdminType adminType, LocalTime startTime, ReminderTime reminderTime) {
        this.name = name;
        this.medAmt = medAmt;
        this.adminType = adminType;
        this.startTime = startTime;
        this.reminderTime = reminderTime;
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

    public ReminderTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(ReminderTime reminderTime) {
        this.reminderTime = reminderTime;
    }
}
