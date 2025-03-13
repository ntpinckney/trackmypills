package com.example.trackmypills;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
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
import androidx.room.Room;

import java.time.LocalTime;

public class EditMedication extends AppCompatActivity {
    private EditText nameInput, dosageInput;
    private Spinner adminSpinner, frequencySpinner;
    private TextView timeTextView;
    private Button saveButton, deleteButton;
    private MedicationDatabase db;
    private Medication medication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_medication);

        nameInput = findViewById(R.id.edit_med_name);
        dosageInput = findViewById(R.id.edit_max_amt_number);
        adminSpinner = findViewById(R.id.edit_admin_spinner);
        frequencySpinner = findViewById(R.id.edit_freq_spinner);
        timeTextView = findViewById(R.id.edit_med_time);
        saveButton = findViewById(R.id.save_btn);
        deleteButton = findViewById(R.id.delete_btn);
        db = Room.databaseBuilder(getApplicationContext(), MedicationDatabase.class, "medication_db")
                .allowMainThreadQueries()
                .build();

        // Gets the medication ID from Adapter
        int medicationId = getIntent().getIntExtra("medication_id", -1);

        // Sets up spinners
        SpinnerUtil.setUpSpinner(this, adminSpinner, AdminType.values());
        SpinnerUtil.setUpSpinner(this, frequencySpinner, Frequency.values());

        // Updates information based on user entries and medication ID
        if (medicationId != -1) {
            db.medicationDao().getMedicationById(medicationId).observe(this, medication -> {
                if (medication != null) {
                    nameInput.setText(medication.getName());
                    dosageInput.setText(String.valueOf(medication.getMaxAmt()));
                    timeTextView.setText(medication.getNextDosageTime().toString());

                    SpinnerUtil.setSpinnerSelection(adminSpinner, medication.getAdminType().getLabel());
                    SpinnerUtil.setSpinnerSelection(frequencySpinner, medication.getFrequency().getLabel());
                }
            });

            saveButton.setOnClickListener(v -> {
                if (medication != null) {
                    medication.setName(nameInput.getText().toString());
                    medication.setMaxAmt(Integer.parseInt(dosageInput.getText().toString()));

                    new Thread(() -> {
                        db.medicationDao().updateMedication(medication);

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Medication updated", Toast.LENGTH_SHORT).show(); // 🔹 Added .show()
                            finish();
                        });
                    }).start();
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (medication != null) {
                    new AlertDialog.Builder(this)
                            .setTitle("Delete Medication")
                            .setMessage("Are you sure you want to delete this medication?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                new Thread(() -> {
                                    db.medicationDao().deleteMedication(medication);
                                    runOnUiThread(() -> {
                                        Toast.makeText(this, "Medication deleted",
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                                }).start();
                            })
                            .setNegativeButton("No", null)
                            .show(); // Closes and returns to MainActivity
                }
            });

            timeTextView.setOnClickListener(v -> {
                LocalTime currentTime = medication.getNextDosageTime();
                int hour = currentTime.getHour();
                int minute = currentTime.getMinute();

                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        this,
                        (view, selectedHour, selectedMinute) -> {

                            // Updates TextView
                            LocalTime newTime = LocalTime.of(selectedHour, selectedMinute);

                            //Stores the new time
                            timeTextView.setText(newTime.toString());

                        },
                        hour, minute, true
                );
                timePickerDialog.show();
            });

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }
}