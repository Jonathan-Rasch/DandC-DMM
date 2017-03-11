package com.example.work.dmm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.ToggleButton;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.shinelw.library.ColorArcProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DCvoltageActivity extends AppCompatActivity {
    private LineChart loggingLineChart;
    private Button logVoltageButton;
    boolean isLogging = false;
    private ColorArcProgressBar voltageGauge;
    private float voltage=0;
    private ArrayList<Entry> entry_list = new ArrayList<Entry>();
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == MessageCode.PARSED_DATA_DC_VOLTAGE) {
                /*CODE FOR LOGGING AND PROCESSING DATA GOES HERE*/
                voltage = intent.getFloatExtra(MessageCode.VALUE,0f);
                voltageGauge.setCurrentValues(voltage);
                if (isLogging) {
                    Entry e =new Entry((float)entry_list.size(),voltage);
                    entry_list.add(e);
                    if(entry_list.size()>0){
                        genChart();
                    }
                }
            }else{//inside voltage activity but received wrong packet. send change mode packet
                Intent change_mode = new Intent(MessageCode.DMM_CHANGE_MODE_REQUEST);
                change_mode.putExtra(MessageCode.MODE,MessageCode.DC_VOLTAGE_MODE);
                sendBroadcast(change_mode);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dcvoltage);

        //Voltage Gauge
        voltageGauge = (ColorArcProgressBar) findViewById(R.id.voltageGauge);
        voltageGauge.setDiameter(200);
        voltageGauge.setCurrentValues(0);
        voltageGauge.setMaxValues(10);

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

        logVoltageButton = (Button) findViewById(R.id.logVoltageButton);
        logVoltageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

    private void genChart() {
        //Generate new dataset
        List<Entry> entries = entry_list;
        //limit the number of entries
        if (entries.size() > 100){
            entries.remove(0);
        }
        LineDataSet dataSet = new LineDataSet(entries, "Shitty Point Data"); // add entries to dataset
        dataSet.setColor(ColorTemplate.COLORFUL_COLORS[1]);
        dataSet.setValueTextColor(Color.BLACK);
        LineData lineData = new LineData(dataSet);

        loggingLineChart.setData(lineData);
        loggingLineChart.invalidate(); // refresh
        loggingLineChart.notifyDataSetChanged();//Causes redraw when we add data. I imagine we'll initiate
    }

}
