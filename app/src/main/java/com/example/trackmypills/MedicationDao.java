package com.example.trackmypills;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.time.LocalDate;
import java.util.List;

@Dao
public interface MedicationDao {

    // Inserts new medication into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMedication(Medication medication);

    // Updates entries with new information
    @Update
    public void updateMedication(Medication medication);

    // Removes entries from database
    @Delete
    void deleteMedication(Medication medication);

    // Removes everything from database. DEBUGGING PURPOSES ONLY

    // Shows all medication within the database. LiveData ensures it is updated in real-time
    @Query("SELECT * FROM medications")
    LiveData<List<Medication>> loadAllMedication();

    // Gets a medication by its Id. Required for reminders
    @Query("SELECT * FROM medications WHERE id = :medicationId LIMIT 1")
    LiveData<Medication> getMedicationById(int medicationId);

    @Query("UPDATE medications SET dosesTaken = 0 WHERE lastResetDate < :currentDate")
    void resetDoses(LocalDate currentDate);

}
