package com.example.trackmypills.ui.activities;

import android.app.TimePickerDialog;
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
import androidx.room.Room;

import com.example.trackmypills.models.AdminType;
import com.example.trackmypills.models.Frequency;
import com.example.trackmypills.models.Medication;
import com.example.trackmypills.data.database.MedicationDatabase;
import com.example.trackmypills.viewmodel.MedicationViewModel;
import com.example.trackmypills.R;
import com.example.trackmypills.utils.NotifUtil;
import com.example.trackmypills.utils.SpinnerUtil;
import com.example.trackmypills.utils.TimePickerUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Locale;

public class NewMedication extends AppCompatActivity {
    private EditText medNameInput, maxAmtInput;
    private TextView timeTextView;
    private Spinner adminSpinner, frequencySpinner;
    private MedicationDatabase db;
    private String selectedTime = LocalTime.now().toString();
    private MedicationViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_medication);

        medNameInput = findViewById(R.id.enter_med_name);
        maxAmtInput = findViewById(R.id.max_amt_number);
        timeTextView = findViewById(R.id.med_time);

        //  Converts enum values into String values and establishes spinners
        adminSpinner = findViewById(R.id.admin_spinner);
        frequencySpinner = findViewById(R.id.freq_spinner);

        // Establishes ViewModel
        viewModel = new ViewModelProvider(this).get(MedicationViewModel.class);

        Button confirmBtn = findViewById(R.id.confirmBtn);

        db = Room.databaseBuilder(getApplicationContext(),
                MedicationDatabase.class, "medication_db")
                .fallbackToDestructiveMigration()
                .build();

        // Populates the spinners
        SpinnerUtil.setUpSpinner(this, adminSpinner, AdminType.values());
        SpinnerUtil.setUpSpinner(this, frequencySpinner, Frequency.values());

        // Show TimePicker when clicking the EditText
        timeTextView.setOnClickListener(v -> TimePickerUtil.showTimePickerDialog(this, timeTextView));

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

                    // Converts to 12-hour format
                    String amPm = (selectedHour >= 12) ? "PM" : "AM";
                    int hour12 = (selectedHour == 0) ? 12 : (selectedHour > 12 ?
                            selectedHour - 12 : selectedHour);
                    String formattedTime = String.format("%02d:%02d %s", hour12,
                            selectedMinute, amPm);

                    timeTextView.setText(formattedTime); // Updates UI
                },
                hour, minute, false); // False for 12-hour format
        timePickerDialog.show();
    }



    private void saveMedication() {
        String medName = medNameInput.getText().toString();
        String maxAmtStr = maxAmtInput.getText().toString();
        String medTime = timeTextView.getText().toString();

        // Checks if input is valid
        if (medName.isEmpty() || maxAmtStr.isEmpty() || medTime.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sets the values based on user input
        int maxAmount = Integer.parseInt(maxAmtStr);
        AdminType adminType = AdminType.values()[adminSpinner.getSelectedItemPosition()];
        Frequency frequency = Frequency.values()[frequencySpinner.getSelectedItemPosition()];
        String timeString = timeTextView.getText().toString().trim();

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US);
            LocalTime parsedTime = LocalTime.parse(timeString, formatter);

            LocalDateTime now = LocalDateTime.now();
            LocalDate today = now.toLocalDate();

            LocalDateTime scheduledDateTime = LocalDateTime.of(today, parsedTime);

            if(scheduledDateTime.isBefore(now)){
                scheduledDateTime = scheduledDateTime.plusDays(1);
            }

            LocalTime adjustedDateTime = LocalTime.from(scheduledDateTime);

            Log.d("MedicationTime", "Parsed Time: " + parsedTime + ", Adjusted Time: " + adjustedDateTime);

            Medication medication = new Medication(medName, maxAmount, adminType, adjustedDateTime, frequency);
            // Uses ViewModel to insert medication into database
            viewModel.insert(medication);

            Toast.makeText(NewMedication.this, "Medication saved!", Toast.LENGTH_SHORT).show();
            // Adds notification
            NotifUtil.scheduleNotification(this, medication);

            // Returns to MainActivity once saved
            Intent intent = new Intent(NewMedication.this, MainActivity.class);
            startActivity(intent);
            finish();

        } catch (DateTimeParseException e){
            Log.e("NewMedication", "Invalid time format: " + timeString, e);
            Toast.makeText(this,"Invalid time format! Please use h:mm AM/PM.", Toast.LENGTH_SHORT).show();
        }

    }

}