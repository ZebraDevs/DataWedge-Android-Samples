package com.zebra.datawedgeexerciser;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by darry on 15/08/2016.
 */
public class ListeningService extends IntentService {

    public ListeningService() {super("ListeningService");}

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent.getAction().equals(getResources().getString(R.string.activity_intent_filter_action)))
        {
            //  Send a Broadcast intent to the MainActivity to notify it we have received a Scan
            Intent informMainActivity = new Intent();
            informMainActivity.setAction(getResources().getString(R.string.activity_action_from_service));
            informMainActivity.putExtras(intent.getExtras());
            sendBroadcast(informMainActivity);
        }
    }
}
