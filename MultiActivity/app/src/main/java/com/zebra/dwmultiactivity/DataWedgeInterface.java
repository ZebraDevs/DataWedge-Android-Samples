package com.zebra.dwmultiactivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class DataWedgeInterface {
    public static class MessageEvent {
        String activeProfile;

        public MessageEvent(String activeProfile) {
            this.activeProfile = activeProfile;
        }
    }

    public static final String ACTION_DATAWEDGE = "com.symbol.datawedge.api.ACTION";
    public static final String ACTION_RESULT_DATAWEDGE = "com.symbol.datawedge.api.RESULT_ACTION";
    public static final String EXTRA_RESULT_GET_ACTIVE_PROFILE = "com.symbol.datawedge.api.RESULT_GET_ACTIVE_PROFILE";
    public static final String EXTRA_GET_ACTIVE_PROFILE = "com.symbol.datawedge.api.GET_ACTIVE_PROFILE";
    public static final String EXTRA_SET_CONFIG = "com.symbol.datawedge.api.SET_CONFIG";
    public static final String ACTIVITY_INTENT_FILTER_ACTION = "com.zebra.dwmultiactivity.ACTION";
    public static final String DATAWEDGE_INTENT_KEY_DATA = "com.symbol.datawedge.data_string";
    public static final String DATAWEDGE_INTENT_KEY_DECODER = "com.symbol.datawedge.label_type";
    public static final String EXTRA_EMPTY = "";

    public static void sendDataWedgeIntentWithExtra(Context context, String action, String extraKey, String extraValue)
    {
        Intent dwIntent = new Intent();
        dwIntent.setAction(action);
        dwIntent.putExtra(extraKey, extraValue);
        context.sendBroadcast(dwIntent);
    }

    public static void sendDataWedgeIntentWithExtra(Context context, String action, String extraKey,  Bundle extras)
    {
        Intent dwIntent = new Intent();
        dwIntent.setAction(action);
        dwIntent.putExtra(extraKey, extras);
        context.sendBroadcast(dwIntent);
    }
}
