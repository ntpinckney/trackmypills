package com.example.trackmypills.utils;

import androidx.room.TypeConverter;

import com.example.trackmypills.models.AdminType;
import com.example.trackmypills.models.Frequency;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Converters {
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
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
    public static String toStringDateTime(LocalDateTime dateTime){
        return dateTime == null ? null : dateTime.format(timeFormatter);
    }

    @TypeConverter
    public static LocalDateTime fromStringDateTime(String value) {
        if (value == null) {
            return null;
        }

        LocalTime parsedTime = LocalTime.parse(value, timeFormatter);
        return LocalDateTime.of(LocalDate.now(), parsedTime);
    }


    @TypeConverter
    public static String toStringTime(LocalTime time){
        return time == null ? null : time.format(timeFormatter);
    }


    @TypeConverter
    public static LocalTime fromStringTime(String value){
        return value == null ? null : LocalTime.parse(value);
    }

    @TypeConverter
    public static String toStringDate(LocalDate date){
        return date == null ? null : date.format(dateFormatter);
    }

    @TypeConverter
    public static LocalDate fromStringDate(String value){
        return value == null ? null : LocalDate.parse(value);
    }


}
