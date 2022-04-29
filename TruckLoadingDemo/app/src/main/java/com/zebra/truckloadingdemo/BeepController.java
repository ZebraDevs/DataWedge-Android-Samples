// Copyright (c) 2020 Zebra Technologies Corporation and/or its affiliates. All rights reserved.
package com.zebra.truckloadingdemo;

import android.content.Context;
import android.media.MediaPlayer;

public class BeepController {
    private final static String TAG = "BeepCtrl";

    private final Context mContext;

    public BeepController(Context context){
        mContext = context;
    }

    /**
     * Calls playSound based on if the scan was good/bad
     * @param isGoodScan - which beep needs to be played
     */
    public void beep(boolean isGoodScan){
        playSound(isGoodScan ? R.raw.beep_good : R.raw.beep_bad);
    }

    /**
     * Plays mp3 file
     * @param resId specifies which mp3 file to play
     */
    private void playSound(int resId){
        if(resId == 0) return;
        final MediaPlayer mp = MediaPlayer.create(mContext, resId);
        if(mp != null) {
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
        }
    }
}
