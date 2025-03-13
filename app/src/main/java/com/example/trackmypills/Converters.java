package com.example.trackmypills;

import androidx.room.TypeConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Converters {
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;



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
    public static LocalTime fromStringTime(String value) {
        return value == null ? null : LocalTime.parse(value, timeFormatter);
    }

    @TypeConverter
    public static String toStringTime(LocalTime time){
        return time == null ? null : time.format(timeFormatter);
    }

    @TypeConverter
    public static LocalDate fromStringDate(String value){
        return value == null ? null : LocalDate.parse(value);
    }

    @TypeConverter
    public static String toStringDate(LocalDate date){
        return date == null ? null : date.format(dateFormatter);
    }


}
