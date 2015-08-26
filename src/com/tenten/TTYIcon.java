package com.tenten;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.internal.telephony.cdma.TtyIntent;

public class TTYIcon extends ImageView {
    public TTYIcon(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        setImageDrawable(setDrawable("stat_sys_tty_mode"));
        setVisibility(GONE);

        IntentFilter iF = new IntentFilter();
        iF.addAction(TtyIntent.TTY_ENABLED_CHANGE_ACTION);

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateTTY(intent);
            }
        }, iF);
    }

    private void updateTTY(Intent intent) {
        final String action = intent.getAction();
        final boolean enabled = intent.getBooleanExtra(TtyIntent.TTY_ENABLED, false);

        if (enabled) {
            // TTY is on
            setImageDrawable(setDrawable("stat_sys_tty_mode"));
            setVisibility(VISIBLE);
        } else {
            // TTY is off
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
