package com.example.work.dmm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DCvoltageActivity extends AppCompatActivity {
    private BaseApplication base;
    private LineChart loggingLineChart;
    private Button logVoltageButton;
    private Speedometer gauge;
    boolean isLogging = false;
    private int xoffset = 0;
    private float voltage=0;
    private boolean currentVoltageLogged = false;

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
                }else{
                    currentVoltageLogged = false;
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
        //TODO needs to be verified and tested with actual bord
        switch(range){
            case 0:
                return voltage*1;//V
            case 1:
                return voltage*1000;//mV
            case 2:
                return voltage*1000;//mV
            case 3:
                return voltage*1000;//mV
        }
        return 0;
    }
    /*DEBUG*//*
    final Handler h = new Handler();
    final int delay = 100; //milliseconds
    Random r = new Random(System.currentTimeMillis());
    *//*DEBUG END*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dcvoltage);
        base = (BaseApplication)getApplicationContext();
        logVoltageButton = (Button) findViewById(R.id.btn_exportData);
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
        entry_list = new ArrayList<Entry>();

        //Data Logging Toggle
        Switch dataLogTog = (Switch) findViewById(R.id.loggingToggle);
        dataLogTog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleAutoLogging();
            }
        });


   /* *//*DEBUG*//*
        h.postDelayed(new Runnable(){
            public void run(){
                int range = r.nextInt(4);
                float voltage = 0;
                //values so that they are sometimes out of range
                switch (range){
                    case 0:
                        voltage = (r.nextFloat()*15+r.nextFloat()*-15);
                        break;
                    case 1:
                        voltage = (r.nextFloat()*1.5f+r.nextFloat()*-1.5f);
                        break;
                    case 2:
                        voltage = (r.nextFloat()*0.15f+r.nextFloat()*-0.15f);
                        break;
                    case 3:
                        voltage = (r.nextFloat()*0.015f+r.nextFloat()*-0.015f);
                        break;
                }
                Log.e("blah","voltage="+voltage+" Range="+range);
                Intent I = new Intent(MessageCode.PARSED_DATA_DC_VOLTAGE);
                I.putExtra(MessageCode.VALUE,voltage);
                I.putExtra(MessageCode.RANGE,range);
                sendBroadcast(I);
                h.postDelayed(this, delay);
            }
        }, delay);
    *//*DEBUG END*/

    }

    private void toggleAutoLogging(){
        isLogging = !isLogging;
    }

    public void logVoltage(View view){
        if (!currentVoltageLogged) {
            Entry e =new Entry((float)xoffset,voltage);
            entry_list.add(e);
            if(entry_list.size()>0){
                genChart();
            }
            xoffset++;
            currentVoltageLogged = true;
        }
    }

    public void onClickOscilloscopeMode(View view){
        Intent start_oscilloscope_activity_intent = new Intent(this,VoltageOscilloscopeActivity.class);
        startActivity(start_oscilloscope_activity_intent);
    }

    public void onClickExportData(View view){
        File root   = Environment.getExternalStorageDirectory();
        File dir    =   new File (root.getAbsolutePath() + "/DMM_Saved_Logs");
        if(!dir.exists()){
            dir.mkdirs();
        }
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        File file   =   new File(dir, "VoltageLog_"+currentDateTimeString+".txt");
        StringBuilder dataString = new StringBuilder("");
        ArrayList<Entry> copyList = (ArrayList<Entry>) entry_list.clone();
        for(int i=0;i<copyList.size();i++){
            String dataPoint = "x:"+copyList.get(i).getX()+" y:"+copyList.get(i).getY()+"\n";
            dataString.append(dataPoint);
        }
        String data = dataString.toString();
        try {
            if (data.length()>0) {
                FileOutputStream out = new FileOutputStream(file);
                out.write(data.getBytes());
                out.close();
                // try to send file via email
                Uri fileLocation  =   Uri.fromFile(file);
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "DMM Voltage data");
                sendIntent.putExtra(Intent.EXTRA_STREAM, fileLocation);
                sendIntent.setType("text/html");
                startActivity(sendIntent);
            }
        } catch (Exception e) {
            base.ts("ERROR encountered when trying to export data.");
            e.printStackTrace();
        }
    }

    public void onClickExportImage(View view){
        if(entry_list.size()<= 0){
            base.t("Graph empty, aborting image export");
            return;
        }
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        String imageName =   "VoltageGraphImage_"+currentDateTimeString;
        loggingLineChart.saveToGallery(imageName,100);
        base.ts("Image saved to Gallery as: "+imageName);
    }

    public void onClickClearLog(View view){
        entry_list = new ArrayList<>();
        loggingLineChart.clear();
        loggingLineChart.invalidate();
    }


    private void genChart() {
        //Generate new dataset
        List<Entry> entries = entry_list;
        //limit the number of entries
        if (entries.size() > base.getMaxDataPointsToKeep()){
            entries.remove(0);
        }
        LineDataSet dataSet = new LineDataSet(entries, "Voltage"); // add entries to dataset
        dataSet.setColor(ColorTemplate.COLORFUL_COLORS[1]);
        dataSet.setValueTextColor(Color.BLACK);
        LineData lineData = new LineData(dataSet);
        loggingLineChart.clear();
        loggingLineChart.setData(lineData);
        loggingLineChart.invalidate(); // refresh
        loggingLineChart.notifyDataSetChanged();//Causes redraw when we add data. I imagine we'll initiate
    }

    @Override
    protected void onDestroy() {
        //preventing receiver leaking
        this.unregisterReceiver(this.receiver);
        super.onDestroy();
    }
}
