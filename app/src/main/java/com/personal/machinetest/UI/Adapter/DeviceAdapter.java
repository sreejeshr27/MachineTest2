package com.personal.machinetest.UI.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.personal.machinetest.data.database.Device;
import com.personal.machinetest.databinding.DeviceItemLayoutBinding;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    List<Device> deviceList = new ArrayList<>();
    private final DeviceClickListener deviceClickListener;

    public DeviceAdapter(DeviceClickListener deviceClickListener) {
        this.deviceClickListener = deviceClickListener;
    }

    public interface DeviceClickListener {
        void onDeviceClicked(Device device);
    }

    public void setDeviceList(List<Device> deviceList) {
        this.deviceList = deviceList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DeviceItemLayoutBinding binding = DeviceItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceAdapter.ViewHolder holder, int position) {
        Device device = deviceList.get(position);
        holder.binding.tvDeviceName.setText(device.name);
        holder.binding.tvDeviceIp.setText(device.ipAddress);
        if (device.isOnline) {
            holder.binding.tvDeviceStatus.setText("Online");
            holder.binding.tvDeviceStatus.setTextColor(Color.GREEN);
        } else {
            holder.binding.tvDeviceStatus.setText("Offline");
            holder.binding.tvDeviceStatus.setTextColor(Color.RED);

        }
        holder.itemView.setOnClickListener(v -> deviceClickListener.onDeviceClicked(device));

    }


    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        DeviceItemLayoutBinding binding;

        public ViewHolder(DeviceItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }
}
