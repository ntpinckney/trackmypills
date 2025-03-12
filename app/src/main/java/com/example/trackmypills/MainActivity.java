package com.example.trackmypills;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;


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

        adapter =  new MedicationAdapter(new ArrayList<>(), medication -> {
            new Thread(() -> {
                // If the doses taken does not exceed the max amount, increment and add to database
                if(medication.dosesTaken < medication.maxAmt){
                    medication.setDosesTaken(medication.dosesTaken++);
                    db.medicationDao().updateMedication(medication);

                    runOnUiThread(this::refreshMedicationList);
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Max dose reached", Toast.LENGTH_SHORT)
                                    .show()
                    );
                }
            }).start();
        }, db.medicationDao());

        recyclerView.setAdapter(adapter);

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
    private void refreshMedicationList(){
        db.medicationDao().loadAllMedication().observe(this, medications -> {
            adapter.setMedications(medications);
            adapter.notifyDataSetChanged();
        });
    }
}
