package com.suprema.biominisample;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class WIFIInternetConnectionDetector {
    private Context context;
    public WIFIInternetConnectionDetector(Context _context) {
        context = _context;
    }
    public boolean checkMobileInternetConn(){
        ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivity!=null)
        {
            NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if(info != null){
                if(info.isConnected())
                {
                    return true;
                }
            }
        }

        return false;
    }
}
