// Copyright (c) 2020-2021 Zebra Technologies Corporation and/or its affiliates. All rights reserved.
package com.zebra.truckloadingdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class LogoutResponse extends AppCompatActivity {
    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.logout);

        mHandler = new Handler();
        mHandler.postDelayed(this::returnFromLogout, 10000);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_F3) {
            returnFromLogout();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_F4) {
            logoutClicked();
            return true;
        } else return super.onKeyUp(keyCode, event);
    }

    /**
     * Logs out - Starts an intent that goes back to main activity
     *
     * @param view - Connects to Log Out button in logout*.xml files
     */
    public void logoutClicked(View view) {
        logoutClicked();
    }

    /**
     * Logs out - Starts an intent that goes back to main activity
     */
    public void logoutClicked() {
        mHandler.removeCallbacksAndMessages(null);

        Intent logout;
        logout = new Intent(LogoutResponse.this, MainActivity.class);
        startActivity(logout);
        finish();
    }

    /**
     * Starts an intent that goes back to the activity that started the current activity
     *
     * @param view - Connects to Cancel button in logout*.xml files
     */
    public void returnFromLogout(View view) {
        returnFromLogout();
    }

    /**
     * Starts an intent that goes back to the activity that started the current activity
     */
    public void returnFromLogout() {
        mHandler.removeCallbacksAndMessages(null);

        Intent returnFrom;
        String caller = getIntent().getExtras().getString("caller");

        if (caller.equals("area")) returnFrom = new Intent(LogoutResponse.this, AreaDisplay.class);
        else returnFrom = new Intent(LogoutResponse.this, PackageScan.class);

        startActivity(returnFrom);
        finish();
    }
}
