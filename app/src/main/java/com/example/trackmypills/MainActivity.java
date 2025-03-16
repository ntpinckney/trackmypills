package com.example.trackmypills;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalTime;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> newMedicationLauncher;
    private MedicationDatabase db;
    private MedicationAdapter adapter;
    private RecyclerView recyclerView;

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


        // Initializes the database
        db = Room.databaseBuilder(getApplicationContext(),
                        MedicationDatabase.class, "medication_db")
                .allowMainThreadQueries()
                .build();

        adapter = new MedicationAdapter(new ArrayList<>(), db.medicationDao(), medication -> {
            Intent intent = new Intent(MainActivity.this, EditMedication.class);
            intent.putExtra("medication_id", medication.getId());
            editMedicationLauncher.launch(intent);
        });

        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.add_med_fab);

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NewMedication.class);
            newMedicationLauncher.launch(intent);
        });

        createNotificationChannel(); // Calls the NotificationChannel function

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        reload();
    }

    // Forces RecyclerView to reload the medication list via database
    private void reload(){
        db.medicationDao().loadAllMedication().observe(this, medications -> {
            if(medications != null) {
                for(Medication medication: medications){
                    if(LocalTime.now().isAfter(medication.getNextDosageTime())) {
                        medication.setDosesTaken(0);
                        db.medicationDao().update(medication);
                    }
                }
            }
            adapter.setMedications(medications);
            adapter.notifyDataSetChanged();
        });
    }

    // Establishes notifications
    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "MedicationsReminders";
            String description = "Medication reminders based on time";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("medication_channel", name, importance);
            channel.setDescription(description);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}