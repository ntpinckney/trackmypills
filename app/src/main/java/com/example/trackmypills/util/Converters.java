package com.example.trackmypills.util;

import androidx.room.TypeConverter;

import com.example.trackmypills.models.AdminType;
import com.example.trackmypills.models.Frequency;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Converters {


    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;


    //Converts AdminType to String
    @TypeConverter
    public static String fromAdminType(AdminType adminType) {
        return adminType == null ? null : adminType.name();
    }

    // Converts String to AdminType
    @TypeConverter
    public AdminType toAdminType(String adminType) {
        return adminType == null ? null : AdminType.valueOf(adminType);
    }

    // Converts Frequency to String
    @TypeConverter
    public static String fromFrequency(Frequency frequency) {
        return frequency == null ? null : frequency.name();
    }

    // Converts String to Frequency
    @TypeConverter
    public static Frequency toFrequency(String frequency) {
        return frequency == null ? null : Frequency.valueOf(frequency);
    }

    // Converts DateTime to String
    @TypeConverter
    public static String toStringDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(timeFormatter);
    }

    //Converts String to DateTime
    @TypeConverter
    public static LocalDateTime fromStringDateTime(String value) {
        if (value == null) {
            return null;
        }

        LocalTime parsedTime = LocalTime.parse(value, timeFormatter);
        return LocalDateTime.of(LocalDate.now(), parsedTime);
    }

    // Converts Time to String
    @TypeConverter
    public static String toStringTime(LocalTime time) {
        return time == null ? null : time.format(timeFormatter);
    }

    //Converts String to Time
    @TypeConverter
    public static LocalTime fromStringTime(String value) {
        return value == null ? null : LocalTime.parse(value, timeFormatter);
    }


    // Converts Date to String
    @TypeConverter
    public static String toStringDate(LocalDate date) {
        return date == null ? null : date.format(dateFormatter);
    }

    // Converts String to Date
    @TypeConverter
    public static LocalDate fromStringDate(String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    // Converts List<LocalDateTime> to JSON/comma-separated string
    @TypeConverter
    public static String fromLocalDateTimeList(List<LocalDateTime> dateTimeList) {
        if (dateTimeList == null) {
            return null;
        }
        return dateTimeList.stream()
                .map(dateTime -> dateTime.format(timeFormatter))
                .collect(Collectors.joining(","));
    }

    // Converts String back to List<LocalDateTime>
    @TypeConverter
    public static List<LocalDateTime> toLocalDateTimeList(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return new ArrayList<>();
        }
        String[] dateTimeStrings = dateTimeString.split(",");
        List<LocalDateTime> dateTimeList = new ArrayList<>();
        for (String dateTimeStr : dateTimeStrings) {
            LocalTime time = LocalTime.parse(dateTimeStr, timeFormatter);
            dateTimeList.add(LocalDateTime.of(LocalDate.now(), time));
        }
        return dateTimeList;
    }
}
