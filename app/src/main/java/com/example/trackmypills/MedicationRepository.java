package com.example.trackmypills;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MedicationRepository {
    private MedicationDao medicationDao;
    private ExecutorService executorService;
    private LiveData<List<Medication>> allMedication;

    public MedicationRepository(Application application){
        MedicationDatabase database =
                MedicationDatabase.getInstance(application);
        medicationDao = database.medicationDao();
        executorService = Executors.newSingleThreadExecutor();
        allMedication =  medicationDao.loadAllMedication();
    }

    public LiveData<List<Medication>> loadAllMedication(){
        return allMedication;
    }

    public void insert(Medication medication){
        executorService.execute(() ->
                medicationDao.insertMedication(medication));
    }

    public void update(Medication medication){
        executorService.execute(() ->
                medicationDao.updateMedication(medication));
    }

    public void delete(Medication medication){
        executorService.execute(() -> medicationDao.deleteMedication(medication));
    }

    public LiveData<Medication> getMedicationById(int medicationId) {
        return medicationDao.getMedicationById(medicationId);
    }

    public LiveData getNextDosageTime(int medicationId) {
        return Transformations.map(getNextDosageTime(medicationId), Medication::getNextDosageTime);
    }

}
