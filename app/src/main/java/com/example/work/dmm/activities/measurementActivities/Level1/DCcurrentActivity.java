package com.example.work.dmm.activities.measurementActivities.Level1;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.example.work.dmm.activities.measurementActivities.Level2.CurrentOscilloscopeActivity;
import com.example.work.dmm.activities.measurementActivities.Level2.VoltageOscilloscopeActivity;
import com.example.work.dmm.utilityClasses.BaseApplication;
import com.example.work.dmm.utilityClasses.MessageCode;
import com.example.work.dmm.R;
import com.example.work.dmm.displayAndVisualisationClasses.Speedometer;
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

public class DCcurrentActivity extends AppCompatActivity {
    private static BaseApplication base;
    private static LineChart loggingLineChart;
    private Button logCurrentButton;
    private Speedometer gauge;
    boolean isLogging = false;
    private int xoffset = 0;
    private float current =0;
    private boolean currentCurrentLogged = false;

    private static ArrayList<Entry> entry_list = new ArrayList<Entry>();

    private String[] unitsForRanges = {"A","mA","mA","mA"};
    private float[] maxValuesForRanges = {1,100,10,1};
    private float[] minValuesForRanges = {-1,-100,-10,-1};
    private int currentRange = -1;//-1 to ensure range update on first intent

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == MessageCode.PARSED_DATA_DC_CURRENT) {
                parseRange(intent.getIntExtra(MessageCode.RANGE,0));
                current = intent.getFloatExtra(MessageCode.VALUE,0f);
                float adjustedCurrent = valueToRangeAdjustment(currentRange, current);
                rangeBoundaryProximity(adjustedCurrent,minValuesForRanges[currentRange],maxValuesForRanges[currentRange]);
                gauge.onSpeedChanged(adjustedCurrent);
                if (isLogging) {
                    Entry e =new Entry((float)xoffset, current);
                    entry_list.add(e);
                    if(entry_list.size()>0){
                        genChart();
                    }
                    xoffset++;
                }else{
                    currentCurrentLogged = false;
                }
            }else{//inside current activity but received wrong packet. send change mode packet
                Intent change_mode = new Intent(MessageCode.DMM_CHANGE_MODE_REQUEST);
                change_mode.putExtra(MessageCode.MODE,MessageCode.DC_CURRENT_MODE);
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

    private void rangeBoundaryProximity(float value,float minVal,float maxVal){
        if(value > maxVal || value < minVal){
            base.vibratePulse(500);
        }else if(value >= maxVal*0.9f || value <= minVal*1.1f){
            base.vibratePulse(250);
        }
    }

    /*converts input current (e.g 0.3A) to the current range, so for example 0.3A->300mA*/
    private float valueToRangeAdjustment(int range, float current){
        //TODO needs to be verified and tested with actual bord
        switch(range){
            case 0:
                return current*1;//A
            case 1:
                return current*1000;//mA
            case 2:
                return current*1000;//mA
            case 3:
                return current*1000;//mA
        }
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dccurrent);
        getSupportActionBar().setTitle("Current mode");
        base = (BaseApplication)getApplicationContext();
        logCurrentButton = (Button) findViewById(R.id.logCurrent_btn);
        gauge = (Speedometer) findViewById(R.id.CurrentGauge);
        gauge.setMax(1000);
        gauge.setMin(-1000);
        gauge.setCurrentSpeed(0);
        gauge.setUnit("A");
        /*Registering all message types so that application can send switch mode packet if the
        * wrong packet type is received*/
        registerReceiver(receiver,base.FILTER);

        //Line Chart
        loggingLineChart = (LineChart) findViewById(R.id.loggingLineChart_Current);
        entry_list = new ArrayList<Entry>();

        //Data Logging Toggle
        Switch dataLogTog = (Switch) findViewById(R.id.autoLogCurrent_swt);
        dataLogTog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleAutoLogging();
            }
        });

    }

    private void toggleAutoLogging(){
        isLogging = !isLogging;
    }

    public void logCurrent(View view){
        if (!currentCurrentLogged) {
            Entry e =new Entry((float)xoffset, current);
            entry_list.add(e);
            if(entry_list.size()>0){
                genChart();
            }
            xoffset++;
            currentCurrentLogged = true;
        }else{
            base.ts("No new data packages");
        }
    }

    public void onClickOscilloscopeMode(View view){
        Intent start_oscilloscope_activity_intent = new Intent(this,CurrentOscilloscopeActivity.class);
        startActivity(start_oscilloscope_activity_intent);
    }

    public void onClickExportData(View view){
        DialogFragment exportDialog = new DCcurrentActivity.exportDialogFragment();
        exportDialog.show(getSupportFragmentManager(),"exportFragmentDcCurrent");
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
        LineDataSet dataSet = new LineDataSet(entries, "Current"); // add entries to dataset
        dataSet.setColor(ColorTemplate.COLORFUL_COLORS[1]);
        dataSet.setValueTextColor(Color.BLACK);
        LineData lineData = new LineData(dataSet);
        loggingLineChart.clear();
        loggingLineChart.setData(lineData);
        loggingLineChart.invalidate(); // refresh
        loggingLineChart.notifyDataSetChanged();//Causes redraw when we add data. I imagine we'll initiate
    }

    public static class exportDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String[] items = {"Export chart data points and send them via email",
                    "Save chart image to the phone image gallery",
                    "Cancel"};
            builder.setTitle("Chart export options")
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case 0:
                                    exportData();
                                    break;
                                case 1:
                                    exportImage();
                                    break;
                                case 2:
                                    base.ts("Export canceled.");
                                    return;
                            }
                        }
                    });
            return builder.create();
        }
    }

    private static void  exportData(){
        if(entry_list.size()<=0){
            base.ts("No chart data to export, Aborting.");
            return;
        }
        File root   = Environment.getExternalStorageDirectory();
        File dir    =   new File (root.getAbsolutePath() + "/DMM_Saved_Logs");
        if(!dir.exists()){
            dir.mkdirs();
        }
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        File file   =   new File(dir, "CurrentLog_"+currentDateTimeString+".txt");
        StringBuilder dataString = new StringBuilder("");
        ArrayList<Entry> copyList = new ArrayList<>(entry_list);
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
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "DMM Current data");
                sendIntent.putExtra(Intent.EXTRA_STREAM, fileLocation);
                sendIntent.setType("text/html");
                sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                base.startActivity(sendIntent);
            }
        } catch (Exception e) {
            base.ts("ERROR encountered when trying to export data.");
            e.printStackTrace();
        }
    }

    private static void exportImage(){
        if(entry_list.size()<= 0){
            base.t("Graph empty, aborting image export");
            return;
        }
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        String imageName =   "CurrentGraphImage_"+currentDateTimeString;
        loggingLineChart.saveToGallery(imageName,100);
        base.ts("Image saved to Gallery as: "+imageName);
    }

    @Override
    protected void onDestroy() {
        //preventing receiver leaking
        this.unregisterReceiver(this.receiver);
        super.onDestroy();
    }
}
