package com.suprema.biominisample;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class MobileInternetConnectionDetector {
    private Context context;

    public MobileInternetConnectionDetector(Context _context) {
        this.context = _context;
    }

    public boolean checkMobileInternetConn(){
        ConnectivityManager connectivity =(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivity != null){
            NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if(info != null){
                if(info.isConnected()){
                    return true;
                }
            }
        }
        return false;
    }
}
