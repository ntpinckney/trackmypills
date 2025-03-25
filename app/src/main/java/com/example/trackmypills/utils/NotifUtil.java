package com.example.trackmypills.utils;

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
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("medication_name", medication.getName());

        int intervalHours = medication.getFrequency().getIntervalHours();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDoseTime = medication.getNextDosageTime();

        /* Allows application to schedule multiple notifications throughout the day
        Example: If the medication is taken every 6 hours, the loop will run 4 times.
        24 / 6 = 4.
         */
        for (int i = 0; i < 24 / intervalHours; i++) {
            LocalDateTime nextDoseTime = firstDoseTime.plusHours(i * intervalHours);
            // Makes sure notifications are only scheduled for the future and skips past times
            if (nextDoseTime.isBefore(now)) {
                continue;
            }
            long triggerTime =
                    nextDoseTime
                            .atZone(ZoneId.systemDefault())
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