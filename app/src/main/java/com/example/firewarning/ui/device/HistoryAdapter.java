package com.example.firewarning.ui.device;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firewarning.R;
import com.example.firewarning.common.CommonActivity;
import com.example.firewarning.ui.device.model.Device;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private Activity context;
    private ArrayList<Device> history;

    public HistoryAdapter(Activity context, ArrayList<Device> history) {
        this.context = context;
        this.history = history;
    }

    public void setData(ArrayList<Device> history) {
        this.history = history;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_history, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Device device = history.get(position);
        holder.tvTime.setText(device.getTime());
        holder.tvTemperature.setText("Nhiệt độ : " + device.getNO() + " độ C");
        try {
            if (CommonActivity.isNullOrEmpty(device.getNG()) || CommonActivity.isNullOrEmpty(device.getNO()))
                return;
            if (Double.parseDouble(device.getNO()) > Double.parseDouble(device.getNG())) {
                holder.tvTemperature.setTextColor(context.getResources().getColor(R.color.red));
                holder.tvTime.setTextColor(context.getResources().getColor(R.color.red));
            } else {
                holder.tvTemperature.setTextColor(context.getResources().getColor(R.color.black));
                holder.tvTime.setTextColor(context.getResources().getColor(R.color.black));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return history.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        TextView tvTemperature;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTemperature = itemView.findViewById(R.id.tvTemperature);
        }
    }
}
