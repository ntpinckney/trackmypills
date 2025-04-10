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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        ImageButton expandButton, takeButton, undoButton, editButton,
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
            expandButton = itemView.findViewById(R.id.expand_button);

            // Expandable view interface display
            expandableView = itemView.findViewById(R.id.expandable_view);
            takeButton = itemView.findViewById(R.id.take_button);
            undoButton = itemView.findViewById(R.id.undo_button);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }

        public void bind(Medication medication, OnMedicationClickListener listener) {
            medNameTextView.setText(medication.getName());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextDosageTime = medication.getNextDosageTime();
            expandableView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            // Animations
            final Animation rotateClockwise = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.rotate_clockwise);
            final Animation rotateCounterclockwise = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.rotate_counterclockwise);
            final Animation collapse = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.collapse_animation);
            final Animation expand = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.expand_animation);

            expandButton.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                if (isExpanded) {
                    expandableView.setVisibility(View.VISIBLE);
                    expandableView.startAnimation(expand);
                    expandButton.startAnimation(rotateClockwise);
                    expandButton.setRotation(90);
                } else {
                    expandableView.startAnimation(collapse);
                    collapse.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) { }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            expandableView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) { }
                    });
                    expandButton.startAnimation(rotateCounterclockwise);
                    expandButton.setRotation(0);
                }
            });

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

                takeDose(medication, v, totalMedsGreaterThanZero, dosesTakenLessThanMaxAmt, dontShowAgain, prefs);
            });

            double initialTotalMeds = medication.getTotalMeds(); // Establishes what the totalMeds are before listener

            // Undoes medication taken in case of mistakes
            undoButton.setOnClickListener(v -> {
                double dosesTaken = medication.getDosesTaken();

                // Ensures there is at least one dose taken
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
                            .setTitle("Delete Medication")
                            .setMessage(String.format("Are you sure you want to delete %s? " +
                                    "This cannot be undone.", medication.getName()))
                            .setPositiveButton("Yes", (dialog, which) -> {
                                Medication medToDelete = medications.get(position);
                                medications.remove(position); // Removes from list
                                viewModel.delete(medToDelete); // Removes from database
                                Toast.makeText(v.getContext(), String.format("%s deleted", medToDelete.getName()), Toast.LENGTH_SHORT).show();
                                notifyItemRemoved(position); // Refreshes UI
                            })
                            .setNegativeButton("No", (dialog, which) ->
                                    dialog.dismiss()) // Does not delete medication
                            .show();
                }
            });

            // Updates nextDosageTime for display
            nextDosageTime = medication.getNextDosageTime();

            // Formats time
            String formattedTime = (nextDosageTime != null) ?
                    nextDosageTime.format(DateTimeFormatter.ofPattern("h:mm a")) : "N/A";

            // Tells user what the next reminder is
            medTimeTextView.setText(String.format("Next reminder: %s", formattedTime));

            // Informs users how many doses have been taken
            medDosageTextView.setText(String.format("%.2f/%.2f %s taken", medication.getDosesTaken(), medication.getMaxAmt(),
                medication.getAdminType().getLabel()));

            // Tells user how many pills remain in total
            totalMedsTextView.setText(String.format("%.2f %s remaining", medication.getTotalMeds(),
                    medication.getAdminType().getLabel()));

        }

        private void takeDose(Medication medication, View v, boolean totalMedsGreaterThanZero, boolean dosesTakenLessThanMaxAmt, boolean dontShowAgain, SharedPreferences prefs) {

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
            // Blocks further dosage increments if there are no more meds left
            Toast.makeText(v.getContext(), "You are out of medication!", Toast.LENGTH_LONG).show();

        } else {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Max Dose Reached")
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
                        updateNextDosageTime(medication);
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
        }
    }

    private void updateNextDosageTime(Medication medication) {
        // Gets maximum doses from medication
        double maxDoses = medication.getMaxAmt();
        // Gets doses taken from medication
        double dosesTaken = medication.getDosesTaken();
        // Gets current date and time
        LocalDateTime now = LocalDateTime.now();

        // Gets initial dose time and creates new LocalDateTime with initial time
        LocalTime startTime = medication.getStartTime();
        LocalDateTime nextTime = LocalDateTime.of(now.toLocalDate(), startTime);

        // Compares does with maximum doses. If equal or greater sets time to tomorrow
        if(dosesTaken >= maxDoses){
            // Sets the next dose time to tomorrow
            nextTime = nextTime.plusDays(1);
            medication.setNextDosageTime(nextTime);
            return;
        };

        // Finds the next time while doses taken is less than max doses
        while(!nextTime.isAfter(now)) {
            // Adds time between doses
            nextTime = nextTime.plusHours((long) medication.getFrequency().getIntervalHours());
        }

        // Updates nextDosageTime
        medication.setNextDosageTime(nextTime);
        }

    }