package com.example.work.dmm.activities.measurementActivities.Level3;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.work.dmm.utilityClasses.BaseApplication;
import com.example.work.dmm.displayAndVisualisationClasses.Log10AxisValueFormatter;
import com.example.work.dmm.utilityClasses.MessageCode;
import com.example.work.dmm.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FrequencyResponseActivity extends AppCompatActivity {
    private static LineChart logchart;
    private static BaseApplication base;
    private static List<Entry> entry_list = new ArrayList<>();

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            /*freq response package uses the same format as all other <m:int;v:float;r:int>
                * m: stands for mode, which is 4 for freq resp packets
                * v: Gain for this specific frequency value
                * r: the frequency at which the value is taken (in Hz)*/
            if(intentAction == MessageCode.PARSED_DATA_FREQ_RESP){
                float gain = intent.getFloatExtra(MessageCode.VALUE,0f);
                int frequency = intent.getIntExtra(MessageCode.RANGE,0);//freq in Hz
                //creating Entry for log linear plot
                Entry e = new Entry((float)Math.log10(frequency),gain);
                entry_list.add(e);
                updateChart();
            }else{// send change mode packet since the received packet is wrong
                Intent change_mode = new Intent(MessageCode.DMM_CHANGE_MODE_REQUEST);
                change_mode.putExtra(MessageCode.MODE,MessageCode.FREQ_RESP_MODE);
                change_mode.putExtra(MessageCode.FREQ_RESP_START_FREQ,base.getFreqRespStartFreqHz());
                change_mode.putExtra(MessageCode.FREQ_RESP_END_FREQ,base.getFreqRespEndFreqHz());
                change_mode.putExtra(MessageCode.FREQ_RESP_STEPS,base.getNumberOfSteps());
                sendBroadcast(change_mode);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frequency_response);
        getSupportActionBar().setTitle("Frequency response mode");
        /*setting up the log linear chart*/
        logchart = (LineChart) findViewById(R.id.freqRespGraph);
        logchart.getXAxis().setValueFormatter(new Log10AxisValueFormatter());
        /*Registering all message types so that application can send switch mode packet if the
        * wrong packet type is received*/
        IntentFilter filter = new IntentFilter(MessageCode.PARSED_DATA_DC_VOLTAGE);
        filter.addAction(MessageCode.PARSED_DATA_DC_CURRENT);
        filter.addAction(MessageCode.PARSED_DATA_RESISTANCE);
        filter.addAction(MessageCode.PARSED_DATA_FREQ_RESP);
        filter.addAction(MessageCode.SIGGEN_ACK);
        registerReceiver(broadcastReceiver,filter);
        base = (BaseApplication) getApplication();
    }

    public void onClickExport (View view){
        DialogFragment exportDialog = new exportDialogFragment();
        exportDialog.show(getSupportFragmentManager(),"exportDialog");
    }

    private static void exportImage(){
        if(entry_list.size()<= 0){
            base.t("Graph empty, aborting image export");
            return;
        }
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        String imageName =   "FrequencyResponseImage_"+currentDateTimeString;
        logchart.saveToGallery(imageName,100);
        base.ts("Image saved to Gallery as: "+imageName);
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
        File file   =   new File(dir, "FreqRespLog_"+currentDateTimeString+".txt");
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
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "DMM Frequency response data");
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

    public static class exportDialogFragment extends DialogFragment{
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


    public void onClickBeginDataAquisition(View view){
        /*Sending message to DMM to tell it to start the freq response, the format is:
        * <m:int;start:int;end:int;steps:int>
        *   m: the mode to switch to (4 in this case)
        *   start: start frequency in Hz
        *   end: end frequency in Hz
        *   steps: number of steps to take to reach the end frequency */
        clearChart();
        Intent change_mode = new Intent(MessageCode.DMM_CHANGE_MODE_REQUEST);
        change_mode.putExtra(MessageCode.MODE,MessageCode.FREQ_RESP_MODE);
        change_mode.putExtra(MessageCode.FREQ_RESP_START_FREQ,base.getFreqRespStartFreqHz());
        change_mode.putExtra(MessageCode.FREQ_RESP_END_FREQ,base.getFreqRespEndFreqHz());
        change_mode.putExtra(MessageCode.FREQ_RESP_STEPS,base.getNumberOfSteps());
        sendBroadcast(change_mode);
    }

    private void updateChart(){
        if (entry_list.size() > 0) {
            LineDataSet dataSet = new LineDataSet(entry_list, "Gain"); // add entries to dataset
            dataSet.setColor(Color.BLUE);
            dataSet.setValueTextColor(Color.BLACK);
            LineData lineData = new LineData(dataSet);
            Description desc = new Description();
            desc.setText("X: Frequency Y: Gain");
            logchart.setDescription(desc);
            logchart.setData(lineData);
            logchart.invalidate(); // refresh
            logchart.notifyDataSetChanged();
        }
    }

    public void clearChart(){
        logchart.clear();
        entry_list = new ArrayList<>();
        logchart.invalidate();
    }

    @Override
    public void onBackPressed() {
        unregisterReceiver(broadcastReceiver);
        this.finish();
        super.onBackPressed();
    }
}
