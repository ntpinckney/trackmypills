package com.example.trackmypills;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class NotifUtil {

    public static void scheduleNotification(Context context, Medication medication) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("medication_name", medication.getName());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                medication.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextDoseTime = LocalDateTime.of(LocalDate.now(), medication.getNextDosageTime());

        long triggerTime = nextDoseTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
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