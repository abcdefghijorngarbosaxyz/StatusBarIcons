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
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.tenten.ButtonAnimation.R;

public class SignalIcon extends ImageView {
    SignalStrength mSignalStrength;
    ServiceState mServiceState;
    int mInetCondition;
    int mPhoneState = TelephonyManager.CALL_STATE_IDLE;
    private final Drawable[][] sSignalImages = {
            { setDrawable("stat_sys_signal_0"),
                    setDrawable("stat_sys_signal_1"),
                    setDrawable("stat_sys_signal_2"),
                    setDrawable("stat_sys_signal_3"),
                    setDrawable("stat_sys_signal_4")},
            { setDrawable("stat_sys_signal_0_fully"),
                    setDrawable("stat_sys_signal_1_fully"),
                    setDrawable("stat_sys_signal_2_fully"),
                    setDrawable("stat_sys_signal_3_fully"),
                    setDrawable("stat_sys_signal_4_fully")}
    };
    private final Drawable[][] sSignalImages_r = {
            { setDrawable("stat_sys_r_signal_0"),
                    setDrawable("stat_sys_r_signal_1"),
                    setDrawable("stat_sys_r_signal_2"),
                    setDrawable("stat_sys_r_signal_3"),
                    setDrawable("stat_sys_r_signal_4")},
            { setDrawable("stat_sys_r_signal_0_fully"),
                    setDrawable("stat_sys_r_signal_1_fully"),
                    setDrawable("stat_sys_r_signal_2_fully"),
                    setDrawable("stat_sys_r_signal_3_fully"),
                    setDrawable("stat_sys_r_signal_4_fully")}
    };
    private TelephonyManager mPhone;
    private boolean mAlwaysUseCdmaRssi;
    public SignalIcon(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        mAlwaysUseCdmaRssi = true;
        setImageResource(R.drawable.stat_sys_signal_null);
        mPhone = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        mSignalStrength = new SignalStrength();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.INET_CONDITION_ACTION);
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                NetworkInfo info = intent.getParcelableExtra(
                        ConnectivityManager.EXTRA_NETWORK_INFO);
                int connectionStatus = intent.getIntExtra(ConnectivityManager.EXTRA_INET_CONDITION, 0);

                int inetCondition = (connectionStatus > 50 ? 1 : 0);

                switch (info.getType()) {
                    case ConnectivityManager.TYPE_MOBILE:
                        mInetCondition = inetCondition;
                        updateSignalStrength();
                        break;
                }
            }
        }, mFilter);

        PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                mSignalStrength = signalStrength;
                updateSignalStrength();
            }

            @Override
            public void onServiceStateChanged(ServiceState state) {
                mServiceState = state;
                updateSignalStrength();
            }

            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (isCdma()) {
                    updateSignalStrength();
                }
            }
        };
        ((TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE))
                .listen(mPhoneStateListener,
                        PhoneStateListener.LISTEN_SERVICE_STATE
                                | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                                | PhoneStateListener.LISTEN_CALL_STATE
                                | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                                | PhoneStateListener.LISTEN_DATA_ACTIVITY);

    }

    private void updateSignalStrength() {
        int iconLevel = -1;
        Drawable[] iconList;

        Drawable mPhoneSignalIconId;
        if (mServiceState == null || (!hasService() && !mServiceState.isEmergencyOnly())) {
            if (Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) == 1) {
                mPhoneSignalIconId = setDrawable("stat_sys_signal_flightmode");
            } else {
                mPhoneSignalIconId = setDrawable("stat_sys_signal_null");
            }
            setImageDrawable(mPhoneSignalIconId);
            return;
        }

        if (!isCdma()) {
            int asu = mSignalStrength.getGsmSignalStrength();
            if (asu <= 2 || asu == 99) iconLevel = 0;
            else if (asu >= 12) iconLevel = 4;
            else if (asu >= 8)  iconLevel = 3;
            else if (asu >= 5)  iconLevel = 2;
            else iconLevel = 1;

            if (mPhone.isNetworkRoaming()) {
                iconList = sSignalImages_r[mInetCondition];
            } else {
                iconList = sSignalImages[mInetCondition];
            }
        } else {
            iconList = sSignalImages[mInetCondition];

            if ((mPhoneState == TelephonyManager.CALL_STATE_IDLE) && isEvdo()
                    && !mAlwaysUseCdmaRssi) {
                iconLevel = getEvdoLevel();
            } else {
                iconLevel = getCdmaLevel();
            }
        }
        mPhoneSignalIconId = iconList[iconLevel];
        setImageDrawable(mPhoneSignalIconId);

    }

    private boolean isCdma() {
        return (mSignalStrength != null) && !mSignalStrength.isGsm();
    }

    private boolean hasService() {
        if (mServiceState != null) {
            switch (mServiceState.getState()) {
                case ServiceState.STATE_OUT_OF_SERVICE:
                case ServiceState.STATE_POWER_OFF:
                    return false;
                default:
                    return true;
            }
        } else {
            return false;
        }
    }

    private int getEvdoLevel() {
        int evdoDbm = mSignalStrength.getEvdoDbm();
        int evdoSnr = mSignalStrength.getEvdoSnr();
        int levelEvdoDbm = 0;
        int levelEvdoSnr = 0;

        if (evdoDbm >= -65) levelEvdoDbm = 4;
        else if (evdoDbm >= -75) levelEvdoDbm = 3;
        else if (evdoDbm >= -90) levelEvdoDbm = 2;
        else if (evdoDbm >= -105) levelEvdoDbm = 1;
        else levelEvdoDbm = 0;

        if (evdoSnr >= 7) levelEvdoSnr = 4;
        else if (evdoSnr >= 5) levelEvdoSnr = 3;
        else if (evdoSnr >= 3) levelEvdoSnr = 2;
        else if (evdoSnr >= 1) levelEvdoSnr = 1;
        else levelEvdoSnr = 0;

        return (levelEvdoDbm < levelEvdoSnr) ? levelEvdoDbm : levelEvdoSnr;
    }
    private int getCdmaLevel() {
        final int cdmaDbm = mSignalStrength.getCdmaDbm();
        final int cdmaEcio = mSignalStrength.getCdmaEcio();
        int levelDbm = 0;
        int levelEcio = 0;

        if (cdmaDbm >= -75) levelDbm = 4;
        else if (cdmaDbm >= -85) levelDbm = 3;
        else if (cdmaDbm >= -95) levelDbm = 2;
        else if (cdmaDbm >= -100) levelDbm = 1;
        else levelDbm = 0;

        if (cdmaEcio >= -90) levelEcio = 4;
        else if (cdmaEcio >= -110) levelEcio = 3;
        else if (cdmaEcio >= -130) levelEcio = 2;
        else if (cdmaEcio >= -150) levelEcio = 1;
        else levelEcio = 0;

        return (levelDbm < levelEcio) ? levelDbm : levelEcio;
    }
    private boolean isEvdo() {
        return ( (mServiceState != null)
                && ((mServiceState.getRadioTechnology()
                == ServiceState.RADIO_TECHNOLOGY_EVDO_0)
                || (mServiceState.getRadioTechnology()
                == ServiceState.RADIO_TECHNOLOGY_EVDO_A)
                || (mServiceState.getRadioTechnology()
                == ServiceState.RADIO_TECHNOLOGY_EVDO_B)));
    }
    public Drawable setDrawable(String mDrawableName){
        final String packName = "com.android.systemui";
        int mDrawableResID = 0 ;
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
