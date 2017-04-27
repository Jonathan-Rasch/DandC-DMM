package com.descon.work.dmm.activities.measurementActivities.Level2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.descon.work.dmm.R;
import com.descon.work.dmm.utilityClasses.BaseApplication;
import com.descon.work.dmm.utilityClasses.MessageCode;
import com.github.mikephil.charting.data.Entry;

public class ContinuityActivity extends AppCompatActivity {

    private BaseApplication base;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent change_mode = new Intent(MessageCode.DMM_CHANGE_MODE_REQUEST);
            change_mode.putExtra(MessageCode.MODE,MessageCode.CONTINUITY_MODE);
            sendBroadcast(change_mode);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continuity);
        getSupportActionBar().setTitle("Continuity mode");
        base = (BaseApplication)getApplicationContext();
        registerReceiver(receiver,base.intentFILTER);
        //sending mode change request
        Intent change_mode = new Intent(MessageCode.DMM_CHANGE_MODE_REQUEST);
        change_mode.putExtra(MessageCode.MODE,MessageCode.CONTINUITY_MODE);
        sendBroadcast(change_mode);
    }

    @Override
    public void onBackPressed() {
        unregisterReceiver(receiver);
        super.onBackPressed();
        finish();
    }
}
