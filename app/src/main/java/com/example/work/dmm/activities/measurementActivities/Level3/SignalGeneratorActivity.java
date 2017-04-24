package com.example.work.dmm.activities.measurementActivities.Level3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.work.dmm.R;
import com.example.work.dmm.utilityClasses.BaseApplication;
import com.example.work.dmm.utilityClasses.MessageCode;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Random;

public class SignalGeneratorActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "SigGen";
    private static final int MINIMUM_FREQUENCY = 100;
    private static final int MAXIMUM_FREQUENCY = 3000000;
    private static final float MINIMUM_AMPLITUDE = 0.000001f;
    private static final float MAXIMUM_AMPLITUDE = 10f;
    BaseApplication base;
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
                float amplitude = intent.getFloatExtra(MessageCode.VALUE,-1);
                int tfrequency = intent.getIntExtra(MessageCode.RANGE,-1);
                if(amplitude==sigAmplitude && frequency == tfrequency){
                    return;//packet data correct
                }
            }
            //if program got to this point then some data in packet is not ok
            Intent sigGenMode = new Intent(MessageCode.DMM_CHANGE_MODE_REQUEST);
            sigGenMode.putExtra(MessageCode.MODE,MessageCode.SIG_GEN_MODE);
            sigGenMode.putExtra(MessageCode.SIGGEN_FREQ,frequency);
            sigGenMode.putExtra(MessageCode.SIGGEN_AMPL,sigAmplitude);
            sigGenMode.putExtra(MessageCode.SIGGEN_SIGTYPE,sigType);
            base.sendBroadcast(sigGenMode);
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
        registerReceiver(broadcastReceiver,base.FILTER);

        //getting edit text views
        chart = (LineChart) findViewById(R.id.LineChart_SigGen);
        edittext_amplitude =(EditText) findViewById(R.id.editText_Amplitude_SigGen);
        edittext_amplitude.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                edittext_amplitude.setBackground(new ColorDrawable(Color.YELLOW));
                allFiledsSet =false;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editText_frequency = (EditText) findViewById(R.id.editText_freq_SigGen);
        editText_frequency.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editText_frequency.setBackground(new ColorDrawable(Color.YELLOW));
                allFiledsSet =false;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editText_period = (EditText) findViewById(R.id.editText_period_SigGen);
        editText_period.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editText_period.setBackground(new ColorDrawable(Color.YELLOW));
                allFiledsSet =false;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //getting base application
        base =(BaseApplication) getApplication();

        //setting initial values
        resetFields();
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
        double period = ((double)1)/frequency;
        if (period >= 1){
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
        //resetting amplitude value
        if (sigAmplitude >= 1){
            itemSelectionProgramatical = true;
            edittext_amplitude.setText(String.valueOf(sigAmplitude));
            AmplitudeUnit.setSelection(0);//V
        }if(sigAmplitude < 1 && sigAmplitude >= 0.001){
            itemSelectionProgramatical = true;
            edittext_amplitude.setText(String.valueOf(sigAmplitude*1000));
            AmplitudeUnit.setSelection(1);//mV
        }if(sigAmplitude < 0.001){
            itemSelectionProgramatical = true;
            edittext_amplitude.setText(String.valueOf(sigAmplitude*1000000));
            AmplitudeUnit.setSelection(2);//uV
        }

        edittext_amplitude.setBackground(new ColorDrawable(Color.WHITE));
        editText_period.setBackground(new ColorDrawable(Color.WHITE));
        editText_frequency.setBackground(new ColorDrawable(Color.WHITE));
        allFiledsSet = true;
        updateChart();
    }

    //multipliers
    private float amplitudeMultiplier=1;
    private float frequencyMultiplier=1;
    private float periodMultiplier=1;
    private boolean itemSelectionProgramatical = false;
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int viewID = parent.getId();
        /*if(itemSelectionProgramatical){
            itemSelectionProgramatical = false;
            return;
        }*/
        if(viewID == R.id.spinner_waveform_SigGen){
            waveformSelector.setBackground(new ColorDrawable(Color.WHITE));
            switch(position){
                case 0://Sinusoidal wave
                    sigType = "sinusoidal";
                    break;
                case 1://Square wave
                    sigType = "square";
                    break;
                case 2://Triangle wave
                    sigType = "triangle";
                    break;
                case 3://Noise
                    sigType = "noise";
                    break;
            }
        }else if (viewID == R.id.spinner_PeriodUnit_SigGen){
            editText_period.setBackground(new ColorDrawable(Color.YELLOW));
            allFiledsSet = false;
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
            editText_frequency.setBackground(new ColorDrawable(Color.YELLOW));
            allFiledsSet = false;
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
            edittext_amplitude.setBackground(new ColorDrawable(Color.YELLOW));
            allFiledsSet = false;
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
        updateChart();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //do nothing
    }

    // button click methods
    public void onSetPeriodClick(View v){
        String text = editText_period.getText().toString();
        float newPeriod = 0;
        try {
            newPeriod = Float.parseFloat(text);
        } catch (NumberFormatException e) {
            Log.e(TAG,"Invalid Period value: "+text);
            base.ts("Invalid Period value");
            resetFields();
            return;
        }
        newPeriod *= periodMultiplier;
        float newFrequency = ((float)1)/newPeriod;
        if(newFrequency > MINIMUM_FREQUENCY && newFrequency < MAXIMUM_FREQUENCY){
            frequency = (int)newFrequency;
            editText_frequency.setBackground(new ColorDrawable(Color.WHITE));
            editText_period.setBackground(new ColorDrawable(Color.WHITE));
            base.ts("Period set!");
            resetFields();
        }else{
            Log.e(TAG,"Period out of allowed bounds");
            base.ts("Period out of allowed bounds");
            resetFields();
            return;
        }
    }

    public void onSetFreqClick(View v){
        String text = editText_frequency.getText().toString();
        float newFrequency = 0;
        try {
            newFrequency = Float.parseFloat(text);
        } catch (NumberFormatException e) {
            Log.e(TAG,"Invalid frequency value: "+text);
            base.ts("Invalid frequency value");
            resetFields();
            return;
        }
        newFrequency *= frequencyMultiplier;
        if(newFrequency > MINIMUM_FREQUENCY && newFrequency < MAXIMUM_FREQUENCY){
            frequency = (int)newFrequency;
            editText_frequency.setBackground(new ColorDrawable(Color.WHITE));
            base.ts("Frequency set!");
            resetFields();
        }else{
            Log.e(TAG,"Frequency out of allowed bounds: "+newFrequency);
            base.ts("Frequency out of allowed bounds");
            resetFields();
            return;
        }
    }

    public void onSetAmplClick(View v){
        String text = edittext_amplitude.getText().toString();
        float newAmplitude = 0;
        try {
            newAmplitude = Float.parseFloat(text);
        } catch (NumberFormatException e) {
            Log.e(TAG,"Invalid amplitude value: "+text);
            base.ts("Invalid amplitude value");
            resetFields();
            return;
        }
        newAmplitude *= amplitudeMultiplier;
        if(newAmplitude <= MAXIMUM_AMPLITUDE && newAmplitude >= MINIMUM_AMPLITUDE){
            sigAmplitude = newAmplitude;
            edittext_amplitude.setBackground(new ColorDrawable(Color.WHITE));
            base.ts("Amplitude set!");
            resetFields();
        }else{
            Log.e(TAG,"Amplitude out of allowed bounds: "+newAmplitude);
            base.ts("Amplitude out of allowed bounds");
            resetFields();
            return;
        }
    }

    private boolean allFiledsSet = true;
    public void onSendClick(View v){
        if(!allFiledsSet){
            base.ts("Some fields have changed values and have not been set. Aborting");
            return;
        }
        Intent sigGenMode = new Intent(MessageCode.DMM_CHANGE_MODE_REQUEST);
        sigGenMode.putExtra(MessageCode.MODE,MessageCode.SIG_GEN_MODE);
        sigGenMode.putExtra(MessageCode.SIGGEN_FREQ,frequency);
        sigGenMode.putExtra(MessageCode.SIGGEN_AMPL,sigAmplitude);
        sigGenMode.putExtra(MessageCode.SIGGEN_SIGTYPE,sigType);
        this.sendBroadcast(sigGenMode);
        edittext_amplitude.setBackground(new ColorDrawable(Color.GREEN));
        editText_period.setBackground(new ColorDrawable(Color.GREEN));
        editText_frequency.setBackground(new ColorDrawable(Color.GREEN));
        waveformSelector.setBackground(new ColorDrawable(Color.GREEN));
    }

    ArrayList<Entry> entries;
    LineChart chart;
    private void updateChart(){
        entries = new ArrayList<>();
        if(sigType.equals("sinusoidal")){
            for(float i=0;i<1;i+=0.01f){
                Entry e = new Entry(i,sigAmplitude*(float)Math.sin(i*2*Math.PI));
                entries.add(e);
            }
        }else if(sigType.equals("square")){
            for(float i=0;i<1;i+=0.01f){
                if(i<=0.5){
                    Entry e = new Entry(i,sigAmplitude);
                    entries.add(e);
                }else{
                    Entry e = new Entry(i,-sigAmplitude);
                    entries.add(e);
                }
            }
        }else if(sigType.equals("triangle")){
            entries.add(new Entry(0f,0f));
            entries.add(new Entry(0.25f,sigAmplitude));
            entries.add(new Entry(0.5f,0f));
            entries.add(new Entry(0.75f,-sigAmplitude));
            entries.add(new Entry(1f,0f));
        }else if(sigType.equals("noise")){
            Random r = new Random(System.currentTimeMillis());
            for(float i=0;i<1;i+=0.01f){
                int sign = r.nextInt(2);
                if(sign == 0){
                    Entry e = new Entry(i,-sigAmplitude*r.nextFloat());
                    entries.add(e);
                }else{
                    Entry e = new Entry(i,sigAmplitude*r.nextFloat());
                    entries.add(e);
                }
            }
        }
        if(entries.size()>0){
            LineDataSet dataSet = new LineDataSet(entries, "Voltage"); // add entries to dataset
            dataSet.setDrawCircles(false);
            dataSet.setColor(ColorTemplate.COLORFUL_COLORS[1]);
            dataSet.setValueTextColor(Color.BLACK);
            LineData lineData = new LineData(dataSet);
            chart.clear();
            chart.setData(lineData);
            chart.notifyDataSetChanged();//Causes redraw when we add data. I imagine we'll initiate
            chart.invalidate(); // refresh
        }
    }

    private void clearChart(){
        entries = new ArrayList<Entry>();
        chart.clear();
        chart.invalidate();
    }

    @Override
    public void onBackPressed() {
        this.unregisterReceiver(broadcastReceiver);
        this.finish();
        super.onBackPressed();
    }
}
