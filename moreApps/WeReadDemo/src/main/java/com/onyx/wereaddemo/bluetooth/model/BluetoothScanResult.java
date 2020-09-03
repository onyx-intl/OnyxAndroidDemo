package com.onyx.wereaddemo.bluetooth.model;

import android.bluetooth.BluetoothDevice;
import android.databinding.ObservableField;
import android.databinding.ObservableShort;

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
    private int state;
    public ObservableShort rssi = new ObservableShort();
    public ObservableField<String> stateString = new ObservableField<>();
    public ObservableField<String> name = new ObservableField<>();
    public int typeRes;

    public BluetoothScanResult(BluetoothDevice device) {
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
            case CONNECTED_STATE:
                stateString = "已连接";
                break;
            case CONNECTING_STATE:
                stateString = "正在连接...";
                break;
            case BluetoothDevice.BOND_NONE:
            default:
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
    }

    public void update(BluetoothDevice device) {
        update(device, device.getBondState());
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
}
