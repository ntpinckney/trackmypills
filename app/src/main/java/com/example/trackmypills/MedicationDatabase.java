package com.example.trackmypills;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Medication.class}, version = 2)
@TypeConverters({Converters.class})
public abstract class MedicationDatabase extends RoomDatabase {

    private static volatile MedicationDatabase INSTANCE;

    public abstract MedicationDao medicationDao();

    public static MedicationDatabase getInstance(Context context) {
        if(INSTANCE == null) {
            synchronized (MedicationDatabase.class){
                if (INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),

                            MedicationDatabase.class,
                            "medication_database").fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }



}
