package com.example.work.dmm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VoltageOscilloscopeActivity extends AppCompatActivity {
    private SeekBar seekBar;
    private LineChart lineChart;
    private int updateCounterResetVal = 50;//reduces number of updates;
    private int updateCounter = 0;//reduces number of updates;
    private float maxVoltage = 10f;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        private int previousRange = -1;
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction= intent.getAction();
            if(intentAction != MessageCode.PARSED_DATA_DC_VOLTAGE){
                return;
            }
            int range = intent.getIntExtra(MessageCode.RANGE,-1);
            float value = intent.getFloatExtra(MessageCode.VALUE,0);
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
            entry_list.add(new Entry((float)t,value));
            //limit the number of entries TODO
            if (entry_list.size() > 150){
                entry_list.remove(0);
            }
            updateCounter -= 1;
            if(updateCounter <= 0){
                updateCounter = updateCounterResetVal;
                genChart();
            }
        }
    };

    /*DEBUG*/
    final Handler h = new Handler();
    final int delay = 10; //milliseconds
    private double t = 0f;//used to generate sine wave
    /*DEBUG END*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voltage_oscilloscope);
        getSupportActionBar().hide();
        //registering the broadcast receiver
        IntentFilter filter = new IntentFilter(MessageCode.PARSED_DATA_DC_VOLTAGE);
        registerReceiver(broadcastReceiver,filter);
        //obtaining views
        seekBar = (SeekBar) findViewById(R.id.seekBar1);
        lineChart = (LineChart) findViewById(R.id.oscilloscopeChart);
        lineChart.setAutoScaleMinMaxEnabled(false);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                genChart();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        /*DEBUG*/
        h.postDelayed(new Runnable(){
            public void run(){
                float voltage = 0;
                t += ((float)delay)/1000;
                //values so that they are sometimes out of range
                double sineArg= (t*2*Math.PI);
                voltage = (float)Math.sin(sineArg)*5;
                Log.e("blah","voltage="+voltage+" Range="+1);
                Intent I = new Intent(MessageCode.PARSED_DATA_DC_VOLTAGE);
                I.putExtra(MessageCode.VALUE,voltage);
                I.putExtra(MessageCode.RANGE,0);
                sendBroadcast(I);
                h.postDelayed(this, delay);
            }
        }, delay);
        /*DEBUG END*/
    }

    private ArrayList<Entry> entry_list = new ArrayList<>();
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
        indicatorLineEntries.add(new Entry(entries.get(0).getX(),maxVoltage*multiplier));
        indicatorLineEntries.add(new Entry(entries.get(entries.size()-1).getX(),maxVoltage*multiplier));
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


}
