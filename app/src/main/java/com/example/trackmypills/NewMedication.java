package com.example.trackmypills;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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
    private EditText medNameInput, maxAmtInput;
    private TextView medTimeView;
    private Spinner adminSpinner, frequencySpinner;
    private MedicationDatabase db;
    private String selectedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_medication);

        medNameInput = findViewById(R.id.enter_med_name);
        maxAmtInput = findViewById(R.id.max_amt_number);
        medTimeView = findViewById(R.id.med_time);

        //  Converts enum values into String values and establishes spinners
        adminSpinner = findViewById(R.id.admin_spinner);
        frequencySpinner = findViewById(R.id.freq_spinner);

        Button confirmBtn = findViewById(R.id.confirmBtn);

        db = Room.databaseBuilder(getApplicationContext(),
                MedicationDatabase.class, "medication_db")
                .fallbackToDestructiveMigration()
                .build();

        // Populates the spinners
        SpinnerUtil.setUpSpinner(this, adminSpinner, AdminType.values());
        SpinnerUtil.setUpSpinner(this, frequencySpinner, Frequency.values());

        // Show TimePicker when clicking the EditText
        medTimeView.setOnClickListener(v -> showTimePickerDialog());

        // Handle Confirm button click
        confirmBtn.setOnClickListener(v -> saveMedication());


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    medTimeView.setText(selectedTime); // Updates UI
                },
                hour, minute, true); // True for 24-hour format
        timePickerDialog.show();
    }



    private void saveMedication(){
        String medName = medNameInput.getText().toString();
        String maxAmtStr = maxAmtInput.getText().toString();
        String medTime = medTimeView.getText().toString();

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

                setResult(Activity.RESULT_OK);

                finish(); // Closes NewMedication Activity
            });
        }).start();
    }
}