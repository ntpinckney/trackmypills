package com.example.trackmypills;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class MedicationViewModel extends AndroidViewModel{
    private final MedicationRepository repository;
    private final LiveData<List<Medication>> allMedication;

    public MedicationViewModel(Application application) {
        super(application);
        repository = new MedicationRepository(application);
        allMedication = repository.getAllMedication();
    }

    public LiveData<List<Medication>> getAllMedication() {
        return allMedication;
    }

    public LiveData<Medication> getMedicationById(int medId){
        return repository.getMedicationById(medId);
    }

    public void insert(Medication medication) {
        repository.insert(medication);
    }

    public void update(Medication medication){
        repository.update(medication);
    }


    public void delete(Medication medication){
        repository.delete(medication);
    }





}
