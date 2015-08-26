package com.tenten;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.AttributeSet;
import android.widget.ImageView;

public class WifiIcon extends ImageView {
    int mInetCondition;
    private final Drawable[][] sWifiSignalImages = {
            {setDrawable("stat_sys_wifi_signal_1"),
                    setDrawable("stat_sys_wifi_signal_2"),
                    setDrawable("stat_sys_wifi_signal_3"),
                    setDrawable("stat_sys_wifi_signal_4") },
            { setDrawable("stat_sys_wifi_signal_1_fully"),
                    setDrawable("stat_sys_wifi_signal_2_fully"),
                    setDrawable("stat_sys_wifi_signal_3_fully"),
                    setDrawable("stat_sys_wifi_signal_4_fully") }
    };
    private final Drawable sWifiTemporarilyNotConnectedImage = setDrawable("stat_sys_wifi_signal_0");
    private int mLastWifiSignalLevel = -1;
    private boolean mIsWifiConnected = false;
    public WifiIcon(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        setImageDrawable(sWifiSignalImages[0][0]);
        setVisibility(GONE);

        IntentFilter iF = new IntentFilter();
        iF.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        iF.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        iF.addAction(WifiManager.RSSI_CHANGED_ACTION);
        iF.addAction(ConnectivityManager.INET_CONDITION_ACTION);
        iF.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) ||
                        action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION) ||
                        action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
                    updateWifi(intent);
                }
                else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION) ||
                        action.equals(ConnectivityManager.INET_CONDITION_ACTION)) {
                    updateConnectivity(intent);
                }
            }
        }, iF);
    }

    private void updateWifi(Intent intent) {
        final String action = intent.getAction();
        if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {

            final boolean enabled = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_ENABLED;

            if (!enabled) {
                setVisibility(GONE);
            }

        } else if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
            final boolean enabled = intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED,
                    false);
            if (!enabled) {
                setVisibility(GONE);
            }
        } else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
            Drawable iconId;
            final int newRssi = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -200);
            int newSignalLevel = WifiManager.calculateSignalLevel(newRssi,
                    sWifiSignalImages[0].length);
            if (newSignalLevel != mLastWifiSignalLevel) {
                mLastWifiSignalLevel = newSignalLevel;
                if (mIsWifiConnected) {
                    iconId = sWifiSignalImages[mInetCondition][newSignalLevel];
                } else {
                    iconId = sWifiTemporarilyNotConnectedImage;
                }
                setImageDrawable(iconId);
            }
        }
    }

    private void updateConnectivity(Intent intent){
        NetworkInfo info = intent.getParcelableExtra(
                ConnectivityManager.EXTRA_NETWORK_INFO);
        int connectionStatus = intent.getIntExtra(ConnectivityManager.EXTRA_INET_CONDITION, 0);

        int inetCondition = (connectionStatus > 50 ? 1 : 0);
        switch (info.getType()) {
            case ConnectivityManager.TYPE_WIFI:
                mInetCondition = inetCondition;
                if (info.isConnected()) {
                    mIsWifiConnected = true;
                    Drawable iconId;
                    if (mLastWifiSignalLevel == -1) {
                        iconId = sWifiSignalImages[mInetCondition][0];
                    } else {
                        iconId = sWifiSignalImages[mInetCondition][mLastWifiSignalLevel];
                    }
                    setImageDrawable(iconId);
                    setVisibility(VISIBLE);
                } else {
                    mLastWifiSignalLevel = -1;
                    mIsWifiConnected = false;
                    Drawable iconId = sWifiSignalImages[0][0];

                    setImageDrawable(iconId);
                    setVisibility(GONE);
                }
                break;
        }
    }

    public Drawable setDrawable(String mDrawableName){
        final String packName = "com.android.systemui";
        int mDrawableResID = 0;
        Drawable myDrawable = null;
        try {
            PackageManager manager = getContext().getPackageManager();
            Resources mApk1Resources = manager.getResourcesForApplication(packName);

            mDrawableResID = mApk1Resources.getIdentifier(mDrawableName, "drawable", packName);

            myDrawable = mApk1Resources.getDrawable( mDrawableResID );
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return myDrawable;
    }
}
