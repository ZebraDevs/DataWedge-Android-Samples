package com.zebra.dwmultiactivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SecondActivity extends AppCompatActivity {

    IntentFilter filter = new IntentFilter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        Button btnSwitchActivity = findViewById(R.id.btnGoToFirstActivity);
        btnSwitchActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent switchActivity = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(switchActivity);
            }
        });

        // Register to receive data broadcast from DataWedge intent output
        // The filtered action must match the "Intent action" specified in the Intent output configuration
        // of the DataWedge profile.
        filter.addAction(DataWedgeInterface.ACTION_RESULT_DATAWEDGE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(DataWedgeInterface.ACTIVITY_INTENT_FILTER_ACTION);
        registerReceiver(myBroadcastReceiver, filter);
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        registerReceiver(myBroadcastReceiver, filter);

        DataWedgeInterface.sendDataWedgeIntentWithExtra(getApplicationContext(),
                DataWedgeInterface.ACTION_DATAWEDGE, DataWedgeInterface. EXTRA_GET_ACTIVE_PROFILE,
                DataWedgeInterface.EXTRA_EMPTY);

    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        unregisterReceiver(myBroadcastReceiver);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DataWedgeInterface.MessageEvent event) {
        TextView txtActiveProfile = findViewById(R.id.txtActiveProfile);
        txtActiveProfile.setText(event.activeProfile);
    };

    // Receive scanned data
    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle b = intent.getExtras();

            //  This is useful for debugging to verify the format of received intents from DataWedge
            //for (String key : b.keySet())
            //{
            //    Log.v("MultiActivity", key);
            //}

            if (action.equals(DataWedgeInterface.ACTION_RESULT_DATAWEDGE))
            {
                if (intent.hasExtra(DataWedgeInterface.EXTRA_RESULT_GET_ACTIVE_PROFILE))
                {
                    String activeProfile = intent.getStringExtra(DataWedgeInterface.EXTRA_RESULT_GET_ACTIVE_PROFILE);
                    EventBus.getDefault().post(new DataWedgeInterface.MessageEvent(activeProfile));
                }
            }

            if (action.equals(DataWedgeInterface.ACTIVITY_INTENT_FILTER_ACTION)) {
                //  Received a barcode scan
                try {
                    displayScanResult(intent, "via Broadcast");
                } catch (Exception e) {
                    //  Catch if the UI does not exist when we receive the broadcast
                }
            }

        }
    };

    // Display scanned data
    private void displayScanResult(Intent initiatingIntent, String howDataReceived)
    {
        String decodedData = initiatingIntent.getStringExtra(DataWedgeInterface.DATAWEDGE_INTENT_KEY_DATA);
        String decodedDecoder = initiatingIntent.getStringExtra(DataWedgeInterface.DATAWEDGE_INTENT_KEY_DECODER);

        final TextView lblScanData = (TextView) findViewById(R.id.lblScanData);
        final TextView lblScanDecoder = (TextView) findViewById(R.id.lblScanDecoder);

        lblScanData.setText(decodedData);
        lblScanDecoder.setText(decodedDecoder);
    }
}
