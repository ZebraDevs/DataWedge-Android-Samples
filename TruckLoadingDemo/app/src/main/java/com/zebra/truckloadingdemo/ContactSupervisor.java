// Copyright (c) 2020 Zebra Technologies Corporation and/or its affiliates. All rights reserved.
package com.zebra.truckloadingdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class ContactSupervisor extends AppCompatActivity {

    private String mCaller;
    private BeepController mBeepController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBeepController = new BeepController(this);

        //caller can have value "badge" if this activity was started from LoginResponse.java
        //or "package" if it was called from PackageScan.java
        mCaller = getIntent().getExtras().getString("caller");

        if(mCaller.equals("badge")) setContentView(R.layout.contact_supervisor_badge);
        else setContentView(R.layout.contact_supervisor_package);
    }

    /**
     * Calls messageSent(), but takes in a view as a param
     * @param view - Any button with a canned answer from contact_supervisor* xml layout files
     */
    public void cannedAnswerClicked(View view){ messageSent(); }


    /**
     * Calls returnToCaller(), but takes in a view as a param
     * @param view - Cancel button in the contact_supervisor* xml layout files
     */
    public void cancelClicked(View view){ returnToCaller(); }

    /**
     * Sets layout to custom message input layout. Listener executes messageSent() on done event
     * @param view - Custom Message button in the contact_supervisor* xml layout files
     */
    public void customClicked (View view){
        Intent custom = new Intent(ContactSupervisor.this, CustomMessage.class);
        custom.putExtra("caller", mCaller);
        startActivity(custom);
        finish();
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
     * Start the activity which started this one. End this activity
     */
    private void returnToCaller(){
        Intent returnFrom;

        if(mCaller.equals("badge")) returnFrom = new Intent(ContactSupervisor.this, MainActivity.class);
        else returnFrom = new Intent(ContactSupervisor.this, PackageScan.class);

        startActivity(returnFrom);
        finish();
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //Goes back to caller on key back
        if (keyCode == KeyEvent.KEYCODE_BACK){
            returnToCaller();
            return true;
        }
        else return super.onKeyUp(keyCode, event);
    }

}