package com.example.trackmypills.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackmypills.R;
import com.example.trackmypills.models.Medication;
import com.example.trackmypills.ui.adapter.MedicationAdapter;
import com.example.trackmypills.data.database.MedicationDatabase;
import com.example.trackmypills.viewmodel.MedicationViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    private MedicationDatabase db;
    private RecyclerView recyclerView;
    private MedicationViewModel viewModel;
    private MedicationAdapter adapter;

    private final ActivityResultLauncher<Intent> newMedicationLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    reload(); // Reloads the medication list after adding a new one
                }
            });

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

        // Initializes ViewModel
        viewModel = new ViewModelProvider(this).get(MedicationViewModel.class);

        // Initializes Adapter
        adapter = new MedicationAdapter(new ArrayList<>(), viewModel, medication -> {
            Intent intent = new Intent(MainActivity.this, EditMedication.class);
            intent.putExtra("medication_id", medication.getId());
            editMedicationLauncher.launch(intent);
        });

        recyclerView.setAdapter(adapter);

        // Observes medication list
        viewModel.getAllMedication().observe(this, medications -> {
            if (medications != null) {
                adapter.setMedications(medications);
                adapter.notifyDataSetChanged();
            }
        });

        FloatingActionButton fab = findViewById(R.id.add_med_fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NewMedication.class);
            newMedicationLauncher.launch(intent); // Make sure this is initialized
        });


        new Handler().postDelayed(() -> showBatteryOptimizationDialog(this), 500);
        createNotificationChannel();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        } else if (requestCode == 102) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        }
    }

    // Forces reload of medication list via ViewModel
    private void reload() {
        viewModel.getAllMedication().observe(this, medications -> {
            if (medications != null) {
                LocalDateTime now = LocalDateTime.now(); // Gets full current date and time

                for (Medication medication : medications) {
                    if (now.isAfter(medication.getNextDosageTime())) { // Compares LocalDateTime
                        medication.setDosesTaken(0);

                        // Updates in background thread
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(() -> db.medicationDao().update(medication));
                    }
                }
            }
        });
    }

    // Establishes notifications
    public void showNotificationPermissionDialog(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+ permissions
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        // Shows battery optimization permissions dialog before notification dialog
        new Handler().postDelayed(() -> showBatteryOptimizationDialog(this), 500);
    }

    public void createNotificationChannel() {
        // Android 8+ permissions and notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "MedicationReminders";
            String description = "Medication reminders based on time.";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("med_channel", name, importance);
            channel.setDescription(description);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            // Retrieves NotificationManager and creates the channel
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d("NotifUtil", "Notification channel created");
            }
        }
    }

    // Shows user dialog and opens optimization permissions under settings
    public void showBatteryOptimizationDialog(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        boolean dontShowAgain = prefs.getBoolean("dont_show_notification_dialog", false);

        // Skips if the dialog shouldn't be shown again
        if (dontShowAgain || isBatteryOptimizationIgnored(context)) {
            return;
        }

        // Checks if the app has notification permissions
        boolean hasNotificationPermissions = ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
        boolean hasExactAlarmPermission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.SCHEDULE_EXACT_ALARM)
                == PackageManager.PERMISSION_GRANTED;

        // If user is missing permissions, or battery dialog isn't disabled, show dialog
        if (!hasNotificationPermissions || !hasExactAlarmPermission || !isBatteryOptimizationIgnored(context)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Optimize Notifications");

            String message = "To ensure timely reminders and alarms, please:\n\n";
            if (!hasNotificationPermissions) {
                message += "Allow the app to send notifications.\n";
            }
            if (!hasExactAlarmPermission) {
                message += "Allow the app to schedule exact alarms.\n";
            }
            if (!isBatteryOptimizationIgnored(context)) {
                message += "Disable battery optimization for the app.\n";
            }

            builder.setMessage(message);

            builder.setPositiveButton("Go to Settings", (dialog, which) -> {
                if (!hasNotificationPermissions) {
                    // Requests notification permission
                    requestNotificationPermission(context);
                }
                if (!hasExactAlarmPermission) {
                    // Requests exact alarm permission
                    requestExactAlarmPermission(context);
                }
                if (!isBatteryOptimizationIgnored(context)) {
                    // Requests ignoring battery optimizations
                    requestIgnoreBatteryOptimizations(context);
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.setNeutralButton("Don't Show Again", (dialog, which) -> {
                prefs.edit().putBoolean("dont_show_notification_dialog", true).apply();
                dialog.dismiss();
            });

            builder.show();
        }
    }

    private void requestNotificationPermission(Context context) {
        // Requests POST_NOTIFICATIONS permission
        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
    }

    private void requestExactAlarmPermission(Context context) {
        // Requests SCHEDULE_EXACT_ALARM permission
        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.SCHEDULE_EXACT_ALARM}, 102);
    }

    public void requestIgnoreBatteryOptimizations(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            context.startActivity(intent);

            // After the user disables battery optimization, request notification permissions
            new Handler().postDelayed(() -> showNotificationPermissionDialog(context), 2000);
        }
    }

    public boolean isBatteryOptimizationIgnored(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return pm.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return true; //  Assumes battery optimization isn't an issue on older devices
    }
}