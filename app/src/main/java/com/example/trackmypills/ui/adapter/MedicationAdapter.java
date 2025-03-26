package com.example.trackmypills.ui.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackmypills.models.Medication;
import com.example.trackmypills.viewmodel.MedicationViewModel;
import com.example.trackmypills.R;
import com.example.trackmypills.utils.NotifUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {
    private List<Medication> medications;
    private OnMedicationClickListener listener; // Callback for item clicks

    private static MedicationViewModel viewModel;

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
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        Medication medication = medications.get(position);
        holder.bind(medication, listener); // Assigns binding logic to bind()
    }

    @Override
    public int getItemCount() {
        return medications == null ? 0 : medications.size();
    }

    class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView medName, medTime, medDosage, totalMeds;
        ImageButton editButton;
        ImageButton notifButton;


        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            medName = itemView.findViewById(R.id.med_name);
            medTime = itemView.findViewById(R.id.med_time);
            medDosage = itemView.findViewById(R.id.med_dosage);
            totalMeds = itemView.findViewById(R.id.total_meds);
            editButton = itemView.findViewById(R.id.edit_button);
            notifButton = itemView.findViewById(R.id.notif_button);
        }

        public void bind(Medication medication, OnMedicationClickListener listener) {
            medName.setText(medication.getName());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextDosageTime = medication.getNextDosageTime();

            if(nextDosageTime.isBefore(now)){
                nextDosageTime = nextDosageTime.plusDays(1);
            }

            String formattedTime = (nextDosageTime != null) ?
                    nextDosageTime.format(DateTimeFormatter.ofPattern("h:mm a")) : "N/A";

            medTime.setText(String.format("Next reminder: %s", formattedTime));

            medDosage.setText(String.format("%.2f/%.2f %s taken", medication.getDosesTaken(), medication.getMaxAmt(),
                medication.getAdminType().getLabel()));

            totalMeds.setText(String.format("%.2f %s remaining", medication.getTotalMeds(),
                    medication.getAdminType().getLabel()));

            //Loads notification state
            boolean isNotificationsEnabled = medication.isNotificationsEnabled();

            isNotificationsEnabled = !isNotificationsEnabled;

            // Sets initial button based on state
            notifButton.setImageResource(isNotificationsEnabled ? R.drawable.ic_notif_on : R.drawable.ic_notif_off);

            // Handles notification button clicks
            notifButton.setOnClickListener(v -> {
                boolean newState = !medication.isNotificationsEnabled();
                medication.setNotificationsEnabled(newState);

                if (newState) {
                    NotifUtil.scheduleNotification(v.getContext(), medication);
                    notifButton.setImageResource(R.drawable.ic_notif_on);
                } else {
                    NotifUtil.cancelNotification(v.getContext(), medication);
                    notifButton.setImageResource(R.drawable.ic_notif_off);
                }


                if(viewModel != null){
                    viewModel.update(medication);
                } else {
                    Log.e("MedicationAdapter", "ViewModel is NULL! Check initialization.");
                }

                notifyItemChanged(getAdapterPosition());

            });


            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMedicationClick(medication);
                }
            });

            // Validates medQuantity and maxAmt before it sets the texts
            if (medication.getMedQuantity() < 0.01){
                medication.setMedQuantity(0.01); // Automatically sets it to 0.01 if number is less than 0.01
            }

            if(medication.getMaxAmt() < 0.01){
                medication.setMaxAmt(0.01); // Automatically sets it to 0.01 if number is less than 0.01
            }

            itemView.setOnClickListener(v -> {
                SharedPreferences prefs = v.getContext().getSharedPreferences("ExceedPrefs", Context.MODE_PRIVATE);
                boolean dontShowAgain = prefs.getBoolean("don't_show_exceed_dialog", false);

                /* If the doses taken is lower than the maximum amount, totalMeds isn't 0, or "Don't Show Again"
                is selected, medQuantity increases dosesTaken
                 */

                boolean dosesTakenLessThanMaxAmt = medication.getDosesTaken() < medication.getMaxAmt();

                boolean totalMedsGreaterThanZero = medication.getTotalMeds() > 0;

                if (totalMedsGreaterThanZero && (dosesTakenLessThanMaxAmt || dontShowAgain)) {
                    medication.setDosesTaken(medication.getDosesTaken() + medication.getMedQuantity());

                    // Ensures totalMeds never goes negative
                    if (medication.getTotalMeds() >= medication.getMedQuantity()) {
                        medication.setTotalMeds(medication.getTotalMeds() - medication.getMedQuantity());
                    } else {
                        medication.setTotalMeds(0);
                        Toast.makeText(v.getContext(), "You are out of medication!", Toast.LENGTH_LONG).show();
                    }

                    viewModel.update(medication);
                    notifyItemChanged(getAdapterPosition());

                    if (medication.getDosesTaken() > medication.getMaxAmt()) {
                        //Changes dose text to red once max dose is exceeded
                        TextView dosesTextView = itemView.findViewById(R.id.med_dosage);
                        dosesTextView.setTextColor(Color.RED);
                    }
                } else if (medication.getTotalMeds() == 0 ){

                    // Blocks further dosage if there are no more meds left
                    Toast.makeText(v.getContext(), "You are out of medication!", Toast.LENGTH_LONG).show();

                } else {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Max Dose Reached!")
                            .setMessage("You have already taken the maximum dosage. Do you want to exceed it?")
                            .setPositiveButton("Yes", (dialog, which) -> {

                                // "Yes" allows user to exceed maximum dosage and warns them every time
                                medication.setDosesTaken(medication.getDosesTaken() + medication.getMedQuantity());

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
        }
    }
}
