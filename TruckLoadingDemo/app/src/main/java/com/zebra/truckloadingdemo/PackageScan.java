// Copyright (c) 2020-2022 Zebra Technologies Corporation and/or its affiliates. All rights reserved.
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

public class PackageScan extends AppCompatActivity implements ScanReceiver.OnScanListener {
    private View mView;
    private GestureDetector mGestureDetector;
    private BeepController mBeepController;

    boolean mIsPackage = true;

    private LedController mLedController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mLedController = new LedController(this);

        setLayoutPackage();

        //Set up the ScanReceiver, and it's listener
        final ScanReceiver scanReceiver = new ScanReceiver(this);
        scanReceiver.setOnScanListener(this);

        mBeepController = new BeepController(this);

        //This gesture detector simply implements 1 tap -> good scan,  2 taps -> bad scan
        //for temporary testing convenience
        mGestureDetector = new GestureDetector(PackageScan.this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent event) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent event) {
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                //Tap to go back to scan package screen
                if (mView == findViewById(R.id.bad_package)) {
                    setLayoutPackage();
                }
                //Simulates good package scan
                else {
                    setLayoutGreenCheck();

                    mBeepController.beep(true);
                    mLedController.sendAndClearLED(true);

                    new Handler().postDelayed(() -> setLayoutPackage(), 1000);
                }
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent event) {
                if (mView == findViewById(R.id.scan_package)) {
                    setLayoutError();

                    mBeepController.beep(false);
                    mLedController.sendAndClearLED(false);
                }
                return false;
            }

            public void onLongPress(MotionEvent e) {
                if (mView == findViewById(R.id.scan_package)) {
                    mBeepController.beep(true);
                    mLedController.sendAndClearLED(true);

                    Intent logout = new Intent(PackageScan.this, LogoutResponse.class);
                    logout.putExtra("caller", "package");
                    startActivity(logout);
                    finish();
                }
            }
        });

        mView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });

    }

    @Override
    public void onScan(String scan) {
        //Reject the scan if previous bad scan was not dealt with
        if (mView == findViewById(R.id.bad_package)) {
            mBeepController.beep(false);
            return;
        }

        if (scan != null) {
            //Chute finished, return to AreaDisplay activity
            if (scan.contains("Door") && mView == findViewById(R.id.scan_package)) {
                mBeepController.beep(true);
                mLedController.sendAndClearLED(true);

                Intent newArea = new Intent(PackageScan.this, AreaDisplay.class);
                startActivity(newArea);
                finish();
                return;
            }
            //Logout by scanning badge
            else if (scan.contains("badge") && mView == findViewById(R.id.scan_package)) {
                mBeepController.beep(true);
                mLedController.sendAndClearLED(true);

                Intent logout = new Intent(PackageScan.this, LogoutResponse.class);
                logout.putExtra("caller", "package");
                startActivity(logout);
                finish();
            } else {
                //Scan was successful
                if (scan.contains("package")) {
                    setLayoutGreenCheck();

                    mBeepController.beep(true);
                    mLedController.sendAndClearLED(true);

                    new Handler().postDelayed(() -> setLayoutPackage(), 1000);
                } else {
                    //Scan was unsuccessful
                    setLayoutError();

                    mBeepController.beep(false);
                    mLedController.sendAndClearLED(false);
                }
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //KEYCODE L simulates area scan - go back to AreaDisplay activity
        if (keyCode == KeyEvent.KEYCODE_L && mView == findViewById(R.id.scan_package)) {
            mBeepController.beep(true);
            mLedController.sendAndClearLED(true);

            Intent newArea = new Intent(PackageScan.this, AreaDisplay.class);
            startActivity(newArea);
            finish();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        } else return super.onKeyUp(keyCode, event);
    }

    /**
     * Sets layout to scan package screen and sets global mIsPackage to true
     */
    private void setLayoutPackage() {
        setContentView(R.layout.scan_package);

        mView = findViewById(R.id.scan_package);
        mView.setOnTouchListener((v, event) -> mGestureDetector.onTouchEvent(event));
        mIsPackage = true;
    }

    /**
     * Sets layout to bad package screen and sets global mIsPackage to false
     */
    private void setLayoutError() {
        setContentView(R.layout.bad_package);
        mView = findViewById(R.id.bad_package);

        mView.setOnTouchListener((v, event) -> mGestureDetector.onTouchEvent(event));
        mIsPackage = false;
    }

    /**
     * Connects to Done button in bad_package.xml
     * Sets layout to package screens
     */
    public void doneClicked(View view) {
        setLayoutPackage();
    }

    /**
     * Sets layout and view to green check
     */
    private void setLayoutGreenCheck() {
        setContentView(R.layout.green_check);
        mView = findViewById(R.id.green_check);
        mView.setOnTouchListener((v, event) -> mGestureDetector.onTouchEvent(event));
    }

    /**
     * When the Contact Manager button is clicked, start the Contact Supervisor activity
     *
     * @param view - Contact Supervisor button in bad_package.xml
     */
    public void contactSupervisor(View view) {
        Intent contact = new Intent(PackageScan.this, ContactSupervisor.class);
        contact.putExtra("caller", "package");
        startActivity(contact);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLedController.closeService();
        mLedController = null;
    }
}
