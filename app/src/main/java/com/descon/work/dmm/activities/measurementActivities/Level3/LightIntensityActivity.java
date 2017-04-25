package com.descon.work.dmm.activities.measurementActivities.Level3;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.descon.work.dmm.R;
import com.descon.work.dmm.displayAndVisualisationClasses.Speedometer;
import com.descon.work.dmm.utilityClasses.BaseApplication;
import com.descon.work.dmm.utilityClasses.MessageCode;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
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

public class LightIntensityActivity extends AppCompatActivity {

    private float value;
    private boolean isLogging;
    private static LineChart chart;
    private boolean currentValueLogged = false;
    private static ArrayList<Entry> entry_list = new ArrayList<>();
    private int xoffset = 0;
    private static final float MAXIMUM_INTENSITY_VALUE = 3;
    private Speedometer gauge;
    private Button exportDataBtn;
    private Button logIntensityBtn;
    private Button clearChartButton;
    private Switch autoLoggingSwitch;
    private static BaseApplication base;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == MessageCode.PARSED_LIGHT_INTENSITY) {
                value = intent.getFloatExtra(MessageCode.VALUE,0f);
                //convert input value to a percentage
                value = value/MAXIMUM_INTENSITY_VALUE;
                if (isLogging) {
                    Entry e =new Entry((float)xoffset,value);
                    entry_list.add(e);
                    if(entry_list.size()>0){
                        genChart();
                    }
                    xoffset++;
                }else{
                    currentValueLogged = false;
                }
            }else{//inside voltage activity but received wrong packet. send change mode packet
                Intent change_mode = new Intent(MessageCode.DMM_CHANGE_MODE_REQUEST);
                change_mode.putExtra(MessageCode.MODE,MessageCode.LIGHT_INTENSITY_MODE);
                sendBroadcast(change_mode);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_intensity);
        /*Registering all message types so that application can send switch mode packet if the
        * wrong packet type is received*/
        registerReceiver(receiver,base.intentFILTER);
        //obtaining views
        gauge = (Speedometer) findViewById(R.id.lightIntensity_gauge);
        chart = (LineChart) findViewById(R.id.lightIntensity_loggingLineChart);
        exportDataBtn = (Button) findViewById(R.id.lightIntensity_ExportData);
        logIntensityBtn = (Button) findViewById(R.id.lightIntensity_btn_LogIntensity);
        autoLoggingSwitch = (Switch) findViewById(R.id.lightIntensity_loggingToggle);
        clearChartButton = (Button) findViewById(R.id.lightIntensity_btn_ClearLog);
        getSupportActionBar().setTitle("Light mode");
        base = (BaseApplication)getApplicationContext();
        autoLoggingSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickToggleAutoLogging();
            }
        });
        //set gauge range
        gauge.setCurrentSpeed(0);
        gauge.setMax(100f);
        gauge.setMin(0f);
        gauge.setUnit("%");

    }

    private void genChart(){
        //Generate new dataset
        List<Entry> entries = entry_list;
        //limit the number of entries
        if (entries.size() > base.getMaxDataPointsToKeep()){
            entries.remove(0);
        }
        LineDataSet dataSet = new LineDataSet(entries, "Light Intensity"); // add entries to dataset
        dataSet.setColor(ColorTemplate.COLORFUL_COLORS[1]);
        dataSet.setValueTextColor(Color.BLACK);
        LineData lineData = new LineData(dataSet);
        chart.clear();
        Description desc = new Description();
        desc.setText("X: measurement number Y: Voltage");
        chart.setDescription(desc);
        chart.setData(lineData);
        chart.invalidate(); // refresh
        chart.notifyDataSetChanged();//Causes redraw when we add data. I imagine we'll initiate

    }

/*
BUTTON CLICK METHODS:
*/

    public void onClickLogIntensity(View view){
        if (!currentValueLogged) {
            Entry e =new Entry((float)xoffset,value);
            entry_list.add(e);
            if(entry_list.size()>0){
                genChart();
            }
            xoffset++;
            currentValueLogged = true;
        }else{
            base.ts("No new data packages");
        }
    }

    private void onClickToggleAutoLogging(){
        isLogging = !isLogging;
    }

    public void onClickExportData(View view){
        DialogFragment exportDialog = new LightIntensityActivity.exportDialogFragment();
        exportDialog.show(getSupportFragmentManager(),"exportFragmentLight");
    }

    public void onClickClearLog(View view){
        entry_list = new ArrayList<>();
        chart.clear();
        chart.invalidate();
    }

/*

*/

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
        File file   =   new File(dir, "LightLog_"+currentDateTimeString+".txt");
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
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "DMM Light data");
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
        String imageName =   "LightGraphImage_"+currentDateTimeString;
        chart.saveToGallery(imageName,100);
        base.ts("Image saved to Gallery as: "+imageName);
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

    @Override
    protected void onDestroy() {
        //preventing receiver leaking
        this.unregisterReceiver(this.receiver);
        super.onDestroy();
    }
}
