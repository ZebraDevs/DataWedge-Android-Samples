// Copyright (c) 2020-2021 Zebra Technologies Corporation and/or its affiliates. All rights reserved.
package com.zebra.truckloadingdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class LoginResponse extends AppCompatActivity implements ScanReceiver.OnScanListener
{
    private View mView;
    private GestureDetector mGestureDetector;
    private BeepController mBeepController;
    private boolean mIsGoodScan;

    private LedController mLedController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mLedController = new LedController(this);

        mBeepController = new BeepController(this);

        final ScanReceiver scanReceiver = new ScanReceiver(this);
        scanReceiver.setOnScanListener(this);

        //Checking intent from the scan, calling scanResponse
        mIsGoodScan = getIntent().getBooleanExtra("isGoodScan", false);
        scanResponse(mIsGoodScan);

        mGestureDetector = new GestureDetector(LoginResponse.this ,new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent event) {return true;}

            @Override
            public boolean onSingleTapUp(MotionEvent event) { return true; }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent event){
                if (mIsGoodScan) return false;

                Intent loginIntent = new Intent(LoginResponse.this, MainActivity.class);
                startActivity(loginIntent);
                finish();

                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent event){ return false;}
        });
    }

    /**
     * Reacts to good/bad scan. Change layout, beep, start intent, etc..
     *
     * @param isGoodScan 1 if scan was good, 0 if scan was bad
     */
    private void scanResponse (final boolean isGoodScan){
        if(isGoodScan){
            setContentView(R.layout.green_check);
            mView = findViewById(R.id.green_check);

            mBeepController.beep(true);

            new Handler().postDelayed(() -> mLedController.sendAndClearLED(true), 30);

            final Intent startScanning;
            startScanning = new Intent(LoginResponse.this, AreaDisplay.class);

            new Handler().postDelayed(() -> {
                startActivity(startScanning);
                finish();
            }, 1000);
        }
        else {
            setContentView(R.layout.login_denied);
            mView = findViewById(R.id.login_denied);
            mView.setOnTouchListener((v, event) -> mGestureDetector.onTouchEvent(event));
            mBeepController.beep(false);

            new Handler().postDelayed(() -> mLedController.sendAndClearLED(false), 30);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            return true;
        }
        else return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onScan(String scan) {
        if(scan != null && scan.equalsIgnoreCase("badge")) scanResponse(true);
        else scanResponse(false);
    }

    /**
     * When the Contact Manager button is clicked, start the Contact Supervisor activity
     * @param view - Contact Supervisor button in login_denied.xml
     */
    public void contactSupervisor (View view){
        Intent contact = new Intent(LoginResponse.this, ContactSupervisor.class);
        contact.putExtra("caller", "badge");
        startActivity(contact);
        finish();
    }

    /**
     * Called by Go Back button
     * returns the app to the login activity (MainActivity)
     * @param view - GoBack button in login_denied
     */
    public void goBack (View view){
        Intent loginIntent = new Intent(LoginResponse.this, MainActivity.class);
        startActivity(loginIntent);
        finish();
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        mLedController.closeService();
        mLedController = null;
    }
}
