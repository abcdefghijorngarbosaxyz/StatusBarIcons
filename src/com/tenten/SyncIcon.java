package com.tenten;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SyncIcon extends ImageView{
    public SyncIcon(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        setImageResource(com.android.internal.R.drawable.stat_notify_sync_anim0);
        setVisibility(GONE);
        IntentFilter iF = new IntentFilter();
        iF.addAction(Intent.ACTION_SYNC_STATE_CHANGED);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateSyncState(intent);
            }
        }, iF);
    }

    private final void updateSyncState(Intent intent) {
        boolean isActive = intent.getBooleanExtra("active", false);
        boolean isFailing = intent.getBooleanExtra("failing", false);
        if(isActive){
            setVisibility(VISIBLE);
        }else{
            setVisibility(GONE);
        }
    }
}
