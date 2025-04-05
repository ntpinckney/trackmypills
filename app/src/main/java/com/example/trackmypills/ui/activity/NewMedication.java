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

public class NewMedication extends AppCompatActivity {
    private EditText medNameInput, medQuantityInput, maxAmtInput, totalMedsInput;
    private TextView timeTextView;
    private Spinner adminSpinner, frequencySpinner;
    private MedicationViewModel viewModel;
    private Button saveBtn, cancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_medication);

        // Puts user input into variables for data manipulation
        medNameInput = findViewById(R.id.enter_med_name);
        medQuantityInput = findViewById(R.id.quantity_per_dose);
        maxAmtInput = findViewById(R.id.max_amt_number);
        totalMedsInput = findViewById(R.id.total_meds_number);
        timeTextView = findViewById(R.id.med_time);

        //  Converts enum values into String values and establishes spinners
        adminSpinner = findViewById(R.id.admin_spinner);
        frequencySpinner = findViewById(R.id.freq_spinner);

        saveBtn = findViewById(R.id.save_btn);
        cancelBtn = findViewById(R.id.cancel_btn);

        // Establishes ViewModel
        viewModel = new ViewModelProvider(this).get(MedicationViewModel.class);


        // Populates the spinners
        SpinnerUtil.setUpSpinner(this, adminSpinner, AdminType.values());
        SpinnerUtil.setUpSpinner(this, frequencySpinner, Frequency.values());

        // Shows TimePicker when clicking the EditText
        timeTextView.setOnClickListener(v -> TimePickerUtil.showTimePickerDialog(this, timeTextView));

        // Handles save button click
       saveBtn.setOnClickListener(v -> saveMedication());

       // Handles cancel button click. Returns to MainActivity and makes no changes
        cancelBtn.setOnClickListener(v -> {
            Intent intent = new Intent(NewMedication.this, MainActivity.class);
            startActivity(intent);
            finish();
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void saveMedication() {
        String medNameStr = medNameInput.getText().toString();
        String medQuantityStr = medQuantityInput.getText().toString();
        String maxAmtStr = maxAmtInput.getText().toString();
        String totalMedsStr = totalMedsInput.getText().toString();
        String medTimeStr = timeTextView.getText().toString();

        // Checks if input is valid
        if (medNameStr.isEmpty() || maxAmtStr.isEmpty() || medTimeStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sets the values based on user input
        double medQuantity = Double.parseDouble(medQuantityStr);
        double maxAmount = Double.parseDouble(maxAmtStr);
        double totalOfMeds = Double.parseDouble(totalMedsStr);
        AdminType adminType = AdminType.values()[adminSpinner.getSelectedItemPosition()];
        Frequency frequency = Frequency.values()[frequencySpinner.getSelectedItemPosition()];
        String timeString = timeTextView.getText().toString().trim();

        try {
            // Formats time to h:mm a format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US);
            LocalTime parsedTime = LocalTime.parse(timeString, formatter); // Parses time for database

            LocalDateTime now = LocalDateTime.now(); // Stores the user's current time
            LocalDate today = now.toLocalDate(); // Stores the user's current date

            LocalDateTime scheduledDateTime = LocalDateTime.of(today, parsedTime);

            // If the time has already passed, schedules time to the next day
            if(scheduledDateTime.isBefore(now)){
                scheduledDateTime = scheduledDateTime.plusDays(1);
            }

            LocalTime adjustedDateTime = LocalTime.from(scheduledDateTime);

            Log.d("MedicationTime", "Parsed Time: " + parsedTime + ", Adjusted Time: " + adjustedDateTime);

            Medication medication = new Medication(medNameStr, medQuantity, maxAmount, totalOfMeds, adminType, adjustedDateTime, frequency);
            boolean exceedsMaxAmount = medication.getMedQuantity() > medication.getMaxAmt();
            boolean exceedsTotalMeds = medication.getMedQuantity() > medication.getTotalMeds() ||
                    medication.getMaxAmt() > medication.getTotalMeds();

            // Prevents medQuantity from exceeding maxAmt/totalMeds
            if(exceedsMaxAmount || exceedsTotalMeds){
                InvalidDialogUtil.showInvalidDosageDialog(this);
            } else {
                // Uses ViewModel to insert medication into database
                viewModel.insert(medication);

                Toast.makeText(NewMedication.this, "Medication saved!", Toast.LENGTH_SHORT).show();

                // Adds notification
                NotifUtil.scheduleNotification(this, medication);

                // Returns to MainActivity once saved
                Intent intent = new Intent(NewMedication.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (DateTimeParseException e){
            Log.e("NewMedication", "Invalid time format: " + timeString, e);
            Toast.makeText(this,"Invalid time format! Please use h:mm AM/PM.", Toast.LENGTH_SHORT).show();
        }
    }
}