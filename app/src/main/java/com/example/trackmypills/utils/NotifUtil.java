package com.example.trackmypills.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("med_name", medication.getName());

        int intervalHours = medication.getFrequency().getIntervalHours();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDoseTime = medication.getNextDosageTime();

        /* Allows application to schedule multiple notifications throughout the day
        Example: if the medication is taken every 6 hours, the loop will run 4 times.
        24 / 6 = 4.
         */
        for (int i = 0; i < 24 / intervalHours; i++) {
            LocalDateTime nextDoseTime = firstDoseTime.plusHours(i * intervalHours);
            /* Makes sure notifications are only scheduled for the future and skips past times
            Example: if user schedules medication for 3:00PM (Wednesday), and it's currently 3:02PM,
            the scheduling for 3:00PM starts on Thursday. However, if it is 2:57PM (Wednesday), the scheduling
            will start on 3:00PM (Wednesday) as that time has not passed yet.
             */
            if (nextDoseTime.isBefore(now)) {
                continue;
            }
            long triggerTime =
                    nextDoseTime
                            .atZone(ZoneId.systemDefault()) // Scheduling is based on target device's time zone
                            .toInstant()
                            .toEpochMilli();

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    medication.getId() + i, // Provides a unique request code for each notification
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
        }
    }
}
