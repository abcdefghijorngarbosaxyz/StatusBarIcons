package com.tenten;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothPbap;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BluetoothIcon extends ImageView {
    BluetoothAdapter bluetoothAdapter;
    private int mBluetoothHeadsetState;
    private boolean mBluetoothA2dpConnected;
    private int mBluetoothPbapState;
    private boolean mBluetoothEnabled;
    public BluetoothIcon(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter iF = new IntentFilter();
        iF.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        iF.addAction(BluetoothHeadset.ACTION_STATE_CHANGED);
        iF.addAction(BluetoothA2dp.ACTION_SINK_STATE_CHANGED);
        iF.addAction(BluetoothPbap.PBAP_STATE_CHANGED_ACTION);

        if(bluetoothAdapter.isEnabled()){
            setImageDrawable(setDrawable("stat_sys_data_bluetooth"));
            mBluetoothEnabled = bluetoothAdapter.isEnabled();
        }else{
            setVisibility(GONE);
            mBluetoothEnabled = false;
        }

        mBluetoothA2dpConnected = false;
        mBluetoothHeadsetState = BluetoothHeadset.STATE_DISCONNECTED;
        mBluetoothPbapState = BluetoothPbap.STATE_DISCONNECTED;

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateBluetoothIcon(intent);
            }
        }, iF);

    }

    private final void updateBluetoothIcon(Intent intent) {
        Drawable iconId = setDrawable("stat_sys_data_bluetooth");
        String action = intent.getAction();
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            mBluetoothEnabled = state == BluetoothAdapter.STATE_ON;
        } else if (action.equals(BluetoothHeadset.ACTION_STATE_CHANGED)) {
            mBluetoothHeadsetState = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE,
                    BluetoothHeadset.STATE_ERROR);
        } else if (action.equals(BluetoothA2dp.ACTION_SINK_STATE_CHANGED)) {
            BluetoothA2dp a2dp = new BluetoothA2dp(mContext);
            if (a2dp.getConnectedSinks().size() != 0) {
                mBluetoothA2dpConnected = true;
            } else {
                mBluetoothA2dpConnected = false;
            }
        } else if (action.equals(BluetoothPbap.PBAP_STATE_CHANGED_ACTION)) {
            mBluetoothPbapState = intent.getIntExtra(BluetoothPbap.PBAP_STATE,
                    BluetoothPbap.STATE_DISCONNECTED);
        } else {
            return;
        }

        if (mBluetoothHeadsetState == BluetoothHeadset.STATE_CONNECTED || mBluetoothA2dpConnected ||
                mBluetoothPbapState == BluetoothPbap.STATE_CONNECTED) {
            iconId = setDrawable("stat_sys_data_bluetooth_connected");
        }

        setBackgroundDrawable(iconId);
        if(mBluetoothEnabled){
            setVisibility(VISIBLE);
        }else{
            setVisibility(GONE);
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
