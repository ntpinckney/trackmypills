package com.example.trackmypills.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.trackmypills.util.Converters;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity(tableName = "medications") // Establishes RoomDB table
@TypeConverters(Converters.class) // Establishes Converters for ReminderTime, AdminType, and DateTime formats
public class Medication {
    @PrimaryKey(autoGenerate = true) // Generates a new key for each entry
    private int id;
    @ColumnInfo(defaultValue = "medication_name")
    private String name; // Name of medication
    @ColumnInfo(defaultValue = "med_quantity")
    private double medQuantity; // Quantity per dose
    @ColumnInfo(defaultValue = "max_amt")
    private double maxAmt; // Maximum doses per day
    @ColumnInfo(defaultValue = "total_meds")
    private double totalMeds; // Total doses to be taken
    @ColumnInfo(defaultValue = "doses_taken")
    private double dosesTaken; // Tracks how many doses have been taken
    @ColumnInfo(defaultValue = "admin_type")
    private
    AdminType adminType; // Type of administration (pills, mL, puffs, etc.)
    @ColumnInfo(defaultValue = "start_time")
    private LocalTime startTime; // Starting time of medication
    @ColumnInfo(defaultValue = "frequency")
    private Frequency frequency; // Reminder time enums ("Every 2 hours," "Every 4 hours," etc.)
    @ColumnInfo(defaultValue = "last_reset_date")
    private LocalDate lastResetDate; // Used to reset the date, which also resets the medicine counter
    @ColumnInfo(defaultValue = "notifications_enabled")
    private boolean notificationsEnabled; // Boolean to check if notifications are enabled
    @ColumnInfo(defaultValue = "next_dosage_time")
    private LocalDateTime nextDosageTime; // Tracks next dosage time

    // Empty constructor
    public Medication() {

    }

    // Default constructor
    public Medication(String name, double medQuantity, double maxAmt, double totalMeds, AdminType adminType, LocalTime startTime, Frequency frequency) {
        this.name = name;
        this.medQuantity = medQuantity;
        this.maxAmt = maxAmt;
        this.totalMeds = totalMeds;
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

    public double getMaxAmt() {
        return maxAmt;
    }

    public void setMaxAmt(double maxAmt) {
        this.maxAmt = maxAmt;
    }

    public double getDosesTaken() {
        return dosesTaken;
    }

    public void setDosesTaken(double dosesTaken) {
        this.dosesTaken = dosesTaken;
    }

    public double getMedQuantity() {
        return medQuantity;
    }

    public void setMedQuantity(double medQuantity) {
        this.medQuantity = medQuantity;
    }

    public double getTotalMeds() {
        return totalMeds;
    }

    public void setTotalMeds(double totalMeds) {
        this.totalMeds = totalMeds;
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

    public LocalDate getLastResetDate() {
        return lastResetDate;
    }

    public void setLastResetDate(LocalDate lastResetDate) {
        this.lastResetDate = lastResetDate;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public LocalDateTime getNextDosageTime() {
        if (nextDosageTime != null) {
            return nextDosageTime;
        }

        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now(), startTime);
        double intervalHours = frequency.getIntervalHours();
        LocalDateTime now = LocalDateTime.now();

        // Converts fractional hours into minutes
        long intervalMinutes = (long) (intervalHours * 60);

        while (startDateTime.isBefore(now)) {
            startDateTime = startDateTime.plusHours(intervalMinutes);
        }

        return startDateTime;
    }
    public void setNextDosageTime(LocalDateTime nextDosageTime) {
        this.nextDosageTime = nextDosageTime;
    }
}
