package com.tenten;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.widget.ImageView;

public class VolumeIcon extends ImageView {
    private boolean mVolumeVisible;
    public VolumeIcon(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        setImageDrawable(setDrawable("stat_sys_ringer_silent"));
        setVisibility(GONE);
        updateVolume();

        IntentFilter iF = new IntentFilter();
        iF.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        iF.addAction(AudioManager.VIBRATE_SETTING_CHANGED_ACTION);

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateVolume();
            }
        }, iF);
    }

    private void updateVolume() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        final int ringerMode = audioManager.getRingerMode();
        final boolean visible = ringerMode == AudioManager.RINGER_MODE_SILENT ||
                ringerMode == AudioManager.RINGER_MODE_VIBRATE;
        final Drawable iconId = audioManager.shouldVibrate(AudioManager.VIBRATE_TYPE_RINGER)
                ? setDrawable("stat_sys_ringer_vibrate")
                : setDrawable("stat_sys_ringer_silent");

        if (visible) {
            setImageDrawable(iconId);
        }
        if (visible != mVolumeVisible) {
            if(visible){
                setVisibility(VISIBLE);
            }else{
                setVisibility(GONE);
            }
            mVolumeVisible = visible;
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
