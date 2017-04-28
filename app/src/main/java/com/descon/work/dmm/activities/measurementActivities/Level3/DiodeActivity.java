package com.descon.work.dmm.activities.measurementActivities.Level3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.descon.work.dmm.R;
import com.descon.work.dmm.displayAndVisualisationClasses.Speedometer;
import com.descon.work.dmm.utilityClasses.BaseApplication;
import com.descon.work.dmm.utilityClasses.MessageCode;

public class DiodeActivity extends AppCompatActivity {

    private BaseApplication base;
    private String requestedMode = MessageCode.DIODE_TYPE_DIODE;
    float dtmVoltage = 0;
    int zdtMode = 0;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == MessageCode.PARSED_DIODE_VOLTAGE){
                dtmVoltage = intent.getFloatExtra(MessageCode.VALUE,0f);
                if(dtmVoltage < 0){
                    dtmVoltage = 0;
                }else if(dtmVoltage > 12){
                    dtmVoltage = 12;
                }
                zdtMode = intent.getIntExtra(MessageCode.RANGE,0);
                if(zdtMode > -1){ // ZENER MODE
                    if(requestedMode == MessageCode.DIODE_TYPE_DIODE){
                        Intent change_mode = new Intent(MessageCode.DMM_CHANGE_MODE_REQUEST);
                        change_mode.putExtra(MessageCode.MODE,MessageCode.DIODE_MODE);
                        change_mode.putExtra(MessageCode.DIODE_TYPE, requestedMode);
                        sendBroadcast(change_mode);
                        return;
                    }
                    gauge.setCurrentSpeed(0);
                    gauge.setUnit("");
                    gauge.invalidate();
                    switch(zdtMode){
                        case 1:
                            modeText.setText("MODE: ZENER; DIODE: GOOD GERMANIUM DIODE");
                            break;
                        case 2:
                            modeText.setText("MODE: ZENER; DIODE: BAD DIODE");
                            break;
                        case 3:
                            modeText.setText("MODE: ZENER; DIODE: GOOD SILICONE DIODE");
                            break;
                        case 4:
                            modeText.setText("MODE: ZENER; DIODE: OVERLOAD! OPEN DIODE!");
                            break;
                        default:
                            modeText.setText("MODE: ZENER; DIODE: --");
                            break;
                    }
                }else{//DTM MODE
                    if(requestedMode == MessageCode.DIODE_TYPE_ZENER){
                        Intent change_mode = new Intent(MessageCode.DMM_CHANGE_MODE_REQUEST);
                        change_mode.putExtra(MessageCode.MODE,MessageCode.DIODE_MODE);
                        change_mode.putExtra(MessageCode.DIODE_TYPE, requestedMode);
                        sendBroadcast(change_mode);
                        return;
                    }
                    gauge.setUnit("V");
                    modeText.setText("MODE: DTM (SEE GAUGE)");
                    gauge.setCurrentSpeed(dtmVoltage);
                    gauge.invalidate();
                }
            }else{
                Intent change_mode = new Intent(MessageCode.DMM_CHANGE_MODE_REQUEST);
                change_mode.putExtra(MessageCode.MODE,MessageCode.DIODE_MODE);
                change_mode.putExtra(MessageCode.DIODE_TYPE, requestedMode);
                sendBroadcast(change_mode);
            }
        }
    };

    private Speedometer gauge;
    private TextView modeText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diode);
        //setting title
        getSupportActionBar().setTitle("Diode mode");
        //registering receiver
        base = (BaseApplication) getApplicationContext();
        registerReceiver(receiver,base.intentFILTER);
        //get views
        gauge = (Speedometer) findViewById(R.id.diode_gauge);
        gauge.setMax(12);
        gauge.setMin(0);
        gauge.setCurrentSpeed(0);
        gauge.setUnit("V");
        modeText = (TextView) findViewById(R.id.diode_text);
        modeText.setText("MODE: NONE");
        //sent intent
        Intent change_mode = new Intent(MessageCode.DMM_CHANGE_MODE_REQUEST);
        change_mode.putExtra(MessageCode.MODE,MessageCode.DIODE_MODE);
        sendBroadcast(change_mode);
    }

    public void onClickDTM (View view){
        requestedMode = MessageCode.DIODE_TYPE_DIODE;
        Intent change_mode = new Intent(MessageCode.DMM_CHANGE_MODE_REQUEST);
        change_mode.putExtra(MessageCode.MODE,MessageCode.DIODE_MODE);
        change_mode.putExtra(MessageCode.DIODE_TYPE,MessageCode.DIODE_TYPE_DIODE);
        sendBroadcast(change_mode);
    }

    public void onClickZDT (View view){
        requestedMode = MessageCode.DIODE_TYPE_ZENER;
        Intent change_mode = new Intent(MessageCode.DMM_CHANGE_MODE_REQUEST);
        change_mode.putExtra(MessageCode.MODE,MessageCode.DIODE_MODE);
        change_mode.putExtra(MessageCode.DIODE_TYPE,MessageCode.DIODE_TYPE_ZENER);
        sendBroadcast(change_mode);
    }

    @Override
    public void onBackPressed() {
        unregisterReceiver(receiver);
        super.onBackPressed();
        finish();
    }

}
