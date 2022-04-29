// Copyright (c) 2020 Zebra Technologies Corporation and/or its affiliates. All rights reserved.
package com.zebra.truckloadingdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.zebra.led.ILed;

import static com.zebra.led.ILed.LED_LEFT;

public class LedController
{
    private static final String TAG = "LedTest";
    public ILed mLedService = null;
    private Context mContext;

    /**
     * Monitors the connection to the LED service.
     */
    private ServiceConnection ledConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mLedService = ILed.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mLedService = null;
        }
    };

    //Bind the LED service to the calling activity
    LedController(Context context)
    {
        mContext = context;
        Intent intent = new Intent().setComponent(new ComponentName("com.zebra.led", "com.zebra.led.LedService"));
        context.bindService(intent, ledConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Unbind the led service
     */
    public void closeService(){
        mContext.unbindService(ledConnection);
    }

    /**
     * Sets the LEDs. green/red for good/bad scan respectively
     * Then clears the LEDs after 1s
     * @param isGoodScan - 1 if good scan, 0 if bad scan
     */
    public void sendAndClearLED(boolean isGoodScan)
    {
        if(isGoodScan){
            sendLedCommand(Color.GREEN);
        }
        else{
            sendLedCommand(Color.RED);
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            @Override
            public void run(){
                sendLedCommand(Color.BLACK);
            }
        }, 1000);

    }

    /**
     * A utility function called when a button is pressed. Uses the supplied
     * args to send a LED control request to the AIDL layer.
     *
     * @param color The desired colour (0xAARRGGBB format, eg Color.GREEN)
     */
    private void sendLedCommand(int color)
    {
        if (mLedService == null) return;

        try {
            mLedService.setLed(LED_LEFT, color);
        } catch (RemoteException e) {
            Log.e(TAG,"Failed to set led "+ LED_LEFT +". Err: "+ e);
        }
    }

}
