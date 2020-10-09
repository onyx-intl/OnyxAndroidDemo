package com.onyx.wereaddemo.bluetooth.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.onyx.wereaddemo.bluetooth.model.BluetoothScanResult;
import com.onyx.wereaddemo.databinding.LayoutBluetoothItemBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Edward.
 * Date: 2020/8/26
 * Time: 11:06
 * Desc:
 */
public class BluetoothDeviceAdapter extends BaseAdapter {
    private List<BluetoothScanResult> scannedDevices = new ArrayList<>();
    private Context context;

    @Override
    public int getCount() {
        return scannedDevices.size();
    }

    @Override
    public BluetoothScanResult getItem(int position) {
        return scannedDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutBluetoothItemBinding binding = LayoutBluetoothItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        BluetoothScanResult bluetoothDevice = getItem(position);
        binding.setModel(bluetoothDevice);
        binding.type.setImageResource(bluetoothDevice.typeRes);
        return binding.getRoot();
    }

    public List<BluetoothScanResult> createViewModel(List<BluetoothDevice> devices) {
        List<BluetoothScanResult> resultList = new ArrayList<>();
        for (BluetoothDevice device : devices) {
            resultList.add(new BluetoothScanResult(device, context));
        }
        return resultList;
    }

    public void clearData() {
        scannedDevices.clear();
    }

    public List<BluetoothScanResult> getScannedDevices() {
        return scannedDevices;
    }

    public void setScannedDevices(List<BluetoothScanResult> scannedDevices) {
        this.scannedDevices = scannedDevices;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
