package com.example.trackmypills;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class MedicationViewModel extends AndroidViewModel {
    private MedicationRepository repository;
    private LiveData<List<Medication>> allMedication;

    public MedicationViewModel(Application application) {
        super(application);
        repository = new MedicationRepository(application);
        allMedication = repository.loadAllMedication();

    }

    public LiveData<List<Medication>> getAllMedication() {
        return allMedication;
    }

    public void insert(Medication medication){
        repository.insert(medication);
    }

    public void update(Medication medication){
        repository.update(medication);
    }

    public void delete(Medication medication){
        repository.delete(medication);
    }

    public LiveData getNextDosageTime(int medicationId) {
        return repository.getNextDosageTime(medicationId);
    }

}
