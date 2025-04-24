package com.example.trackmypills.data.database;

import android.content.Context;

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
                            .addMigrations(MIGRATION_9_10)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    public static Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Creates a new table with the updated schema
            database.execSQL("CREATE TABLE IF NOT EXISTS `medications_new` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT, " +
                    "`medQuantity` REAL, " +
                    "`maxAmt` REAL, " +
                    "`totalMeds` REAL, " +
                    "`dosesTaken` REAL, " +
                    "`adminType` TEXT, " +
                    "`startTime` TEXT, " +
                    "`frequency` TEXT, " +
                    "`lastResetDate` TEXT, " +
                    "`notificationsEnabled` INTEGER, " +
                    "`nextDosageTime` TEXT, " +
                    "`missedDosages` TEXT, " +
                    "`takenTimes` TEXT);");

            database.execSQL("INSERT INTO `medications_new` (id, name, medQuantity, maxAmt, totalMeds, dosesTaken, adminType, startTime, frequency, lastResetDate, notificationsEnabled, nextDosageTime, missedDosages, takenTimes) " +
                    "SELECT id, name, medQuantity, maxAmt, totalMeds, dosesTaken, adminType, startTime, frequency, lastResetDate, notificationsEnabled, nextDosageTime, missedDosages, takenTimes FROM `Medication`;");

            // Drops the old table
            database.execSQL("DROP TABLE `medications`;");

            // Renames the new table to the original name
            database.execSQL("ALTER TABLE `medications_new` RENAME TO `medications`;");
        }
    };


}