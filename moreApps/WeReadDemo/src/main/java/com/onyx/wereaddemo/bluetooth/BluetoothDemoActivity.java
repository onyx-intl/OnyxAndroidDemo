package com.onyx.wereaddemo.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;

import com.onyx.android.sdk.api.utils.StringUtils;
import com.onyx.weread.api.OnyxBluetoothController;
import com.onyx.weread.bluetooth.BluetoothCallback;
import com.onyx.wereaddemo.PermissionCheckActivity;
import com.onyx.wereaddemo.R;
import com.onyx.wereaddemo.bluetooth.adapter.BluetoothDeviceAdapter;
import com.onyx.wereaddemo.bluetooth.model.BluetoothScanResult;
import com.onyx.wereaddemo.databinding.ActivityBluetoothBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BluetoothDemoActivity extends PermissionCheckActivity {
    public static final String[] PERMISSION = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
    };
    private static final String TAG = BluetoothDemoActivity.class.getSimpleName();
    public static final int ONCE_SCAN_TIME = 3;
    private ActivityBluetoothBinding binding;
    private BluetoothDeviceAdapter adapter = new BluetoothDeviceAdapter();
    private Set<BluetoothDevice> bluetoothDeviceSet = new HashSet();
    private int scanTime = 0;

    public ObservableBoolean isBluetoothSwitchButtonEnabled = new ObservableBoolean(true);
    public ObservableBoolean isBluetoothEnabled = new ObservableBoolean(false);
    public ObservableField<String> switchTip = new ObservableField<>();
    public ObservableField<String> deviceName = new ObservableField<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.bind(LayoutInflater.from(this).inflate(R.layout.activity_bluetooth, null, false));
        binding.setModel(this);
        setContentView(binding.getRoot());
        initBluetoothAdmin();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission(PERMISSION);
    }

    private void initBluetoothAdmin() {
        OnyxBluetoothController.init(new BluetoothCallback() {
            @Override
            public void onBluetoothStateChanged(int bluetoothState) {
                switch (bluetoothState) {
                    case BluetoothAdapter.STATE_OFF:
                        if (adapter != null) {
                            adapter.clearData();
                            bluetoothDeviceSet.clear();
                        }
                        isBluetoothSwitchButtonEnabled.set(true);
                        switchTip.set("打开蓝牙");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        startDiscovery();
                        updateBondedDevices();
                        isBluetoothSwitchButtonEnabled.set(true);
                        switchTip.set("关闭蓝牙");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        isBluetoothSwitchButtonEnabled.set(false);
                        switchTip.set("正在打开蓝牙...");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        isBluetoothSwitchButtonEnabled.set(false);
                        switchTip.set("正在关闭蓝牙...");
                        break;
                }
                isBluetoothEnabled.set(OnyxBluetoothController.isBluetoothEnabled());
            }

            @Override
            public void onScanningStateChanged(boolean started) {
                if (++scanTime <= ONCE_SCAN_TIME) {
                    startDiscovery();
                } else {
                    scanTime = 0;
                }
            }

            @Override
            public void onDeviceFound(BluetoothDevice device, short rssi) {
                if (StringUtils.isNotBlank(device.getName()) &&
                        device.getBondState() != BluetoothDevice.BOND_BONDED &&
                        !bluetoothDeviceSet.contains(device)) {
                    BluetoothScanResult scanResult = new BluetoothScanResult(device, BluetoothDemoActivity.this);
                    scanResult.setRssi(rssi);
                    adapter.getScannedDevices().add(scanResult);
                    bluetoothDeviceSet.add(device);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onDeviceBondStateChanged(BluetoothDevice device, int bondState) {
                for (BluetoothScanResult scannedDevice : adapter.getScannedDevices()) {
                    if (scannedDevice.getDevice().equals(device)) {
                        scannedDevice.update(device);
                    }
                }
            }

            @Override
            public void onConnectionStateChanged(BluetoothDevice device, int state) {
                for (BluetoothScanResult scannedDevice : adapter.getScannedDevices()) {
                    if (scannedDevice.getDevice().equals(device)) {
                        scannedDevice.update(device, state);
                        return;
                    }
                }
            }

            @Override
            public void onScanModeChanged(int prevMode, int mode) {
//                BluetoothAdapter.SCAN_MODE_NONE
//                BluetoothAdapter.SCAN_MODE_CONNECTABLE
//                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE
            }

            @Override
            public void onLocalNameChanged(String name) {
                if (StringUtils.isNotBlank(name)) {
                    deviceName.set(name);
                }
            }
        });
        OnyxBluetoothController.registerBluetoothReceiver();
        ensureBluetoothEnable();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopDiscovery();
        OnyxBluetoothController.unregisterBluetoothReceiver();
        OnyxBluetoothController.release();
    }

    private void initView() {
        isBluetoothEnabled.set(OnyxBluetoothController.isBluetoothEnabled());
        switchTip.set(isBluetoothEnabled.get() ? "关闭蓝牙" : "打开蓝牙");
        deviceName.set(OnyxBluetoothController.getDeviceName());
        binding.list.setAdapter(adapter);
        adapter.setContext(this);
        updateBondedDevices();
    }

    public void showBluetoothDialog() {
        OnyxBluetoothController.showDeviceRenameDialog(this);
    }

    public void toggleBluetooth() {
        OnyxBluetoothController.toggleBluetooth();
    }

    private void ensureBluetoothEnable() {
        if (!OnyxBluetoothController.isBluetoothEnabled()) {
            OnyxBluetoothController.enableBluetooth();
        }
        startDiscovery();
    }

    private void updateBondedDevices() {
        Set<BluetoothDevice> bondedDevices = OnyxBluetoothController.getBondedDevices();
        bluetoothDeviceSet.addAll(bondedDevices);
        if (adapter != null) {
            ArrayList<BluetoothDevice> list = new ArrayList<>();
            list.addAll(bondedDevices);
            adapter.setScannedDevices(adapter.createViewModel(list));
        }
    }

    public void stopDiscovery() {
        OnyxBluetoothController.startDiscovery();
    }

    public void startDiscovery() {
        OnyxBluetoothController.startDiscovery();

    }

    public void connect(final BluetoothDevice device) {
        // TODO: 2020/9/2
    }

    @NonNull
    @Override
    protected String[] getPermission() {
        return PERMISSION;
    }

    @Override
    protected void onRequestPermissionSuccess() {
    }

    @Override
    protected void onRequestPermissionFailed() {
        finish();
    }
}
