package com.example.work.dmm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DCvoltageActivity extends AppCompatActivity {
    private LineChart loggingLineChart;
    private Button logVoltageButton;
    private Speedometer gauge;
    Random r = new Random(System.currentTimeMillis());
    boolean isLogging = false;
    private int xoffset = 0;
    private float voltage=0;
    private ArrayList<Entry> entry_list = new ArrayList<Entry>();

    private String[] unitsForRanges = {"V","mV","mV","mV"};
    private float[] maxValuesForRanges = {10,1000,100,10};
    private float[] minValuesForRanges = {-10,-1000,-100,-10};
    private int currentRange = -1;//-1 to ensure range update on first intent

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == MessageCode.PARSED_DATA_DC_VOLTAGE) {
                parseRange(intent.getIntExtra(MessageCode.RANGE,0));
                voltage = intent.getFloatExtra(MessageCode.VALUE,0f);
                voltage = valueToRangeAdjustment(currentRange,voltage);
                gauge.onSpeedChanged(voltage);
                if (isLogging) {
                    Entry e =new Entry((float)xoffset,voltage);
                    entry_list.add(e);
                    if(entry_list.size()>0){
                        genChart();
                    }
                    xoffset++;
                }
            }else{//inside voltage activity but received wrong packet. send change mode packet
                Intent change_mode = new Intent(MessageCode.DMM_CHANGE_MODE_REQUEST);
                change_mode.putExtra(MessageCode.MODE,MessageCode.DC_VOLTAGE_MODE);
                sendBroadcast(change_mode);
            }
        }
    };

    /*take the range value and set the correct units for display in gauge*/
    private void parseRange(int range){
        //check if the range has changed
        if(currentRange != range){
            //assigning new gauge values and units
            gauge.setMin(minValuesForRanges[range]);
            gauge.setMax(maxValuesForRanges[range]);
            gauge.setUnit(unitsForRanges[range]);
            currentRange = range;
        }
    }

    /*converts input voltage (e.g 0.3V) to the current range, so for example 0.3V->300mV*/
    private float valueToRangeAdjustment(int range, float voltage){
        switch(range){
            case 0:
                return voltage*1;//V
            case 1:
                return voltage*1;//V
            case 2:
                return voltage*100;//mV
            case 3:
                return voltage*100;//mV
        }
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dcvoltage);
        logVoltageButton = (Button) findViewById(R.id.logVoltageButton);
        gauge = (Speedometer) findViewById(R.id.gauge);
        gauge.setMax(10);
        gauge.setMin(-10);
        //Broadcast receiver and filter
        IntentFilter filter = new IntentFilter(MessageCode.PARSED_DATA_DC_VOLTAGE);
        filter.addAction(MessageCode.PARSED_DATA_DC_CURRENT);
        filter.addAction(MessageCode.PARSED_DATA_RESISTANCE);
        registerReceiver(receiver,filter);

        //Line Chart
        loggingLineChart = (LineChart) findViewById(R.id.loggingLineChart);
        loggingLineChart.setVisibility(View.INVISIBLE);

        //Data Logging Toggle
        Switch dataLogTog = (Switch) findViewById(R.id.loggingToggle);
        dataLogTog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleLogging();
            }
        });

    }

    private void toggleLogging(){
        //Hide appropriate UI elements within this method when not logging
        if(!isLogging) {
            entry_list = new ArrayList<Entry>();
            loggingLineChart.setVisibility(View.VISIBLE);
        } else{
            loggingLineChart.setVisibility(View.INVISIBLE);
        }
        isLogging = !isLogging;
    }

    /*DEBUG CODE !*/
    public void logVoltage(View view){
        int range = r.nextInt(4);
        float voltage = 0;
        //values so that they are sometimes out of range
        switch (range){
            case 0:
                voltage = (r.nextFloat()*11+r.nextFloat()*-11);
                break;
            case 1:
                voltage = (r.nextFloat()*1.1f+r.nextFloat()*-1.1f);
                break;
            case 2:
                voltage = (r.nextFloat()*0.11f+r.nextFloat()*-0.11f);
                break;
            case 3:
                voltage = (r.nextFloat()*0.011f+r.nextFloat()*-0.011f);
                break;
        }

        Intent I = new Intent(MessageCode.PARSED_DATA_DC_VOLTAGE);
        I.putExtra(MessageCode.VALUE,voltage);
        I.putExtra(MessageCode.RANGE,range);
        sendBroadcast(I);
    }
    /*/DEBUG CODE*/

    private void genChart() {
        //Generate new dataset
        List<Entry> entries = entry_list;
        //limit the number of entries
        if (entries.size() > 10){
            entries.remove(0);
        }
        LineDataSet dataSet = new LineDataSet(entries, "Shitty Point Data"); // add entries to dataset
        dataSet.setColor(ColorTemplate.COLORFUL_COLORS[1]);
        dataSet.setValueTextColor(Color.BLACK);
        LineData lineData = new LineData(dataSet);
        loggingLineChart.clear();
        loggingLineChart.setData(lineData);
        loggingLineChart.invalidate(); // refresh
        loggingLineChart.notifyDataSetChanged();//Causes redraw when we add data. I imagine we'll initiate
    }

}
