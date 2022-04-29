// Copyright (c) 2020 Zebra Technologies Corporation and/or its affiliates. All rights reserved.
package com.zebra.truckloadingdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CustomMessage extends AppCompatActivity {

    private String mCaller;
    private BeepController mBeepController;
    private GestureDetector mGestureDetector;
    private View mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBeepController = new BeepController(this);
        mCaller = getIntent().getExtras().getString("caller");

        setContentView(R.layout.custom_message);

        //When in custom message view, will send message on IME done event
        EditText textInput = findViewById(R.id.textInput);
        textInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    messageSent();
                    return true;
                }
                return false;
            }
        });

        mView = findViewById(R.id.custom_message);
        mView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });

        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            //Gesture detector picks up onDown(press) and OnSingleTapUp(release) before onFling(swipe)
            //So they must return true if we want the gesture detector to also handle the swipe
            @Override
            public boolean onDown(MotionEvent event) {return true;}

            @Override
            public boolean onSingleTapUp(MotionEvent event) { return true; }

            //Go back to Custom Message screen on swipe back
            @Override
            public boolean onFling (MotionEvent event1, MotionEvent event2,
                                    float velocityX, float velocityY){
                if(velocityX > 0){
                    Intent custom = new Intent(CustomMessage.this, ContactSupervisor.class);
                    custom.putExtra("caller", mCaller);
                    startActivity(custom);
                    finish();
                    return false;
                }
                return true;
            }

        });
    }

    /**
     * Shows message confirmation layout for 1.5s, then calls returnToCaller()
     */
    private void messageSent(){
        setContentView(R.layout.confirm_message_sent);
        mBeepController.beep(true);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            @Override
            public void run(){
                returnToCaller();
            }
        }, 1500);
    }

    /**
     * Start the activity which started ContactSupervisor. End this activity
     */
    private void returnToCaller(){
        Intent returnFrom;

        if(mCaller.equals("badge")) returnFrom = new Intent(CustomMessage.this, MainActivity.class);
        else returnFrom = new Intent(CustomMessage.this, PackageScan.class);

        startActivity(returnFrom);
        finish();
    }


    //Sends message if user pressed enter on physical keyboard (emulator)
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //When physical Enter key is pressed in custom message screen, send message
        if(keyCode == KeyEvent.KEYCODE_ENTER) {
            messageSent();
            return true;
        }
        //Goes back to ContactSupervisor
        else if (keyCode == KeyEvent.KEYCODE_BACK){
            Intent custom = new Intent(CustomMessage.this, ContactSupervisor.class);
            custom.putExtra("caller", mCaller);
            startActivity(custom);
            finish();
            return true;
        }
        else return super.onKeyUp(keyCode, event);
    }
}
