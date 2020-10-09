package com.onyx.wereaddemo.bluetooth.model;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableShort;

import com.onyx.android.sdk.utils.ActivityUtil;
import com.onyx.weread.api.OnyxBluetoothController;
import com.onyx.wereaddemo.R;

import java.util.Observable;

/**
 * Created by Edward.
 * Date: 2020/8/27
 * Time: 17:27
 * Desc:
 */
public class BluetoothScanResult extends Observable {
    public static final int CONNECTED_STATE = 13;
    public static final int CONNECTING_STATE = 14;
    public static final int DISCONNECTED_STATE = 15;

    private BluetoothDevice device;
    private int state = -1;
    private Context context;
    public ObservableShort rssi = new ObservableShort();
    public ObservableField<String> stateString = new ObservableField<>();
    public ObservableField<String> name = new ObservableField<>();
    public ObservableBoolean isBonded = new ObservableBoolean(false);
    public int typeRes;

    public BluetoothScanResult(BluetoothDevice device, Context context) {
        this.context = context;
        update(device);
    }

    private String getStateString() {
        String stateString = "";
        switch (state) {
            case BluetoothDevice.BOND_BONDED:
                stateString = "已配对";
                break;
            case BluetoothDevice.BOND_BONDING:
                stateString = "正在配对...";
                break;
            case BluetoothAdapter.STATE_CONNECTED:
                stateString = "已连接";
                break;
            case BluetoothAdapter.STATE_CONNECTING:
                stateString = "正在连接...";
                break;
            case BluetoothAdapter.STATE_DISCONNECTED:
                if (getDevice().getBondState() == BluetoothDevice.BOND_BONDED) {
                    stateString = "已配对";
                } else {
                    stateString = "";
                }
                break;
            case BluetoothDevice.BOND_NONE:
            default:
                stateString = "";
                break;
        }
        return stateString;
    }

    public void setRssi(short rssi) {
        this.rssi.set(rssi);
    }

    public void setState(int state) {
        this.state = state;
        stateString.set(getStateString());
        isBonded.set(getDevice().getBondState() == BluetoothDevice.BOND_BONDED);
    }

    public void update(BluetoothDevice device) {
        int state = device.getBondState();
        if (OnyxBluetoothController.isConnected(device)) {
            state = BluetoothAdapter.STATE_CONNECTED;
        }
        update(device, state);
    }

    public void update(BluetoothDevice device, int state) {
        this.device = device;
        name.set(device.getName());
        switch (OnyxBluetoothController.getDeviceType(device)) {
            case PHONE:
                typeRes = R.drawable.ic_phone;
                break;
            case COMPUTER:
                typeRes = R.drawable.ic_bt_laptop;
                break;
            case IMAGING:
                typeRes = R.drawable.ic_settings_print;
                break;
            case PROFILE_HEADSET:
                typeRes = R.drawable.ic_bt_headset_hfp;
                break;
            case PROFILE_A2DP:
                typeRes = R.drawable.ic_bt_headphones_a2dp;
                break;
            case PERIPHERAL_KEYBOARD:
            case PERIPHERAL_KEYBOARD_POINTING:
                typeRes = R.drawable.ic_lockscreen_ime;
                break;
            case PERIPHERAL_POINTING:
                typeRes = R.drawable.ic_bt_pointing_hid;
                break;
            case PERIPHERAL_NON_KEYBOARD_NON_POINTING:
                typeRes = R.drawable.ic_bt_misc_hid;
                break;
            case SETTINGS_BLUETOOTH:
            default:
                typeRes = R.drawable.ic_settings_bluetooth;
                break;
        }
        setState(state);
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void onclick() {
        if (state == BluetoothAdapter.STATE_CONNECTED) {
            toDeviceDetail(getDevice());
        } else if (getDevice().getBondState() == BluetoothDevice.BOND_NONE) {
            OnyxBluetoothController.createBond(getDevice());
        } else if (getDevice().getBondState() == BluetoothDevice.BOND_BONDED) {
            connect(getDevice());
        }
    }

    public void toDeviceDetailClick() {
        toDeviceDetail(getDevice());
    }

    private void toDeviceDetail(BluetoothDevice device) {
        Intent intent = OnyxBluetoothController.buildDeviceDetailDialogIntent(device);
        ActivityUtil.startActivitySafely(context, intent);
    }

    private void connect(BluetoothDevice device) {
        OnyxBluetoothController.connect(device);
    }
}
