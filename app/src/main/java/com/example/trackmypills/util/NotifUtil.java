package com.example.trackmypills.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.example.trackmypills.models.Medication;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class NotifUtil {

    public static void scheduleNotification(Context context, Medication medication) {
        // Checks if the app has permissions to send notifications
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            // If not, it requests the permission
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            return; // Exits if the permission is not granted
        }

        // Checks if the app has permission to schedule exact alarms (Android +12)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (context.checkSelfPermission(Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
                // If not, prompt the user to grant permission
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                ((Activity) context).startActivityForResult(intent, 102);
                return; // Exit if permission isn't granted
            }
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("med_name", medication.getName());

        int intervalHours = medication.getFrequency().getIntervalHours();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDoseTime = medication.getNextDosageTime();

        // Allows application to schedule multiple notifications per interval
        for (int i = 0; i < 24 / intervalHours; i++) {
            LocalDateTime nextDoseTime = firstDoseTime.plusHours(i * intervalHours);

            // Makes sure notifications are only scheduled for the future and skips past times
            if (nextDoseTime.isBefore(now)) {
                continue;
            }
            long triggerTime =
                    nextDoseTime
                            .atZone(ZoneId.systemDefault()) // Scheduling based on target device's time zone
                            .toInstant()
                            .toEpochMilli();

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    medication.getId() + i, // Unique request code for each notification
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                Log.d("NotifUtil", "Notification scheduled for " + medication.getName() + " at " + nextDoseTime);
            } else {
                Log.e("NotifUtil", "AlarmManager is null!");
            }
        }
    }

    public static void cancelNotification(Context context, Medication medication) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                medication.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d("NotifUtil", "Notification canceled for " + medication.getName());
        } else {
            Log.e("NotifUtil", "AlarmManager is null!");
        }
    }
}
