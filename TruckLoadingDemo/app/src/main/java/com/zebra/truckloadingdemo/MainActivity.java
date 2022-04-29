// Copyright (c) 2020-2021 Zebra Technologies Corporation and/or its affiliates. All rights reserved.
package com.zebra.truckloadingdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements ScanReceiver.OnScanListener{

    private View mView;
    public static Context mContext;
    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mContext = getApplicationContext();

        //Set layout
        setContentView(R.layout.login);
        mView = findViewById(R.id.login);
        mView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });

        //Set up the ScanReceiver, and it's listener
        final ScanReceiver scanReceiver = new ScanReceiver(this);
        scanReceiver.setOnScanListener(this);

        //This gesture detector simply implements 1 tap -> good scan,  2 taps -> bad scan
        //for temporary testing convenience
        mGestureDetector = new GestureDetector(MainActivity.this ,new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent event) {return true;}

            @Override
            public boolean onSingleTapUp(MotionEvent event) { return true; }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent event){

                Intent loginIntent = new Intent(MainActivity.this, LoginResponse.class);
                loginIntent.putExtra("isGoodScan", true);

                startActivity(loginIntent);
                finish();
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent event){

                Intent loginIntent = new Intent(MainActivity.this, LoginResponse.class);
                loginIntent.putExtra("isGoodScan", 0);

                startActivity(loginIntent);
                finish();
                return false;
            }

            //Exit app on swipe back
            @Override
            public boolean onFling (MotionEvent event1, MotionEvent event2,
                                    float velocityX, float velocityY){
                if(velocityX > 0){
                    finish();
                    return false;
                }
                return true;
            }

        });
    }

    //This function is called in ScanReceiver.java myBroadcastReceiver
    //upon receiving the scan intent from datawedge
    @Override
    public void onScan(String scan){

        Intent loginIntent;
        loginIntent = new Intent(MainActivity.this, LoginResponse.class);

        if(scan != null && scan.equalsIgnoreCase("badge")) {//Scan was successful
            loginIntent.putExtra("isGoodScan", true);
        }
        else { //Scan was unsuccessful
            loginIntent.putExtra("isGoodScan", false);
        }

        startActivity(loginIntent);
        finish();
    }
}
