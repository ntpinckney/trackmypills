package com.example.trackmypills.ui.adapter;

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
        TextView medName, medTime, medDosage;
        ImageButton editButton;
        ImageButton notifButton;


        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            medName = itemView.findViewById(R.id.med_name);
            medTime = itemView.findViewById(R.id.med_time);
            medDosage = itemView.findViewById(R.id.med_dosage);
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
            medTime.setText(formattedTime);

            medDosage.setText(String.format("%d/%d %s taken", medication.getDosesTaken(), medication.getMaxAmt(),
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

            itemView.setOnClickListener(v -> {
                if (medication.getDosesTaken() < medication.getMaxAmt()) {
                    medication.setDosesTaken(medication.getDosesTaken() + 1);
                    viewModel.update(medication);
                    notifyItemChanged(getAdapterPosition());
                } else {
                    Toast.makeText(v.getContext(), "Max dose reached", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
}
