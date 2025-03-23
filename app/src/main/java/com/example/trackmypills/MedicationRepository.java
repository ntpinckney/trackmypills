package com.example.trackmypills;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MedicationRepository {
    private final MedicationDao medicationDao;
    private final ExecutorService executorService;

    public MedicationRepository(Application application){
        MedicationDatabase db = MedicationDatabase.getInstance(application);
        medicationDao = db.medicationDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Medication>> getAllMedication() {
        return medicationDao.getAllMedication();
    }

    public LiveData<Medication> getMedicationById(int medId){
        return medicationDao.getMedicationById(medId);
    }

    public void insert(Medication medication) {
        executorService.execute(() -> medicationDao.insert(medication));
    }

    public void update(Medication medication) {
        executorService.execute(() -> medicationDao.update(medication));
    }

    public void delete(Medication medication) {
        executorService.execute(() -> medicationDao.delete(medication));
    }


}
