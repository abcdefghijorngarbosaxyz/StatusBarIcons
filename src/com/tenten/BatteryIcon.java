package com.tenten;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BatteryIcon extends ImageView {
    public BatteryIcon(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent i) {
                int level = i.getIntExtra("level", 0);
                setImageLevel(level);

                int status = i.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;
                if (isCharging){
                    setImageLevel(level);
                    setImageResource(com.android.internal.R.drawable.stat_sys_battery_charge);
                }
                else {
                    setImageResource(com.android.internal.R.drawable.stat_sys_battery);
                }
            }
        }, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }
}
