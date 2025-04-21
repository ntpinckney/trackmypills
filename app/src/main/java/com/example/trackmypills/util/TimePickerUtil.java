package com.example.trackmypills.util;

import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.TextView;

import java.time.LocalTime;
import java.util.Calendar;

public class TimePickerUtil {

    public static void showTimePickerDialog(Context context, TextView textView,
                                            LocalTime prefillTime) {

        int hour = prefillTime.getHour();
        int minute = prefillTime.getMinute();

        TimePickerDialog timePickerDialog = new TimePickerDialog(context,
                (view, selectedHour, selectedMinute) -> {

                    // Converts to 12-hour format and switches to AM or PM, depending on the hour
                    String amPm = (selectedHour >= 12) ? "PM" : "AM";
                    int hour12 = (selectedHour == 0) ? 12 : (selectedHour > 12 ?
                            selectedHour - 12 : selectedHour);
                    String formattedTime = String.format("%02d:%02d %s", hour12,
                            selectedMinute, amPm);

                    textView.setText(formattedTime); // Updates UI
                },
                hour, minute, false); // False for 12-hour format
        timePickerDialog.show();
    }
}
