package com.example.work.dmm.activities.measurementActivities.Level3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.work.dmm.R;
import com.example.work.dmm.utilityClasses.BaseApplication;
import com.example.work.dmm.utilityClasses.MessageCode;

public class SignalGeneratorActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "SigGen";
    private static final int MINIMUM_FREQUENCY = 100;
    private static final int MAXIMUM_FREQUENCY = 3000000;
    private static final float MINIMUM_AMPLITUDE = 0.001f;
    private static final float MAXIMUM_AMPLITUDE = 10f;
    BaseApplication base =(BaseApplication) getApplication();
    //views
    private Spinner waveformSelector;
    private Spinner periodUnit;
    private Spinner freqencyUnit;
    private Spinner AmplitudeUnit;

    private EditText edittext_amplitude;
    private EditText editText_frequency;
    private EditText editText_period;

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
        waveformSelector = (Spinner) findViewById(R.id.spinner_waveform_SigGen);
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

        //setting this as item selected listener for spinners
        waveformSelector.setOnItemSelectedListener(this);
        periodUnit.setOnItemSelectedListener(this);
        freqencyUnit.setOnItemSelectedListener(this);
        AmplitudeUnit.setOnItemSelectedListener(this);

        //registering broadcastReceiver
        IntentFilter filter = new IntentFilter(MessageCode.PARSED_DATA_DC_VOLTAGE);
        filter.addAction(MessageCode.PARSED_DATA_DC_CURRENT);
        filter.addAction(MessageCode.PARSED_DATA_RESISTANCE);
        filter.addAction(MessageCode.PARSED_DATA_FREQ_RESP);
        filter.addAction(MessageCode.SIGGEN_ACK);
        registerReceiver(broadcastReceiver,filter);

        //getting edit text views
        edittext_amplitude =(EditText) findViewById(R.id.editText_Amplitude_SigGen);
        editText_frequency = (EditText) findViewById(R.id.editText_freq_SigGen);
        editText_period = (EditText) findViewById(R.id.editText_period_SigGen);

    }

    //packet fields
    private String sigType = "";
    private float sigAmplitude = 1;
    private int frequency = 100000;
    private void resetFields(){
        if(frequency < 1000){
            editText_frequency.setText(String.valueOf(frequency));
            itemSelectionProgramatical = true;
            freqencyUnit.setSelection(0);
        }else if(frequency >= 1000 && frequency < 1000000){
            float newVal =frequency/1000;
            editText_frequency.setText(String.valueOf(newVal));
            itemSelectionProgramatical = true;
            freqencyUnit.setSelection(1);
        }else if(frequency >= 1000000){
            float newVal =frequency/1000000;
            editText_frequency.setText(String.valueOf(newVal));
            itemSelectionProgramatical = true;
            freqencyUnit.setSelection(2);
        }
        double period = 1/frequency;
        if (period > 1){
            itemSelectionProgramatical = true;
            editText_period.setText(String.valueOf(period));
            periodUnit.setSelection(0);//S
        }if(period < 1 && period >= 0.001){
            itemSelectionProgramatical = true;
            editText_period.setText(String.valueOf(period*1000));
            periodUnit.setSelection(1);//mS
        }if(period < 0.001){
            itemSelectionProgramatical = true;
            editText_period.setText(String.valueOf(period*1000000));
            periodUnit.setSelection(2);//uS
        }
    }

    //multipliers
    private float amplitudeMultiplier=1;
    private float frequencyMultiplier=1;
    private float periodMultiplier=1;
    private boolean itemSelectionProgramatical = false;
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(itemSelectionProgramatical){
            itemSelectionProgramatical = false;
            return;
        }
        int viewID = parent.getId();
        if(viewID == R.id.spinner_waveform_SigGen){
            switch(position){
                case 0://Sinusoidal wave
                    sigType = "Sinusoidal";
                    break;
                case 1://Square wave
                    sigType = "Square";
                    break;
                case 2://Triangle wave
                    sigType = "Triangle";
                    break;
                case 3://Noise
                    sigType = "Noise";
                    break;
            }
        }else if (viewID == R.id.spinner_PeriodUnit_SigGen){
            switch(position){
                case 0://S
                    periodMultiplier = 1;
                    break;
                case 1://mS
                    periodMultiplier = 0.001f;
                    break;
                case 2://uS
                    periodMultiplier = 0.000001f;
                    break;
            }
        }else if(viewID == R.id.spinner_freqUnit_SigGen){
            switch(position){
                case 0://Hz
                    frequencyMultiplier = 1;
                    break;
                case 1://KHz
                    frequencyMultiplier = 1000;
                    break;
                case 2://MHz
                    frequencyMultiplier = 1000000;
                    break;
            }
        }else if(viewID == R.id.spinner_AmplUnit_SigGen){
            switch(position){
                case 0://V
                    amplitudeMultiplier = 1;
                    break;
                case 1://mV
                    amplitudeMultiplier = 0.001f;
                    break;
                case 2://uV
                    amplitudeMultiplier = 0.000001f;
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //do nothing
    }

    // button click methods
    public void onSetPeriodClick(View v){
        //first obtain the text from the edit text and confirm it is valid
    }

    public void onSetFreqClick(View v){
        // TODO: 14/04/2017 implement
    }

    public void onSetAmplClick(View v){
        resetFields();
    }


    public void onSendClick(View v){

        Intent sigGenMode = new Intent(MessageCode.DMM_CHANGE_MODE_REQUEST);
        sigGenMode.putExtra(MessageCode.MODE,MessageCode.SIG_GEN_MODE);
        sigGenMode.putExtra(MessageCode.SIGGEN_FREQ,frequency);
        sigGenMode.putExtra(MessageCode.SIGGEN_AMPL,sigAmplitude);
        sigGenMode.putExtra(MessageCode.SIGGEN_SIGTYPE,sigType);
        this.sendBroadcast(sigGenMode);
    }


}
