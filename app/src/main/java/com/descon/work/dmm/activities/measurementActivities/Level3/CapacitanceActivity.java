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
import android.widget.TextView;

import com.descon.work.dmm.R;
import com.descon.work.dmm.activities.measurementActivities.Level1.DCvoltageActivity;
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

public class CapacitanceActivity extends AppCompatActivity {

    private static BaseApplication base;
    private Button logCapacitance_btn;
    private Button exportData_btn;
    private TextView valueDisplay_txt;
    private static LineChart chart;
    private static ArrayList<Entry> entry_list;

    private int xoffset = 0;
    private float value =0;
    private boolean currentValueLogged = true;
    private String unit = "";
    private String displayString = "";
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == MessageCode.PARSED_CAPACITANCE) {
                value = intent.getFloatExtra(MessageCode.VALUE,0f);
                currentValueLogged = false;
                //scaling the value and setting the units
                if(value >= 0){
                    unit = "F";
                    value *= 1;
                }else if(value < 0 && value >= 1E-3){
                    unit = "mF";
                    value *= 1E3;
                }else if(value < 1E-3 && value >= 1E-6){
                    unit = "uF";
                    value *= 1E6;
                }else if(value < 1E-6 && value >= 1E-9){
                    unit = "nF";
                    value *= 1E9;
                }else if(value <1E-9){
                    unit = "pF";
                    value *= 1E12;
                }else{
                    unit = "";
                    value = Float.NaN;
                    currentValueLogged = true;//prevent logging of erroneous values
                }
                //displaying value
                displayString = String.format("Capacitance: %.2f%s",value,unit);
                valueDisplay_txt.setText(displayString);
            }else{//inside capacitance activity but received wrong packet. send change mode packet
                Intent change_mode = new Intent(MessageCode.DMM_CHANGE_MODE_REQUEST);
                change_mode.putExtra(MessageCode.MODE,MessageCode.CAPACITANCE_MODE);
                sendBroadcast(change_mode);
                valueDisplay_txt.setText("Incorrect DMM mode!");
                valueDisplay_txt.invalidate();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capacitance);
        getSupportActionBar().setTitle("Capacitance mode");
        entry_list = new ArrayList<>();
        //obtaining references
        base = (BaseApplication)getApplicationContext();
        logCapacitance_btn = (Button) findViewById(R.id.capacitance_logCapacitance_btn);
        exportData_btn = (Button) findViewById(R.id.capacitance_exportData_btn);
        valueDisplay_txt = (TextView) findViewById(R.id.capacitance_valueDisplay_txt);
        chart = (LineChart) findViewById(R.id.capacitance_chart);
        //registering receiver
        registerReceiver(receiver,base.intentFILTER);
    }

    //<editor-fold desc="Export related methods">
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
        File file   =   new File(dir, "CapacitanceLog_"+currentDateTimeString+".txt");
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
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "DMM Capacitance data");
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
        String imageName =   "CapacitanceGraphImage_"+currentDateTimeString;
        chart.saveToGallery(imageName,100);
        base.ts("Image saved to Gallery as: "+imageName);
    }
    //</editor-fold>

    //<editor-fold desc="Button onClick methods">
    public void onClickExportData(View view){
        DialogFragment exportDialog = new CapacitanceActivity.exportDialogFragment();
        exportDialog.show(getSupportFragmentManager(),"exportFragmentCapacitance");
    }

    public void onClickLogCapacitance(View view){
        if(!currentValueLogged){
            Entry e = new Entry(xoffset,value);
            xoffset++;
            entry_list.add(e);
            currentValueLogged=true;
            genChart();
        }else{
            base.ts("No new data to log");
        }
    }
    //</editor-fold>

    private void genChart() {
        //Generate new dataset
        List<Entry> entries = entry_list;
        //limit the number of entries
        if (entries.size() > base.getMaxDataPointsToKeep()){
            entries.remove(0);
        }
        LineDataSet dataSet = new LineDataSet(entries, "Capacitance"); // add entries to dataset
        dataSet.setColor(ColorTemplate.COLORFUL_COLORS[1]);
        dataSet.setValueTextColor(Color.BLACK);
        LineData lineData = new LineData(dataSet);
        chart.clear();
        Description desc = new Description();
        desc.setText("X: measurement number Y: capacitance(Farad)");
        chart.setDescription(desc);
        chart.setData(lineData);
        chart.invalidate(); // refresh
        chart.notifyDataSetChanged();//Causes redraw when we add data. I imagine we'll initiate
    }

    @Override
    protected void onDestroy() {
        //preventing receiver leaking
        this.unregisterReceiver(this.receiver);
        super.onDestroy();
    }
}
