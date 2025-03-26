package com.example.trackmypills.utils;

import android.app.AlertDialog;
import android.content.Context;

public class InvalidDialogUtil {

    // Sends a pop-up message if dosage exceeds max/total amount
    public static void showInvalidDosageDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Invalid Dosage")
                .setMessage("The dose/max amount cannot be greater than the maximum/total amount.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}

