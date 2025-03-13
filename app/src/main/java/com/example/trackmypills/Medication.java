package com.example.trackmypills;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity(tableName = "medications") // Establishes RoomDB table
@TypeConverters(Converters.class) // Establishes Converters for ReminderTime and AdminType
public class Medication {
    @PrimaryKey(autoGenerate = true) // Generates a new key for each entry
    private int id;
    @ColumnInfo(defaultValue = "medication_name")
    private String name; // Name of medication
    @ColumnInfo(defaultValue = "max_amt")
    private int maxAmt; // Maximum doses
    @ColumnInfo(defaultValue = "doses_taken")
    private int dosesTaken; // Tracks how many doses have been taken
    @ColumnInfo(defaultValue = "admin_type")
    private
    AdminType adminType; // Type of administration (pills, mL, puffs, etc.)
    @ColumnInfo(defaultValue = "start_time")
    private LocalTime startTime; // Starting time of medication
    @ColumnInfo(defaultValue = "frequency")
    private Frequency frequency; // Reminder time enums ("Every 2 hours," "Every 4 hours," etc.)
    @ColumnInfo(defaultValue = "last_reset_date")
    private LocalDate lastResetDate; // Used to reset the date, which also resets the medicine counter


    public Medication(String name, int maxAmt, AdminType adminType, LocalTime startTime, Frequency frequency) {
        this.name = name;
        this.maxAmt = maxAmt;
        this.adminType = adminType;
        this.startTime = startTime;
        this.frequency = frequency;
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
    public int getDosesTaken() {
        return dosesTaken;
    }

    public void setDosesTaken(int dosesTaken) {
        this.dosesTaken = dosesTaken;
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

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public LocalDate getLastResetDate(){
        return lastResetDate;
    }

    public  void setLastResetDate(LocalDate lastResetDate){
        this.lastResetDate = lastResetDate;
    }

    // Calculates the next dosageTime from startTime
    public LocalTime getNextDosageTime() {
        LocalTime now = LocalTime.now();
        LocalTime nextDose = startTime;

        for (int i = 0; i < maxAmt; i++) {
            nextDose = nextDose.plusHours(frequency.getIntervalHours());
            if (nextDose.isAfter(now)) {
                return nextDose;
            }
        }
        return startTime;
    }
}


