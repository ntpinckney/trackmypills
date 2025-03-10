package com.example.trackmypills;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Medication.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract MedicationDao medicationDao();

    public static AppDatabase getInstance(Context context) {
        if(INSTANCE == null) {
            synchronized (AppDatabase.class){
                if (INSTANCE == null){
                    Room.databaseBuilder(
                            context.getApplicationContext(),

                            AppDatabase.class,
                            "medication_database").fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }



}
