package com.onyx.wereaddemo;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.onyx.android.sdk.utils.CollectionUtils;
import com.onyx.weread.api.OnyxWifiController;
import com.onyx.weread.wifi.WifiAdmin;
import com.onyx.weread.wifi.WifiUtil;
import com.onyx.weread.wifi.model.AccessPoint;
import com.onyx.wereaddemo.databinding.LayoutWifiFragmentBinding;
import com.onyx.wereaddemo.databinding.WifiInfoItemBinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Edward.
 * Date: 2020/8/19
 * Time: 17:24
 * Desc:
 */
public class WifiConfigFragment extends Fragment {

    private LayoutWifiFragmentBinding binding;
    private List<AccessPoint> scanResult = new ArrayList<>();
    private BaseAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWifiAdmin();
        OnyxWifiController.registerWifiReceiver();
        OnyxWifiController.scanWifiRepeating();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.layout_wifi_fragment, container, false);
        initView();
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        OnyxWifiController.unregisterWifiReceiver();
        OnyxWifiController.cancelRepeatingScanWifi();
        OnyxWifiController.releaseWifiAdmin();
    }

    private void initView() {
        binding.addWifiContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = OnyxWifiController.buildAddNetworkDialogIntent();
                startActivity(intent);
            }
        });
        binding.switchWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnyxWifiController.toggleWifi();
            }
        });
        updateUi(OnyxWifiController.isWifiEnabled());
        adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return scanResult.size();
            }

            @Override
            public AccessPoint getItem(int position) {
                return scanResult.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
//                accessPoint.getSignalLevel(); 0 ~ 4
//                accessPoint.getSecurity() == BaseSecurity.SECURITY_NONE
                WifiInfoItemBinding itemBinding = WifiInfoItemBinding.inflate(getLayoutInflater(), parent, false);
                final AccessPoint accessPoint = getItem(position);
                itemBinding.textViewWifiSsid.setText(accessPoint.getScanResult().SSID);
                itemBinding.textViewWifiSecurity.setText(accessPoint.getSecurityString());
                itemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = OnyxWifiController.buildShowWifiDialogIntent(accessPoint);
                        startActivity(intent);
                    }
                });
                return itemBinding.getRoot();
            }
        };
        binding.listScanResult.setAdapter(adapter);

    }

    private void initWifiAdmin() {
        OnyxWifiController.init(new WifiAdmin.Callback() {
            @Override
            public void onWifiStateChange(boolean isWifiEnable, int wifiExtraState) {
                updateUi(isWifiEnable);
            }

            @Override
            public void onScanResultReady(List<AccessPoint> scanResult) {
                updateData(scanResult);
            }

            @Override
            public void onSupplicantStateChanged(NetworkInfo.DetailedState state) {
                updateAccessPointDetailedState(state);
            }

            @Override
            public void onNetworkConnectionChange(NetworkInfo.DetailedState state) {
                updateAccessPointDetailedState(state);
            }

            @Override
            public void onLinkConfigurationChange(List<AccessPoint> scanResult) {
                updateData(scanResult);
            }

            @Override
            public void onConfiguredNetworksChange(List<AccessPoint> scanResult) {
                updateData(scanResult);

            }

            @Override
            public void onConnectedNetworkRSSIChange(int newRSSI) {
                updateConnectedAccessPointRSSI(newRSSI);
            }
        });
    }

    private void updateUi(boolean isWifiEnable) {
        binding.switchWifi.setChecked(isWifiEnable);
        binding.container.setVisibility(isWifiEnable ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateAccessPointDetailedState(NetworkInfo.DetailedState state) {
        AccessPoint connectedAccessPoint = null;
        Iterator<AccessPoint> iterator = scanResult.iterator();
        while (iterator.hasNext()) {
            AccessPoint accessPoint = iterator.next();
            WifiConfiguration config = accessPoint.getWifiConfiguration();
            WifiInfo currentConnectionInfo = OnyxWifiController.getCurrentConnectionInfo();
            if (config == null || currentConnectionInfo == null) {
                continue;
            }
            int connectedId = currentConnectionInfo.getNetworkId();
            if (accessPoint.getWifiConfiguration().networkId == connectedId) {
                accessPoint.setDetailedState(state);
                if (state == NetworkInfo.DetailedState.CONNECTED) {
                    accessPoint.setSecurityString(getString(R.string.wifi_connected));
                    accessPoint.updateWifiInfo();
                    connectedAccessPoint = accessPoint;
                    iterator.remove();
                }
                break;
            }
        }
        if (connectedAccessPoint != null) {
            scanResult.add(0, connectedAccessPoint);
        }
        adapter.notifyDataSetChanged();
    }

    private void updateConnectedAccessPointRSSI(int rssi) {
        for (AccessPoint accessPoint : scanResult) {
            if (accessPoint.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                accessPoint.setSignalLevel(WifiUtil.getWifiSignalLevel(rssi));
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }


    private boolean updateData(List<AccessPoint> scanResult) {
        if (CollectionUtils.isNullOrEmpty(scanResult)) {
            return true;
        }
        setScanResult(scanResult);
        adapter.notifyDataSetChanged();
        return false;
    }

    public void setScanResult(List<AccessPoint> scanResult) {
        this.scanResult = scanResult;
    }
}
