package com.example.trackmypills;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class EditMedication extends AppCompatActivity {
    private EditText nameInput, maxAmtInput;
    private Spinner adminSpinner, frequencySpinner;
    private TextView timeTextView;
    private Button saveButton, deleteButton;
//    private MedicationDatabase db;
    private Medication medication;

    private MedicationViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_medication);

        nameInput = findViewById(R.id.edit_med_name);
        maxAmtInput = findViewById(R.id.edit_max_amt_number);
        adminSpinner = findViewById(R.id.edit_admin_spinner);
        frequencySpinner = findViewById(R.id.edit_freq_spinner);
        timeTextView = findViewById(R.id.edit_med_time);
        saveButton = findViewById(R.id.save_btn);
        deleteButton = findViewById(R.id.delete_btn);
//        db = Room.databaseBuilder(getApplicationContext(), MedicationDatabase.class, "medication_db")
//                .fallbackToDestructiveMigration()
//                .build();

        // Gets the medication ID from MainActivity's intent
        int medicationId = getIntent().getIntExtra("medication_id", -1);

        // Sets up spinners
        SpinnerUtil.setUpSpinner(this, adminSpinner, AdminType.values());
        SpinnerUtil.setUpSpinner(this, frequencySpinner, Frequency.values());

        // Establishes ViewModel
        viewModel = new ViewModelProvider(this).get(MedicationViewModel.class);

        // Updates information based on user entries and medication ID
        if (medicationId != -1) {
            viewModel.getMedicationById(medicationId).observe(this, fetchedMedication -> {
                if (fetchedMedication != null) {
                    medication = fetchedMedication;
                    nameInput.setText(medication.getName());
                    maxAmtInput.setText(String.valueOf(medication.getMaxAmt()));
                    timeTextView.setText(medication.getNextDosageTime().toString());

                    SpinnerUtil.setSpinnerSelection(adminSpinner, medication.getAdminType().getLabel());
                    SpinnerUtil.setSpinnerSelection(frequencySpinner, medication.getFrequency().getLabel());
                }
            });


            // Saves updated data
            saveButton.setOnClickListener(v -> {
                if (medication != null) {

                    // Displays current information before updating
                    medication.setName(nameInput.getText().toString());
                    medication.setMaxAmt(Integer.parseInt(maxAmtInput.getText().toString()));
                    medication.setNextDosageTime(LocalTime.parse(timeTextView.getText().toString()));

                    medication.setAdminType(AdminType.values()[adminSpinner.getSelectedItemPosition()]);
                    medication.setFrequency(Frequency.values()[frequencySpinner.getSelectedItemPosition()]);

                    // Updates database based on new information
                    viewModel.update(medication);
                    Toast.makeText(this, "Medication updated", Toast.LENGTH_SHORT).show();

                    NotifUtil.scheduleNotification(this, medication); // Schedules notification after update
                    Intent intent = new Intent(EditMedication.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Closes activity and returns to MainActivity
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (medication != null) {
                    new AlertDialog.Builder(this)
                            .setTitle("Delete Medication")
                            .setMessage("Are you sure you want to delete this medication?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                viewModel.delete(medication);
                                Toast.makeText(EditMedication.this, "Medication deleted", Toast.LENGTH_SHORT).show();
                                NotifUtil.cancelNotification(this, medication);
                                Intent intent = new Intent(EditMedication.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            })
                            .setNegativeButton("No", null)
                            .show(); // Closes and returns to MainActivity
                }
            });

            saveButton.setOnClickListener(v -> {
                String timeString = timeTextView.getText().toString();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US);
                try {
                    LocalTime newTime = LocalTime.parse(timeString, formatter);
                    medication.setNextDosageTime(newTime);
                    viewModel.update(medication);

                    Toast.makeText(EditMedication.this, "Medication updated!", Toast.LENGTH_SHORT).show();
                    finish();
                } catch (DateTimeParseException e) {
                    Toast.makeText(EditMedication.this, "Invalid or missing elements. Please try again.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            });


            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }
}