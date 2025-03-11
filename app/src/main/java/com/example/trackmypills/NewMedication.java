package com.example.trackmypills;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.Locale;

public class NewMedication extends AppCompatActivity {
    private EditText medNameInput, maxAmtInput, medTimeInput;
    private Spinner adminSpinner, frequencySpinner;
    private MedicationDatabase db;
    private String selectedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_medication);

        medNameInput = findViewById(R.id.medName);
        maxAmtInput = findViewById(R.id.maxAmtNumber);
        medTimeInput = findViewById(R.id.medTime);

        //  Converts enum values into String values and establishes spinners
        adminSpinner = findViewById(R.id.adminSpinner);
        frequencySpinner = findViewById(R.id.freqSpinner);
        Button confirmBtn = findViewById(R.id.confirmBtn);

        db = Room.databaseBuilder(getApplicationContext(),
                MedicationDatabase.class, "medication_db")
                .fallbackToDestructiveMigration()
                .build();

        setupSpinners(); // Populates the spinners

        // Show TimePicker when clicking the EditText
        medTimeInput.setOnClickListener(v -> showTimePickerDialog());

        // Handle Confirm button click
        confirmBtn.setOnClickListener(v -> saveMedication());


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showTimePickerDialog(){
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    medTimeInput.setText(selectedTime); // Set the selected time in the EditText
                },
                hour, minute, true); // true for 24-hour format, change to false for AM/PM
        timePickerDialog.show();
    }

    private void setupSpinners(){
        String[] adminMethods = new String[AdminType.values().length];
        for(int i = 0; i < AdminType.values().length; i++){
            adminMethods[i] = AdminType.values()[i].getLabel();
        }

        // Creates an ArrayAdapter
        ArrayAdapter<String> adminAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                adminMethods
        );

        adminAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Sets adapter
        adminSpinner.setAdapter(adminAdapter);

        // For Frequency
        String[] frequencyTimes = new String[Frequency.values().length];
        for(int i = 0; i < Frequency.values().length; i++){
            frequencyTimes[i] = Frequency.values()[i].getLabel();
        }


        ArrayAdapter<String> freqAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                frequencyTimes
        );

        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        frequencySpinner.setAdapter(freqAdapter);
    }


    private void saveMedication(){
        String medName = medNameInput.getText().toString();
        String maxAmtStr = maxAmtInput.getText().toString();
        String medTime = medTimeInput.getText().toString();

        // Checks if input is valid
        if(medName.isEmpty() || maxAmtStr.isEmpty() || medTime.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int maxAmount = Integer.parseInt(maxAmtStr);
        AdminType adminType = AdminType.values()[adminSpinner.getSelectedItemPosition()];
        Frequency frequency = Frequency.values()[frequencySpinner.getSelectedItemPosition()];
        LocalTime time = LocalTime.parse(medTime);

        Medication medication = new Medication(medName, maxAmount, adminType, time, frequency);

        new Thread(() -> {
            db.medicationDao().insertMedication(medication);
            runOnUiThread(() -> {
                Toast.makeText(NewMedication.this, "Medication saved!", Toast.LENGTH_SHORT).show();

                // Returns to MainActivity after saving
                Intent intent = new Intent(NewMedication.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Closes NewMedication Activity
            });
        }).start();
    }
}