package com.example.trackmypills.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import com.example.trackmypills.models.AdminType;
import com.example.trackmypills.models.Frequency;
import com.example.trackmypills.models.Medication;
import com.example.trackmypills.util.InvalidDialogUtil;
import com.example.trackmypills.viewmodel.MedicationViewModel;
import com.example.trackmypills.R;
import com.example.trackmypills.util.NotifUtil;
import com.example.trackmypills.util.SpinnerUtil;
import com.example.trackmypills.util.TimePickerUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class EditMedication extends AppCompatActivity {
    private EditText nameInput, medQuantityInput, maxAmtInput, totalMedInput;
    private Spinner adminSpinner, frequencySpinner;
    private TextView timeTextView;
    private Button saveButton, cancelButton;

    private Medication medication;

    private MedicationViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_medication);

        nameInput = findViewById(R.id.enter_med_name);
        medQuantityInput = findViewById(R.id.quantity_per_dose);
        maxAmtInput = findViewById(R.id.max_amt_number);
        totalMedInput = findViewById(R.id.total_meds_number);

        adminSpinner = findViewById(R.id.admin_spinner);
        frequencySpinner = findViewById(R.id.freq_spinner);
        timeTextView = findViewById(R.id.med_time);
        saveButton = findViewById(R.id.save_btn);
        cancelButton = findViewById(R.id.cancel_btn);

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
                    Log.d("EditMedication", "Fetched time from DB: " + fetchedMedication.getNextDosageTime());
                    medication = fetchedMedication;
                    nameInput.setText(medication.getName());
                    medQuantityInput.setText(String.valueOf(medication.getMedQuantity()));
                    maxAmtInput.setText(String.valueOf(medication.getMaxAmt()));
                    totalMedInput.setText(String.valueOf(medication.getTotalMeds()));

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a ", Locale.US);
                    timeTextView.setText(medication.getNextDosageTime().toLocalTime().format(formatter));

                    SpinnerUtil.setSpinnerSelection(adminSpinner, medication.getAdminType().getLabel());
                    SpinnerUtil.setSpinnerSelection(frequencySpinner, medication.getFrequency().getLabel());
                }
            });

            // Saves updated data
            saveButton.setOnClickListener(v -> {
                if (medication != null) {
                    medication.setName(nameInput.getText().toString());
                    medication.setMedQuantity(Double.parseDouble(medQuantityInput.getText().toString()));
                    medication.setMaxAmt(Double.parseDouble(maxAmtInput.getText().toString()));
                    medication.setTotalMeds(Double.parseDouble(totalMedInput.getText().toString()));

                    // Logs the current time in the TextView
                    String timeString = timeTextView.getText().toString().trim();

                    // If you know, you know
                    Log.d("EditMedication", "rAw TiMe from TextView: [" + timeString + "]");

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US);
                    try {
                        if (!timeString.isEmpty()) {
                            LocalTime newTime = LocalTime.parse(timeString, formatter);
                            Log.d("EditMedication", "Parsed LocalTime: " + newTime);

                            // Updates the start time
                            medication.setStartTime(newTime);

                            // Recalculates the next dosage time based on new start time
                            LocalDateTime newNextDosageTime = medication.getNextDosageTime();
                            medication.setNextDosageTime(newNextDosageTime);

                            Log.d("EditMedication", "Recalculated next dosage time: " + newNextDosageTime);
                        } else {
                            Log.e("EditMedication", "Time string was unexpectedly empty! Using existing time.");
                        }
                    } catch (DateTimeParseException e) {
                        Log.e("EditMedication", "Error parsing time: " + timeString, e);
                        Toast.makeText(EditMedication.this, "Invalid or missing time. Please try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    medication.setAdminType(AdminType.values()[adminSpinner.getSelectedItemPosition()]);
                    medication.setFrequency(Frequency.values()[frequencySpinner.getSelectedItemPosition()]);

                    // Prevents exceeding amounts
                    boolean exceedsMaxAmount = medication.getMedQuantity() > medication.getMaxAmt();
                    boolean exceedsTotalMeds = medication.getMedQuantity() > medication.getTotalMeds() ||
                            medication.getMaxAmt() > medication.getTotalMeds();

                    if (exceedsMaxAmount || exceedsTotalMeds) {
                        InvalidDialogUtil.showInvalidDosageDialog(this);
                    } else {
                        Log.d("EditMedication", "Final medication object before update: " + medication.getNextDosageTime());

                        viewModel.update(medication);

                        Toast.makeText(this, "Medication updated!", Toast.LENGTH_SHORT).show();

                        NotifUtil.scheduleNotification(this, medication);

                        Intent intent = new Intent(EditMedication.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                }
            });

            cancelButton.setOnClickListener(v -> {
                Intent intent = new Intent(EditMedication.this, MainActivity.class);
                startActivity(intent);
                finish();
            });

            timeTextView.setOnClickListener(v -> {
                if (medication != null && medication.getNextDosageTime() != null) {
                    TimePickerUtil.showTimePickerDialog(this, timeTextView, medication.getNextDosageTime().toLocalTime());
                } else {
                    // Current time fallback
                    TimePickerUtil.showTimePickerDialog(this, timeTextView, LocalTime.now());
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