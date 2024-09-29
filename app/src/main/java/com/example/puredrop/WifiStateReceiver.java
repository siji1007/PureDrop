package com.example.puredrop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.widget.Switch;

public class WifiStateReceiver extends BroadcastReceiver {

    private Switch aSwitch;

    public WifiStateReceiver(Switch aSwitch) {
        this.aSwitch = aSwitch;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            // WiFi is enabled, turn off the switch
            aSwitch.setChecked(false);
        }
    }
}
