package com.example.work.dmm.activities.measurementActivities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.work.dmm.R;
import com.example.work.dmm.utilityClasses.MessageCode;

public class SignalGeneratorActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    //views
    private Spinner waveformSelector;
    private Spinner periodUnit;
    private Spinner freqencyUnit;
    private Spinner AmplitudeUnit;

    //values
    int frequency = 1000;//Hz
    float period = 0.010f;//s
    float amplitude = 3.2f;//V

    //broadcast receiver
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            /*only purpose of the receiver is to switch back to sig gen mode if a packet with the
            * wrong mode or settings arrives*/
            if(action == MessageCode.SIGGEN_ACK){
                //TODO check settings transmitted with packet and send correction packet if needed
            }else{
                // TODO: 14/04/2017 generate sigGen package with all data such as freq etc since the incoming package had wrong action
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signal_generator);
        getSupportActionBar().setTitle("Signal Generator mode");
        //getting views:
        waveformSelector = (Spinner) findViewById(R.id.spinner_SigGen);
        periodUnit = (Spinner) findViewById(R.id.spinner_PeriodUnit_SigGen);
        freqencyUnit= (Spinner) findViewById(R.id.spinner_freqUnit_SigGen);
        AmplitudeUnit= (Spinner) findViewById(R.id.spinner_AmplUnit_SigGen);
        //setting spinner items

        ArrayAdapter<CharSequence> waveformAdapter = ArrayAdapter.createFromResource(this,
                R.array.waveforms_array,R.layout.support_simple_spinner_dropdown_item);
        waveformAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        waveformSelector.setAdapter(waveformAdapter);

        ArrayAdapter<CharSequence> periodAdapter = ArrayAdapter.createFromResource(this,
                R.array.periodUnits_array,R.layout.support_simple_spinner_dropdown_item);
        periodAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        periodUnit.setAdapter(periodAdapter);

        ArrayAdapter<CharSequence> freqAdapter = ArrayAdapter.createFromResource(this,
                R.array.freqUnits_array,R.layout.support_simple_spinner_dropdown_item);
        periodAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        freqencyUnit.setAdapter(freqAdapter);

        ArrayAdapter<CharSequence> amplAdapter = ArrayAdapter.createFromResource(this,
                R.array.amplUnits_array,R.layout.support_simple_spinner_dropdown_item);
        amplAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        AmplitudeUnit.setAdapter(amplAdapter);

        //registering broadcastReceiver
        IntentFilter filter = new IntentFilter(MessageCode.PARSED_DATA_DC_VOLTAGE);
        filter.addAction(MessageCode.PARSED_DATA_DC_CURRENT);
        filter.addAction(MessageCode.PARSED_DATA_RESISTANCE);
        filter.addAction(MessageCode.PARSED_DATA_FREQ_RESP);
        filter.addAction(MessageCode.SIGGEN_ACK);
        registerReceiver(broadcastReceiver,filter);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //TODO : depending on unit selected, apply multiplier to value to get value in base units.
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //do nothing
    }

    // button click methods
    public void onSetPeriodClick(View v){
        // TODO: 14/04/2017 implement
    }

    public void onSetFreqClick(View v){
        // TODO: 14/04/2017 implement
    }

    public void onSetAmplClick(View v){
        //todo implement
    }

    public void onSendClick(View v){
        // TODO: 14/04/2017 implement
    }


}
