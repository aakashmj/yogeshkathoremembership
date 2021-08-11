package com.yogeshkathore.foundation.firebase;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private final NetworkChangeCallback callback;

    public NetworkChangeReceiver(NetworkChangeCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean status = isNetworkAvailable(context);
        showLog("NetStatus" + status);
        if (callback != null) {
            callback.onNetworkChanged(status);
        }

    }

    private boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
            return (activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting());
        } catch (NullPointerException e) {
            showLog(e.getLocalizedMessage());
            return false;
        }
    }

    private void showLog(String message) {
        Log.e("NetworkChangeReceiver", "" + message);
    }
}
