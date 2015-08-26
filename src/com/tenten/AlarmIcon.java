package com.tenten;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AlarmIcon extends ImageView {
    boolean alarmSet = false;
    public AlarmIcon(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        IntentFilter iF = new IntentFilter();
        iF.addAction(Intent.ACTION_ALARM_CHANGED);

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                alarmSet = intent.getBooleanExtra("alarmSet", false);
                if(alarmSet){
                    setVisibility(VISIBLE);
                }else{
                    setVisibility(GONE);
                }
                Settings.System.putInt( context.getContentResolver(), "alarming", alarmSet ? 1 : 0);
            }
        }, iF);

        setImageDrawable(setDrawable("stat_notify_alarm"));
        if(Settings.System.getInt(context.getContentResolver(), "alarming", 0) == 1){
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
