package com.example.trackmypills.ui.activities;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackmypills.models.Medication;
import com.example.trackmypills.ui.adapter.MedicationAdapter;
import com.example.trackmypills.data.database.MedicationDatabase;
import com.example.trackmypills.viewmodel.MedicationViewModel;
import com.example.trackmypills.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    private MedicationDatabase db;
    private RecyclerView recyclerView;
    private MedicationViewModel viewModel;
    private MedicationAdapter adapter;

    private final ActivityResultLauncher<Intent> newMedicationLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    reload(); // Reloads the medication list after adding a new one
                }
            });

    private final ActivityResultLauncher<Intent> editMedicationLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    reload(); // Reloads data when returning to MainActivity
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.med_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initializes ViewModel
        viewModel = new ViewModelProvider(this).get(MedicationViewModel.class);

        // Initializes Adapter
        adapter = new MedicationAdapter(new ArrayList<>(), viewModel, medication -> {
            Intent intent = new Intent(MainActivity.this, EditMedication.class);
            intent.putExtra("medication_id", medication.getId());
            editMedicationLauncher.launch(intent);
        });

        recyclerView.setAdapter(adapter);




        // Observes medication list
        viewModel.getAllMedication().observe(this, medications -> {
            if (medications != null) {
                adapter.setMedications(medications);
                adapter.notifyDataSetChanged();
            }
        });

        FloatingActionButton fab = findViewById(R.id.add_med_fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NewMedication.class);
            newMedicationLauncher.launch(intent); // Make sure this is initialized
        });

        createNotificationChannel();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // Forces reload of medication list via ViewModel
    private void reload() {
        viewModel.getAllMedication().observe(this, medications -> {
            if (medications != null) {
                LocalDateTime now = LocalDateTime.now(); // Gets full current date and time

                for (Medication medication : medications) {
                    if (now.isAfter(medication.getNextDosageTime())) { // Compares LocalDateTime
                        medication.setDosesTaken(0);

                        // Updates in background thread
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(() -> db.medicationDao().update(medication));
                    }
                }
            }
        });
    }


    // Establishes notifications
            private void createNotificationChannel () {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    //  Allows users to enable alarm notifications through settings
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    if (!alarmManager.canScheduleExactAlarms()) {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                            Toast.makeText(this, "Enable 'Schedule Exact Alarms' in settings",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("NotifUtil", "Cannot find NotifUtil");

                        }
                    }

                    // Creates notification channel
                    CharSequence name = "MedicationsReminders";
                    String description = "Medication reminders based on time";
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    NotificationChannel channel = new NotificationChannel("medication_channel", name, importance);
                    channel.setDescription(description);
                    channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

                    //Gets notification manager and creates channel
                    NotificationManager manager = getSystemService(NotificationManager.class);
                    if (manager != null) {
                        manager.createNotificationChannel(channel);
                    }
                }
            }
        }