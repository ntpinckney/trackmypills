package com.example.trackmypills.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.trackmypills.data.repository.MedicationRepository;
import com.example.trackmypills.models.Medication;
import com.example.trackmypills.util.DoseManager;

import java.time.LocalDate;
import java.util.List;

public class MedicationViewModel extends AndroidViewModel{
    private final MedicationRepository repository;
    private final LiveData<List<Medication>> medicationList;

    public MedicationViewModel(Application application) {
        super(application);
        repository = new MedicationRepository(application);
        medicationList = repository.getAllMedication();
    }

    public LiveData<List<Medication>> getAllMedication() {
        return medicationList;
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

    public void refreshMissedDoses(DoseManager doseManager){
        List<Medication> currentList = medicationList.getValue();
        if(currentList != null){
            for(Medication medication : currentList){
                doseManager.checkMissedDoses(medication, LocalDate.now());
            }
        }
    }

}
