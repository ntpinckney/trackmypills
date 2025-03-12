package com.example.trackmypills;

import androidx.room.TypeConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Converters {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;

    //Converts AdminType to String
    @TypeConverter
    public static String fromAdminType(AdminType adminType){
        return adminType == null ? null: adminType.name();
    }

    // Converts String to AdminType
    @TypeConverter
    public AdminType toAdminType(String adminType){
        return adminType == null ? null: AdminType.valueOf(adminType);
    }

    // Converts Frequency to String
    @TypeConverter
    public static String fromFrequency(Frequency frequency){
        return frequency == null ? null: frequency.name();
    }

    // Converts String to Frequency
    @TypeConverter
    public static Frequency toFrequency(String frequency){
        return frequency == null ? null: Frequency.valueOf(frequency);
    }


    @TypeConverter
    public static LocalTime fromString(String value) {
        return value == null ? null : LocalTime.parse(value, formatter);
    }

    @TypeConverter
    public static String toString(LocalTime time){
        return time == null ? null : time.format(formatter);
    }

}
