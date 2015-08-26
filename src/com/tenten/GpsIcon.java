package com.tenten;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.util.AttributeSet;
import android.widget.ImageView;

public class GpsIcon extends ImageView {
    public GpsIcon(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        setImageDrawable(setDrawable("stat_sys_gps_acquiring_anim"));
        setVisibility(GONE);

        IntentFilter iF = new IntentFilter();
        iF.addAction(LocationManager.GPS_ENABLED_CHANGE_ACTION);
        iF.addAction(LocationManager.GPS_FIX_CHANGE_ACTION);

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateGps(intent);
            }
        }, iF);
    }

    private final void updateGps(Intent intent) {
        final String action = intent.getAction();
        final boolean enabled = intent.getBooleanExtra(LocationManager.EXTRA_GPS_ENABLED, false);

        if (action.equals(LocationManager.GPS_FIX_CHANGE_ACTION) && enabled) {
            // GPS is getting fixes
            setImageResource(com.android.internal.R.drawable.stat_sys_gps_on);
            setVisibility(VISIBLE);
        } else if (action.equals(LocationManager.GPS_ENABLED_CHANGE_ACTION) && !enabled) {
            // GPS is off
            setVisibility(GONE);
        } else {
            // GPS is on, but not receiving fixes
            setImageDrawable(setDrawable("stat_sys_gps_acquiring_anim"));
            setVisibility(VISIBLE);
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
