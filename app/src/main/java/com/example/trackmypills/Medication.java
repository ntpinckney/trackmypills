package com.example.trackmypills;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.time.LocalTime;

@Entity(tableName = "medications") // Establishes RoomDB table
@TypeConverters(Converters.class) // Establishes Converters for ReminderTime and AdminType
public class Medication {
    @PrimaryKey(autoGenerate = true) // Generates a new key for each entry
    private int id;
    private String name; // Name of medication
    private int maxAmt; // Maximum quantity of medication
    private AdminType adminType; // Type of administration (pills, mL, puffs, etc.)
    private LocalTime startTime; // Starting time of medication
    private ReminderTime reminderTime; // Reminder time enums ("Every 2 hours," "Every 4 hours," etc.)

    // Constructor
    public Medication(String name, int maxAmt, AdminType adminType, LocalTime startTime, ReminderTime reminderTime) {
        this.name = name;
        this.maxAmt = maxAmt;
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

    public int getMaxAmt() {
        return maxAmt;
    }

    public void setMaxAmt(int maxAmt) {
        this.maxAmt = maxAmt;
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

    public LocalTime getNextDosageTime() {
        LocalTime now = LocalTime.now();
        LocalTime nextDose = startTime;

        for (int i = 0; i < maxAmt; i++) {
            nextDose = nextDose.plusHours(reminderTime.getIntervalHours());
            if (nextDose.isAfter(now)) {
                return nextDose;
            }
        }
        return startTime;
    }
}


