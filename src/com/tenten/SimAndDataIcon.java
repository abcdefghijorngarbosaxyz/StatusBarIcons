package com.tenten;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.internal.app.IBatteryStats;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.TelephonyIntents;
import com.android.server.am.BatteryStatsService;
import android.net.NetworkInfo;

public class SimAndDataIcon extends ImageView {
    private boolean mHspaDataDistinguishable;
    private int mInetCondition = 0;
    private boolean mDataIconVisible;
    static Context c;
    private final IBatteryStats mBatteryStats;
    SignalStrength mSignalStrength;
    IccCard.State mSimState = IccCard.State.READY;
    ServiceState mServiceState;
    int mDataState = TelephonyManager.DATA_DISCONNECTED;
    int mDataActivity = TelephonyManager.DATA_ACTIVITY_NONE;
    private TelephonyManager mPhone;
    private Drawable[][] sDataNetType_g = {
            { setDrawable("stat_sys_data_connected_g"),
                    setDrawable("stat_sys_data_in_g"),
                    setDrawable("stat_sys_data_out_g"),
                    setDrawable("stat_sys_data_inandout_g")},
            { setDrawable("stat_sys_data_fully_connected_g"),
                    setDrawable("stat_sys_data_fully_in_g"),
                    setDrawable("stat_sys_data_fully_out_g"),
                    setDrawable("stat_sys_data_fully_inandout_g")}
    };
    private Drawable[] mDataIconList = sDataNetType_g[0];
    private final Drawable[][] sDataNetType_e = {
            { setDrawable("stat_sys_data_connected_e"),
                    setDrawable("stat_sys_data_in_e"),
                    setDrawable("stat_sys_data_out_e"),
                    setDrawable("stat_sys_data_inandout_e") },
            { setDrawable("stat_sys_data_fully_connected_e"),
                    setDrawable("stat_sys_data_fully_in_e"),
                    setDrawable("stat_sys_data_fully_out_e"),
                    setDrawable("stat_sys_data_fully_inandout_e") }
    };
    private final Drawable[][] sDataNetType_h = {
            { setDrawable("stat_sys_data_connected_h"),
                    setDrawable("stat_sys_data_in_h"),
                    setDrawable("stat_sys_data_out_h"),
                    setDrawable("stat_sys_data_inandout_h") },
            { setDrawable("stat_sys_data_fully_connected_h"),
                    setDrawable("stat_sys_data_fully_in_h"),
                    setDrawable("stat_sys_data_fully_out_h"),
                    setDrawable("stat_sys_data_fully_inandout_h") }
    };
    private final Drawable[][] sDataNetType_3g = {
            { setDrawable("stat_sys_data_connected_3g"),
                    setDrawable("stat_sys_data_in_3g"),
                    setDrawable("stat_sys_data_out_3g"),
                    setDrawable("stat_sys_data_inandout_3g") },
            { setDrawable("stat_sys_data_fully_connected_3g"),
                    setDrawable("stat_sys_data_fully_in_3g"),
                    setDrawable("stat_sys_data_fully_out_3g"),
                    setDrawable("stat_sys_data_fully_inandout_3g") }
    };
    private final Drawable[][] sDataNetType_1x = {
            { setDrawable("stat_sys_data_connected_1x"),
                    setDrawable("stat_sys_data_in_1x"),
                    setDrawable("stat_sys_data_out_1x"),
                    setDrawable("stat_sys_data_inandout_1x") },
            { setDrawable("stat_sys_data_fully_connected_1x"),
                    setDrawable("stat_sys_data_fully_in_1x"),
                    setDrawable("stat_sys_data_fully_out_1x"),
                    setDrawable("stat_sys_data_fully_inandout_1x") }
    };

    public SimAndDataIcon(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        try {
            mHspaDataDistinguishable = true;
        } catch (Exception e) {
            mHspaDataDistinguishable = false;
        }
        c = context;
        mPhone = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        mSignalStrength = new SignalStrength();
        mBatteryStats = BatteryStatsService.getService();
        setImageDrawable(setDrawable("stat_sys_data_connected_g"));
        setVisibility(GONE);
        IntentFilter iF = new IntentFilter();
        iF.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        IntentFilter iF2 = new IntentFilter();
        iF2.addAction(ConnectivityManager.INET_CONDITION_ACTION);
        iF2.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateConnectivity(intent);
            }
        }, iF2);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent i = new Intent("simupdate");
                i.putExtra(IccCard.INTENT_KEY_ICC_STATE, intent);
                context.sendBroadcast(i);
            }
        }, iF);

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateSimState(intent);
            }
        }, new IntentFilter("simupdate"));
        PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                mSignalStrength = signalStrength;
            }

            @Override
            public void onServiceStateChanged(ServiceState state) {
                mServiceState = state;
                updateDataIcon();
            }

            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
            }

            @Override
            public void onDataConnectionStateChanged(int state, int networkType) {
                mDataState = state;
                updateDataNetType(networkType);
                updateDataIcon();
            }

            @Override
            public void onDataActivity(int direction) {
                mDataActivity = direction;
                updateDataIcon();
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

    private void updateConnectivity(Intent intent) {
        NetworkInfo info = intent.getParcelableExtra(
                ConnectivityManager.EXTRA_NETWORK_INFO);
        int connectionStatus = intent.getIntExtra(ConnectivityManager.EXTRA_INET_CONDITION, 0);

        int inetCondition = (connectionStatus > 50 ? 1 : 0);

        switch (info.getType()) {
            case ConnectivityManager.TYPE_MOBILE:
                mInetCondition = inetCondition;
                updateDataNetType(info.getSubtype());
                updateDataIcon();
                break;
        }
    }
    private void updateDataNetType(int net) {
        switch (net) {
            case TelephonyManager.NETWORK_TYPE_EDGE:
                mDataIconList = sDataNetType_e[mInetCondition];
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                mDataIconList = sDataNetType_3g[mInetCondition];
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
                if (mHspaDataDistinguishable) {
                    mDataIconList = sDataNetType_h[mInetCondition];
                } else {
                    mDataIconList = sDataNetType_3g[mInetCondition];
                }
                break;
            case TelephonyManager.NETWORK_TYPE_CDMA:
                // display 1xRTT for IS95A/B
                mDataIconList = sDataNetType_1x[mInetCondition];
                break;
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                mDataIconList = sDataNetType_1x[mInetCondition];
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_0: //fall through
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                mDataIconList = sDataNetType_3g[mInetCondition];
                break;
            default:
                mDataIconList = sDataNetType_g[mInetCondition];
                break;
        }
    }
    private void updateSimState(Intent intent) {
        String stateExtra = intent.getStringExtra(IccCard.INTENT_KEY_ICC_STATE);
        if (IccCard.INTENT_VALUE_ICC_ABSENT.equals(stateExtra)) {
            mSimState = IccCard.State.ABSENT;
        } else if (IccCard.INTENT_VALUE_ICC_READY.equals(stateExtra)) {
            mSimState = IccCard.State.READY;
        } else if (IccCard.INTENT_VALUE_ICC_LOCKED.equals(stateExtra)) {
            final String lockedReason = intent.getStringExtra(IccCard.INTENT_KEY_LOCKED_REASON);
            if (IccCard.INTENT_VALUE_LOCKED_ON_PIN.equals(lockedReason)) {
                mSimState = IccCard.State.PIN_REQUIRED;
            } else if (IccCard.INTENT_VALUE_LOCKED_ON_PUK.equals(lockedReason)) {
                mSimState = IccCard.State.PUK_REQUIRED;
            } else {
                mSimState = IccCard.State.NETWORK_LOCKED;
            }
        } else {
            mSimState = IccCard.State.UNKNOWN;
        }
        updateDataIcon();
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
    private void updateDataIcon() {
        Drawable iconId;
        boolean visible = true;

        if (!isCdma()) {
            // GSM case, we have to check also the sim state
            if (mSimState == IccCard.State.READY || mSimState == IccCard.State.UNKNOWN) {
                if (hasService() && mDataState == TelephonyManager.DATA_CONNECTED) {
                    switch (mDataActivity) {
                        case TelephonyManager.DATA_ACTIVITY_IN:
                            iconId = mDataIconList[1];
                            break;
                        case TelephonyManager.DATA_ACTIVITY_OUT:
                            iconId = mDataIconList[2];
                            break;
                        case TelephonyManager.DATA_ACTIVITY_INOUT:
                            iconId = mDataIconList[3];
                            break;
                        default:
                            iconId = mDataIconList[0];
                            break;
                    }
                    setImageDrawable(iconId);
                } else {
                    visible = false;
                }
            } else {
                iconId = setDrawable("stat_sys_no_sim");
                setImageDrawable(iconId);
            }
        } else {
            // CDMA case, mDataActivity can be also DATA_ACTIVITY_DORMANT
            if (hasService() && mDataState == TelephonyManager.DATA_CONNECTED) {
                switch (mDataActivity) {
                    case TelephonyManager.DATA_ACTIVITY_IN:
                        iconId = mDataIconList[1];
                        break;
                    case TelephonyManager.DATA_ACTIVITY_OUT:
                        iconId = mDataIconList[2];
                        break;
                    case TelephonyManager.DATA_ACTIVITY_INOUT:
                        iconId = mDataIconList[3];
                        break;
                    case TelephonyManager.DATA_ACTIVITY_DORMANT:
                    default:
                        iconId = mDataIconList[0];
                        break;
                }
                setImageDrawable(iconId);
            } else {
                visible = false;
            }
        }

        long ident = Binder.clearCallingIdentity();
        try {
            mBatteryStats.notePhoneDataConnectionState(mPhone.getNetworkType(), visible);
        } catch (RemoteException ignored) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }

        if (mDataIconVisible != visible) {
            if(visible){
                setVisibility(VISIBLE);
            }else{
                setVisibility(GONE);
            }
            mDataIconVisible = visible;
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
