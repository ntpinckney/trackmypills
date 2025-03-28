package com.example.trackmypills.ui.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackmypills.models.Medication;
import com.example.trackmypills.ui.activity.EditMedication;
import com.example.trackmypills.viewmodel.MedicationViewModel;
import com.example.trackmypills.R;
import com.example.trackmypills.utils.NotifUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {
    private List<Medication> medications;
    private OnMedicationClickListener listener; // Callback for item clicks

    private MedicationViewModel viewModel;

    private Context context;

    public interface OnMedicationClickListener {
        void onMedicationClick(Medication medication);
    }

    public MedicationAdapter(List<Medication> medications,
                             MedicationViewModel viewModel, OnMedicationClickListener listener) {
        this.medications = medications;
        this.listener = listener;
        this.viewModel = viewModel;
    }

    public void setMedications(List<Medication> newMedications) {
        if (newMedications != null) {
            this.medications = newMedications;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medication, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position){
        Medication medication = medications.get(position);
        holder.bind(medication, listener); // Assigns binding logic to bind()
    }

    @Override
    public int getItemCount() {
        return medications == null ? 0 : medications.size();
    }

    class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView medNameTextView, medTimeTextView, medDosageTextView, totalMedsTextView;
        ImageButton notifButton, expandButton, takeButton, undoButton, editButton,
        deleteButton;
        LinearLayout expandableView;

        boolean isExpanded;
        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);

            // Main interface display
            medNameTextView = itemView.findViewById(R.id.med_name);
            medTimeTextView = itemView.findViewById(R.id.med_time);
            medDosageTextView = itemView.findViewById(R.id.med_dosage);
            totalMedsTextView = itemView.findViewById(R.id.total_meds);
            notifButton = itemView.findViewById(R.id.notif_button);
            expandButton = itemView.findViewById(R.id.expand_button);

            // Expandable view interface display
            expandableView = itemView.findViewById(R.id.expandable_view);
            takeButton = itemView.findViewById(R.id.take_button);
            undoButton = itemView.findViewById(R.id.undo_button);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);

            //Expandable view
            expandButton.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                expandableView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                expandButton.setImageResource(isExpanded ? R.drawable.ic_right : R.drawable.ic_down);
            });
        }


        public void bind(Medication medication, OnMedicationClickListener listener) {
            medNameTextView.setText(medication.getName());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextDosageTime = medication.getNextDosageTime();
            expandableView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            if(nextDosageTime.isBefore(now)){
                nextDosageTime = nextDosageTime.plusDays(1);
            }

            // Validates medQuantity and maxAmt before it sets the texts
            if (medication.getMedQuantity() < 0.01){
                medication.setMedQuantity(0.01); // Automatically sets it to 0.01 if number is less than 0.01
            }

            if(medication.getMaxAmt() < 0.01){
                medication.setMaxAmt(0.01); // Automatically sets it to 0.01 if number is less than 0.01
            }


            takeButton.setOnClickListener(v -> {
                SharedPreferences prefs = v.getContext().getSharedPreferences("ExceedPrefs", Context.MODE_PRIVATE);
                boolean dontShowAgain = prefs.getBoolean("don't_show_exceed_dialog", false);



                boolean dosesTakenLessThanMaxAmt = medication.getDosesTaken() < medication.getMaxAmt();

                boolean totalMedsGreaterThanZero = medication.getTotalMeds() > 0;

                    /* If the doses taken is lower than the maximum amount, totalMedsTextView isn't 0, or "Don't Show Again"
                         is selected, medQuantity increases dosesTaken */
                    if (totalMedsGreaterThanZero && (dosesTakenLessThanMaxAmt || dontShowAgain)) {
                        medication.setDosesTaken(medication.getDosesTaken() + medication.getMedQuantity());

                    // Ensures totalMedsTextView never goes negative
                    if (medication.getTotalMeds() >= medication.getMedQuantity()) {
                        medication.setTotalMeds(medication.getTotalMeds() - medication.getMedQuantity());
                    } else {
                        medication.setTotalMeds(0);
                        Toast.makeText(v.getContext(), "You are out of medication!", Toast.LENGTH_LONG).show();
                    }

                    viewModel.update(medication);
                    notifyItemChanged(getAdapterPosition());

                } else if (medication.getTotalMeds() == 0 ){

                    // Blocks further dosage if there are no more meds left
                    Toast.makeText(v.getContext(), "You are out of medication!", Toast.LENGTH_LONG).show();

                } else {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Max Dose Reached!")
                            .setMessage("You have already taken the maximum dosage. Do you want to exceed it?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                // Allows exceeding max dosage, but only if there are meds left
                                if (totalMedsGreaterThanZero) {
                                    medication.setDosesTaken(medication.getDosesTaken() + medication.getMedQuantity());

                                    if (medication.getTotalMeds() >= medication.getMedQuantity()) {
                                        medication.setTotalMeds(medication.getTotalMeds() - medication.getMedQuantity());
                                    } else {
                                        medication.setTotalMeds(0);
                                        Toast.makeText(v.getContext(), "You are out of medication!", Toast.LENGTH_LONG).show();
                                    }
                                }
                                viewModel.update(medication);
                                notifyItemChanged(getAdapterPosition());
                            })
                            .setNegativeButton("No", (dialog, which) -> {
                                // "No" does nothing
                                dialog.dismiss();
                            })
                            .setNeutralButton("Don't Show Again", ((dialog, which) -> {
                                // "Don't Show Again" prevents future pop-ups when exceeding dosage
                                prefs.edit().putBoolean("don't_show_exceed_dialog", true).apply();
                                dialog.dismiss();
                            }))
                            .show();
                }
            });

            double initialTotalMeds = medication.getTotalMeds(); //Establishes what the totalMeds are before listener

            // Undoes medication taken in case of mistakes
            undoButton.setOnClickListener(v -> {
                double dosesTaken = medication.getDosesTaken();

                // Ensures there is one dose taken
                if (dosesTaken > 0) {
                    // Decrements dosesTaken by medQuantity
                    double newDosesTaken = dosesTaken - medication.getMedQuantity();

                    // Ensures dosesTaken does not go below zero
                    if (newDosesTaken < 0) {
                        newDosesTaken = 0;
                    }

                    // Adds the amount back to totalMedsTextView per does undone
                    double amountToAddBack = medication.getMedQuantity();

                    // Ensures that adding back does not exceed the initial totalMedsTextView
                    double updatedTotalMeds = medication.getTotalMeds() + amountToAddBack;
                    medication.setTotalMeds(Math.min(updatedTotalMeds, initialTotalMeds));

                    // Updates doses taken and total meds with the new values
                    medication.setDosesTaken(newDosesTaken);
                    medication.setTotalMeds(updatedTotalMeds);

                    // Updates the medication in ViewModel
                    viewModel.update(medication);
                    notifyItemChanged(getAdapterPosition()); // Refreshes UI
                } else {
                    Toast.makeText(v.getContext(), "No doses taken to undo!", Toast.LENGTH_SHORT).show();
                }
            });


            // Takes user to edit page
            editButton.setOnClickListener(v-> {
                Intent intent = new Intent(itemView.getContext(), EditMedication.class);
                intent.putExtra("medication_id", medication.getId());
                itemView.getContext().startActivity(intent);
            });

            //Deletes entries
            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Deleting medication")
                            .setMessage(String.format("Are you sure you want to delete %s? " +
                                    "This cannot be undone.", medication.getName()))
                            .setPositiveButton("Yes", (dialog, which) -> {
                                Medication medToDelete = medications.get(position);
                                medications.remove(position); // Removes from UI
                                viewModel.delete(medToDelete); // Removes from database
                                Toast.makeText(v.getContext(), String.format("%s deleted", medication.getName()), Toast.LENGTH_SHORT).show();
                                notifyItemRemoved(position);
                            })
                            .setNegativeButton("No", (dialog, which) ->
                                    dialog.dismiss())
                            .show();
                }
            });

            // Formats time
            String formattedTime = (nextDosageTime != null) ?
                    nextDosageTime.format(DateTimeFormatter.ofPattern("h:mm a")) : "N/A";

            // Tells user what the next reminder is
            medTimeTextView.setText(String.format("Next reminder: %s", formattedTime));

            // Informs users how many doses have been taken
            medDosageTextView.setText(String.format("%.2f/%.2f %s taken", medication.getDosesTaken(), medication.getMaxAmt(),
                medication.getAdminType().getLabel()));

            // Tells user how many pills in total they have left after taking dosage
            totalMedsTextView.setText(String.format("%.2f %s remaining", medication.getTotalMeds(),
                    medication.getAdminType().getLabel()));

            // Loads notification state
            boolean isNotificationsEnabled = medication.isNotificationsEnabled();

            // Sets initial button based on state
            notifButton.setImageResource(isNotificationsEnabled ? R.drawable.ic_notif_on : R.drawable.ic_notif_off);

            // Handles notification button clicks
            notifButton.setOnClickListener(v -> {
                boolean newState = medication.isNotificationsEnabled();
                medication.setNotificationsEnabled(newState);

                if (newState) {
                    NotifUtil.scheduleNotification(v.getContext(), medication);
                    notifButton.setImageResource(R.drawable.ic_notif_on);
                } else {
                    NotifUtil.cancelNotification(v.getContext(), medication);
                    notifButton.setImageResource(R.drawable.ic_notif_off);
                }

                // Updates medication if ViewModel is found
                if(viewModel != null){
                    viewModel.update(medication);
                } else {
                    Log.e("MedicationAdapter", "ViewModel is NULL! Check initialization.");
                }

                notifyItemChanged(getAdapterPosition());

            });

        }
    }
}
