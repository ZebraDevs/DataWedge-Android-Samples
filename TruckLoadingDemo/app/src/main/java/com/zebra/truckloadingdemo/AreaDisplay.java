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

public class AreaDisplay extends AppCompatActivity implements ScanReceiver.OnScanListener
{
    private View mView;
    GestureDetector mGestureDetector;
    private BeepController mBeepController;

    boolean mIsArea = true;
    boolean mGoodScan = false;

    private LedController mLedController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mLedController = new LedController(this);

        setLayoutArea();

        //Set up the ScanReceiver, and it's listener
        final ScanReceiver scanReceiver = new ScanReceiver(this);
        scanReceiver.setOnScanListener(this);

        mBeepController = new BeepController(this);

        //This gesture detector simply implements 1 tap -> good scan,  2 taps -> bad scan
        //for temporary testing convenience
        mGestureDetector = new GestureDetector(AreaDisplay.this ,new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent event) {return true;}

            @Override
            public boolean onSingleTapUp(MotionEvent event) { return true; }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent event){
                //Tap to go back to scan area screen
                if(mView == findViewById(R.id.scan_right_area)){
                    setLayoutArea();
                }
                //Simulates good area scan
                else if(mView != findViewById(R.id.green_check)){

                    setLayoutGreenCheck();

                    mBeepController.beep(true);
                    mLedController.sendAndClearLED(true);

                    final Intent startPackageScan = new Intent(AreaDisplay.this, PackageScan.class);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            startActivity(startPackageScan);
                            finish();
                        }
                    }, 1000);
                }
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent event){

                if(mView != findViewById(R.id.green_check)) {
                    setLayoutError();

                    mBeepController.beep(false);
                    mLedController.sendAndClearLED(false);
                }
                return false;
            }

            public void onLongPress(MotionEvent e) {
                if(mView == findViewById(R.id.goto_area)){
                    mBeepController.beep(true);
                    mLedController.sendAndClearLED(true);

                    Intent logout = new Intent(AreaDisplay.this, LogoutResponse.class);
                    logout.putExtra("caller", "area");
                    startActivity(logout);
                    finish();
                }
            }

        });
    }

    @Override
    public void onScan(String scan){

        if(scan != null){
            //Scan was successful
            if(scan.equalsIgnoreCase("Door 21")){
                mGoodScan = true;
                if(mView != findViewById(R.id.green_check)){
                    setLayoutGreenCheck();

                    mBeepController.beep(true);
                }

                mLedController.sendAndClearLED(true);

                final Intent startPackageScan = new Intent(AreaDisplay.this, PackageScan.class);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        startActivity(startPackageScan);
                        finish();
                    }
                }, 1000);
            }
            //Logout by scanning badge
            else if (scan.contains("badge") && mView == findViewById(R.id.goto_area)) {
                mBeepController.beep(true);
                mLedController.sendAndClearLED(true);

                Intent logout = new Intent(AreaDisplay.this, LogoutResponse.class);
                logout.putExtra("caller", "area");
                startActivity(logout);
                finish();
            }
            //Scanned wrong area
            else if(scan.equals("Door 22")){
                if(mView != findViewById(R.id.green_check)) {
                    setLayoutError();

                    mBeepController.beep(false);
                    mLedController.sendAndClearLED(false);
                }
            }
            //Scanned non-area barcode
            else{
                //Show red x screen for 1 second, then go back to scan area screen
                setContentView(R.layout.red_x);
                mView = findViewById(R.id.red_x);
                mBeepController.beep(false);
                mLedController.sendAndClearLED(false);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        if (!mGoodScan) setLayoutArea();
                    }
                }, 1000);
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            return true;
        }
        else return super.onKeyUp(keyCode, event);
    }

    /**
     * Sets layout to area screen, and sets global mIsArea to true
     */
    private void setLayoutArea(){
        setContentView(R.layout.goto_area);
        mView = findViewById(R.id.goto_area);

        mView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });
        mIsArea = true;
    }

    /**
     * Sets layout to error screen, and sets global mIsArea to false
     */
    private void setLayoutError(){
        setContentView(R.layout.scan_right_area);
        mView = findViewById(R.id.scan_right_area);

        mView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });
        mIsArea = false;
    }

    /**
     * Sets layout and view to green checkmark
     */
    private void setLayoutGreenCheck (){
        setContentView(R.layout.green_check);
        mView = findViewById(R.id.green_check);
        mView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });
    }

    /**
     * Called by Go Back button in Wrong Area screen
     * sets the screen back to the Area Display screen
     * @param view - GoBack button in scan_right_area
     */
    public void goBack (View view){
        setLayoutArea();
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        mLedController.closeService();
        mLedController = null;
    }
}
