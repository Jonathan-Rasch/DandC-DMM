package com.descon.work.dmm.activities.measurementActivities.Level2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.descon.work.dmm.utilityClasses.BaseApplication;
import com.descon.work.dmm.utilityClasses.MessageCode;
import com.descon.work.dmm.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class VoltageOscilloscopeActivity extends AppCompatActivity {
    private SeekBar seekBar;
    private LineChart lineChart;
    private float maxVoltage = 10f;
    private float signalFreq= Float.NaN;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        private int previousRange = -1;
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction= intent.getAction();
            if(intentAction != MessageCode.PARSED_DATA_DC_VOLTAGE){
                //packet has wrong mode, tell DMM to switch to voltage mode
                Intent change_mode = new Intent(MessageCode.DMM_CHANGE_MODE_REQUEST);
                change_mode.putExtra(MessageCode.MODE,MessageCode.DC_VOLTAGE_MODE);
                sendBroadcast(change_mode);
                return;
            }
            int range = intent.getIntExtra(MessageCode.RANGE,-1);
            float value = intent.getFloatExtra(MessageCode.VALUE,0);
            int freqInHz = intent.getIntExtra(MessageCode.EXTRA,-1);
            String freqUnit= "";
            //parsing frequency
            if(freqInHz <= 1000){
                signalFreq = ((float)freqInHz);//Hz
                freqUnit = "Hz";
            }else if (freqInHz > 1E3 && freqInHz <= 1E6){
                signalFreq = (float) (((float)freqInHz)/1E3);//kHz
                freqUnit = "kHz";
            }else if (freqInHz > 1E6 && freqInHz <= 1E9){
                signalFreq = (float) (((float)freqInHz)/1E6);//MHz
                freqUnit = "MHz";
            }else{
                signalFreq = Float.NaN;
            }
            String freqString = String.format(" Frequency:%.2f%s",signalFreq,freqUnit);
            //parsing the range
            if(previousRange != range){
                //reset the chart
                clearChart();
                //adjust ranges
                switch (range){
                    case 0:
                        maxVoltage = 10f;
                        lineChart.setVisibleYRange(-10f,10f, YAxis.AxisDependency.LEFT);
                        lineChart.setVisibleYRange(-10f,10f, YAxis.AxisDependency.RIGHT);
                        break;
                    case 1:
                        maxVoltage = 1f;
                        lineChart.setVisibleYRange(-1f,1f, YAxis.AxisDependency.LEFT);
                        lineChart.setVisibleYRange(-1f,1f, YAxis.AxisDependency.RIGHT);
                        break;
                    case 2:
                        maxVoltage = 0.1f;
                        lineChart.setVisibleYRange(-0.1f,0.1f, YAxis.AxisDependency.LEFT);
                        lineChart.setVisibleYRange(-0.1f,0.1f, YAxis.AxisDependency.RIGHT);
                        break;
                    case 3:
                        maxVoltage = 0.01f;
                        lineChart.setVisibleYRange(-0.01f,0.01f, YAxis.AxisDependency.LEFT);
                        lineChart.setVisibleYRange(-0.01f,0.01f, YAxis.AxisDependency.RIGHT);
                        break;
                }
                previousRange = range;
            }

                //create new entry list
                float pkpk = Math.abs(maximumValue)+Math.abs(minimumValue);
                peakToPeakText.setText("Voltage(pk-pk):"+pkpk+"V "+freqString);
                entry_list = new ArrayList<>();
                for(int i =0;i<frame.size();i++){
                    Entry newEntry = new Entry((float)i,frame.get(i));
                    entry_list.add(newEntry);
                }
                genChart();

        }
    };

    private BaseApplication base;
    TextView peakToPeakText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voltage_oscilloscope);
        getSupportActionBar().hide();
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        //registering the broadcast receiver
        base = (BaseApplication)getApplicationContext();
        registerReceiver(broadcastReceiver,base.intentFILTER);
        //obtaining views
        peakToPeakText= (TextView) findViewById(R.id.oscilloscope_pkpkVoltage_textview);
        seekBar = (SeekBar) findViewById(R.id.seekBar1);
        lineChart = (LineChart) findViewById(R.id.oscilloscopeChart);
        lineChart.setAutoScaleMinMaxEnabled(false);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (entry_list.size()>0) {
                    genChart();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    
    private float maximumValue = 0;
    private float minimumValue = 0;
    private ArrayList<Float> frame = new ArrayList<>();


    private ArrayList<Entry> entry_list = new ArrayList<>();
    private float level = 0;
    private void genChart() {
        //Generate new dataset
        List<Entry> entries = entry_list;
        List<Entry> indicatorLineEntries = new ArrayList<>();
        List<Entry> maxMinEntries = new ArrayList<>();
        maxMinEntries.add(new Entry(entries.get(0).getX(),maxVoltage));
        maxMinEntries.add(new Entry(entries.get(0).getX(),-maxVoltage));
        LineDataSet maxMinEntrySet = new LineDataSet(maxMinEntries,"");
        maxMinEntrySet.setColor(Color.TRANSPARENT);
        float seekBarProgressPercentage =  ((float)seekBar.getProgress()/(float)seekBar.getMax());
        float multiplier = (float)(2*seekBarProgressPercentage-1);// function that is -1 at 0% and 1 at 100%
        level = maxVoltage*multiplier;
        indicatorLineEntries.add(new Entry(entries.get(0).getX(),level));
        indicatorLineEntries.add(new Entry(entries.get(entries.size()-1).getX(),level));
        LineDataSet dataSet = new LineDataSet(entries, "Voltage"); // add entries to dataset
        dataSet.setDrawCircles(false);
        LineDataSet levelSet = new LineDataSet(indicatorLineEntries, "level"); // add entries to dataset
        levelSet.setDrawCircles(false);
        levelSet.setColor(ColorTemplate.COLORFUL_COLORS[2]);
        levelSet.setValueTextColor(Color.BLACK);
        dataSet.setColor(ColorTemplate.COLORFUL_COLORS[1]);
        dataSet.setValueTextColor(Color.BLACK);
        LineData lineData = new LineData(dataSet);
        lineData.addDataSet(levelSet);
        lineData.addDataSet(maxMinEntrySet);
        lineChart.clear();
        lineChart.setData(lineData);
        lineChart.notifyDataSetChanged();//Causes redraw when we add data. I imagine we'll initiate
        lineChart.invalidate(); // refresh
    }

    private void clearChart(){
        entry_list = new ArrayList<Entry>();
        lineChart.clear();
        lineChart.invalidate();
    }

    @Override
    public void onBackPressed() {
        this.unregisterReceiver(broadcastReceiver);
        super.onBackPressed();
        this.finish();
    }
}
