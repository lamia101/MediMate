package com.example.personalhealthcareapplication.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalhealthcareapplication.R;
import com.example.personalhealthcareapplication.model.MedicineReminder;

import java.util.List;

public class MedicineReminderAdapter extends RecyclerView.Adapter<MedicineReminderAdapter.ViewHolder> {
    private List<MedicineReminder> reminders;

    public MedicineReminderAdapter(List<MedicineReminder> reminders) {
        this.reminders = reminders;
    }

    public void setReminders(List<MedicineReminder> newReminders) {
        this.reminders = newReminders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine_reminder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicineReminder reminder = reminders.get(position);
        holder.tvMedicineName.setText(reminder.getMedicineName());
        holder.tvQuantity.setText(reminder.getQuantity());
        holder.tvTime.setText(reminder.getFormattedTime());
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicineName;
        TextView tvQuantity;
        TextView tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
