package com.example.trackmypills.util;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.example.trackmypills.data.repository.MedicationRepository;
import com.example.trackmypills.viewmodel.MedicationViewModel;

public class DailyResetReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int medId = intent.getIntExtra("med_id", -1);

        MedicationViewModel viewModel = new ViewModelProvider((ViewModelStoreOwner) context.getApplicationContext()).get(MedicationViewModel.class);

        viewModel.getMedicationById(medId).observeForever(medication -> {
            if (medication != null) {
                DoseManager doseManager = new DoseManager(viewModel);
                doseManager.resetMedicationForNewDay(medication);
                NotifUtil.scheduleNotification(context, medication);
                NotifUtil.scheduleDailyReset(context, medication);
            }
        });
    }
}

