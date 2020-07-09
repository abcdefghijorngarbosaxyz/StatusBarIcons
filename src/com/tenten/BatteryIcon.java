package com.tenten;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BatteryIcon extends ImageView {
    
    private boolean mAttached;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isCharging = false;
            int level = intent.getIntExtra("level", 0);
            int status = intent.getIntExtra("status", 0);
            setImageLevel(level);
            if (status == BatteryManager.BATTERY_STATUS_CHARGING || 
                status == BatteryManager.BATTERY_STATUS_FULL)
                isCharging = true;
            if (isCharging) {
                setImageLevel(level);
                setImageDrawable(setDrawable("stat_sys_battery_charge"));
            } else
                setImageDrawable(setDrawable("stat_sys_battery"));
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mAttached) {
            mAttached = true;
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            getContext().registerReceiver(broadcastReceiver, intentFilter, null, getHandler());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            getContext().unregisterReceiver(broadcastReceiver);
            mAttached = false;
        }
    }

    public BatteryIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Drawable setDrawable(String name) {
        try {
            Resources resources = getContext().getPackageManager().getResourcesForApplication("android");
            return resources.getDrawable(resources.getIdentifier(name, "drawable", "android"));
        } catch (PackageManager.NameNotFoundException nameNotFoundException) {
            nameNotFoundException.printStackTrace();
            return null;
        }
    }
}
