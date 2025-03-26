package com.example.trackmypills.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.example.trackmypills.models.Medication;

import java.time.LocalDate;
import java.util.List;

@Dao
public interface MedicationDao {

    // Inserts new medication into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Medication medication);

    // Updates entries with new information
    @Update
    void update(Medication medication);

    // Removes entries from database
    @Delete
    void delete(Medication medication);

    // Shows all medication within the database. LiveData ensures it is updated in real-time
    @Query("SELECT * FROM medications")
    LiveData<List<Medication>> getAllMedication();

    // Gets a medication by its Id. Required for reminders
    @Query("SELECT * FROM medications WHERE id = :medicationId LIMIT 1")
    LiveData<Medication> getMedicationById(int medicationId);
}
