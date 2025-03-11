package com.example.trackmypills;

import androidx.room.TypeConverter;

public class Converters {

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

    // Converts ReminderTime to String
    @TypeConverter
    public static String fromReminderTime(Frequency frequency){
        return frequency == null ? null: frequency.name();
    }

    // Converts String to ReminderTime
    @TypeConverter
    public static Frequency toReminderTime(String reminderTime){
        return reminderTime == null ? null: Frequency.valueOf(reminderTime);
    }
}
