package com.suprema.biominisample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.telpo.tps550.api.fingerprint.FingerPrint;

import androidx.annotation.Nullable;


public class SplashActivity extends Activity {
    Handler handler = new Handler();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FingerPrint.fingerPrintPower(1);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
            //  Intent intent = new Intent(getApplicationContext(),DefaultActivity.class);
                Intent intent = new Intent(SplashActivity.this,Clockin_Login.class);
                    startActivity(intent);
                    finish();
            }
        },1000);
    }
}
