package com.example.trackmypills.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
import com.example.trackmypills.util.NotifUtil;
import com.example.trackmypills.viewmodel.MedicationViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

        SharedPreferences sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        int savedTheme = sharedPreferences.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedTheme);

        setContentView(R.layout.activity_main);

        createNotificationChannel();
        showNotificationPermissionDialog(this);

        Switch ldSwitch; // Toggle for light/dark mode

        ldSwitch = findViewById(R.id.ld_switch);

        int currentNightMode = AppCompatDelegate.getDefaultNightMode();
        // Automatically sets switch to dark mode if enabled
        ldSwitch.setChecked(currentNightMode == AppCompatDelegate.MODE_NIGHT_YES);

        // Switches to light/dark mode, depending on the position of the toggle
        ldSwitch.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            SharedPreferences sharedPrefs = getSharedPreferences("settings", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPrefs.edit();
                                            if (ldSwitch.isChecked()) {
                                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); // Sets the theme
                                                editor.putInt("theme", AppCompatDelegate.MODE_NIGHT_YES); // Saves the theme
                                            } else {
                                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                                editor.putInt("theme", AppCompatDelegate.MODE_NIGHT_NO);
                                            }
                                            editor.apply(); // Saves changes
                                        }
                                    }
        );

        // Initializes recyclerView
        recyclerView = findViewById(R.id.med_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initializes ViewModel
        viewModel = new ViewModelProvider(this).get(MedicationViewModel.class);


        // Initializes adapter
        adapter = new MedicationAdapter(new ArrayList<>(), viewModel, medication -> {
            Intent intent = new Intent(MainActivity.this, EditMedication.class);
            intent.putExtra("medication_id", medication.getId());
            editMedicationLauncher.launch(intent);
        });

        recyclerView.setAdapter(adapter);



        // Sets spacing between medication entries
        int spacing = getResources().getDimensionPixelSize(R.dimen.medication_item_spacing);
        recyclerView.addItemDecoration(new MedicationItemDecoration(spacing));

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
            newMedicationLauncher.launch(intent);
        });


        new Handler().postDelayed(() -> showBatteryOptimizationDialog(this), 500);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // For spacing between medication entries
    public static class MedicationItemDecoration extends RecyclerView.ItemDecoration {
        private final int spacing;
        public MedicationItemDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(
                Rect outRect,
                @NonNull View view,
                @NonNull RecyclerView parent,
                @NonNull RecyclerView.State state) {
            outRect.top = spacing;
            outRect.bottom = spacing;
            outRect.left = spacing;
            outRect.right = spacing;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private boolean requestingNotification = false;
    private boolean requestingExactAlarm = false;
    private boolean requestingBatteryOpt = false;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) { // Notification permissions
            requestingNotification = false;
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                proceedToNextPermissionStep();
            } else {
                Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 102) { // Exact alarm permissions
            requestingExactAlarm = false;
            // SCHEDULE_EXACT_ALARM is special. Might not be granted directly from dialog
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                if (alarmManager.canScheduleExactAlarms()) {
                    proceedToNextPermissionStep(); // Proceeds to next step
                } else {
                    Toast.makeText(this, "Exact alarm permission denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private boolean allPermissionsHandledToastShown = false;

    private void proceedToNextPermissionStep() {
        // Checks for notification permission
        if (!hasNotificationPermission() && !requestingNotification) {
            requestingNotification = true;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            return;
        }

        // Checks for exact alarm permission
        if (!hasExactAlarmPermission() && !requestingExactAlarm) {
            requestingExactAlarm = true;
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);

            // Adds a small delay before checking again
            new Handler().postDelayed(this::proceedToNextPermissionStep, 1000);
            return;
        }

        // Checks for battery optimization
        if (!isBatteryOptimizationIgnored(this) && !requestingBatteryOpt) {
            requestingBatteryOpt = true;
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            startActivity(intent);

            new Handler().postDelayed(this::proceedToNextPermissionStep, 1000);
            return;
        }

        // Shows toast only once when all permissions are handled
        if (!allPermissionsHandledToastShown) {
            allPermissionsHandledToastShown = true;
            Toast.makeText(this, "All permissions handled!", Toast.LENGTH_SHORT).show();
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
        // Delays a bit to allow user to grant permissions
        new Handler().postDelayed(this::proceedToNextPermissionStep, 700);
    }

    // Creates notification channel
    public void createNotificationChannel() {
        // Android 8+ permissions and notification channel
        // TODO: Make louder and more prominent notifications
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

        // Skip if the dialog shouldn't be shown again or if battery optimization is already ignored
        if (dontShowAgain || isBatteryOptimizationIgnored(context)) {
            return;
        }

        // Checks if the app has necessary permissions
        boolean hasNotificationPermissions = ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
        boolean hasExactAlarmPermission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.SCHEDULE_EXACT_ALARM)
                == PackageManager.PERMISSION_GRANTED;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Optimize Notifications");

        // Custom message to explain what the app needs
        StringBuilder message = new StringBuilder("To ensure timely reminders and alarms, please grant the following permissions:\n\n");
        if (!hasNotificationPermissions) {
            message.append("1. Allow the app to send notifications.\n");
        }
        if (!hasExactAlarmPermission) {
            message.append("2. Allow the app to schedule exact alarms.\n");
        }
        if (!isBatteryOptimizationIgnored(context)) {
            message.append("3. Disable battery optimization for the app.\n");
        }

        builder.setMessage(message.toString());

        builder.setPositiveButton("Go to Settings", (dialog, which) -> {
            proceedToNextPermissionStep();
        });


        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.setNeutralButton("Don't Show Again", (dialog, which) -> {
            prefs.edit().putBoolean("dont_show_notification_dialog", true).apply();
            dialog.dismiss();
        });

        builder.show();
    }

    private boolean isBatteryOptimizationIgnored(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return pm.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return true; // Assumes battery optimization isn't an issue on older devices
    }

    private boolean hasNotificationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true; // Assumes it is granted on older versions
    }
}