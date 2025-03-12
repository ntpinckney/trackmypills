package com.example.trackmypills;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.material.floatingactionbutton.FloatingActionButton;



public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> newMedicationLauncher;
    private MedicationDatabase db;
    private MedicationAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.med_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



        // Initializes the database
        db = Room.databaseBuilder(getApplicationContext(),
        MedicationDatabase.class, "medication_db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        // Registers the activity result launcher
        newMedicationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        refreshMedicationList(); // Reloads data when there is a new medication entry
                    }}
        );

        refreshMedicationList();


        FloatingActionButton fab = findViewById(R.id.add_med_fab);

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NewMedication.class);
            newMedicationLauncher.launch(intent);
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }
    private void refreshMedicationList() {
            db.medicationDao().loadAllMedication().observe(this, medications -> {
                if (adapter == null) {
                    adapter = new MedicationAdapter(medications, db.medicationDao());
                    recyclerView.setAdapter(adapter);
                } else {
                    adapter.setMedications(medications);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }


