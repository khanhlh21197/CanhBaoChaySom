package com.example.firewarning.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkStateMonitor extends BroadcastReceiver {
    Context mContext;
    boolean mIsUp;

    public interface Listener {
        void onNetworkStateChange(boolean up);
    }

    public NetworkStateMonitor(Context context) {
        mContext = context;
        //mListener = (Listener)context;
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(this, intentFilter);
        mIsUp = isUp();
    }

    /**
     * call this when finished with it, and no later than onStop(): callback will crash if app has been destroyed
     */
    public void unregister() {
        mContext.unregisterReceiver(this);
    }

    /*
     * can be called at any time
     */
    public boolean isUp() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    /**
     * registerReceiver callback, passed to mListener
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean upNow = isUp();
        if (upNow == mIsUp) return;     // no change
        mIsUp = upNow;
        ((Listener) mContext).onNetworkStateChange(mIsUp);
    }
}