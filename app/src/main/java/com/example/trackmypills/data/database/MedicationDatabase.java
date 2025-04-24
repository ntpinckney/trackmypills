package com.example.trackmypills.data.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.trackmypills.models.Medication;
import com.example.trackmypills.util.Converters;

@Database(entities = {Medication.class}, version = 10, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class MedicationDatabase extends RoomDatabase {

    private static volatile MedicationDatabase INSTANCE;

    public abstract MedicationDao medicationDao();

    public static MedicationDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (MedicationDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            MedicationDatabase.class,
                            "medication_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}