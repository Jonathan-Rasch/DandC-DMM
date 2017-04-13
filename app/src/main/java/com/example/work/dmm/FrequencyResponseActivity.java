package com.example.work.dmm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FrequencyResponseActivity extends AppCompatActivity {
    private LineChart logchart;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if(intentAction == MessageCode.PARSED_DATA_FREQ_RESP){
                float gain = intent.getFloatExtra(MessageCode.VALUE,0f);
                int frequency = intent.getIntExtra(MessageCode.RANGE,0);//freq in Hz
            }else{

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frequency_response);
        /*setting up the log linear chart*/
        logchart = (LineChart) findViewById(R.id.freqRespGraph);
        logchart.getXAxis().setValueFormatter(new Log10AxisValueFormatter());
        /*Registering all message types so that application can send switch mode packet if the
        * wrong packet type is received*/
        IntentFilter filter = new IntentFilter(MessageCode.PARSED_DATA_DC_VOLTAGE);
        filter.addAction(MessageCode.PARSED_DATA_DC_CURRENT);
        filter.addAction(MessageCode.PARSED_DATA_RESISTANCE);
        filter.addAction(MessageCode.PARSED_DATA_FREQ_RESP);
        registerReceiver(broadcastReceiver,filter);

    }

    private List<Entry> entry_list = new ArrayList<>();
    public void onClickBeginDataAquisition(View view){
        /*Sending message to DMM to tell it to start the freq response, the format is:
        * <m:int;start:int;end:int;steps:int>
        *   m: the mode to switch to (4 in this case)
        *   start: start frequency in Hz
        *   end: end frequency in Hz
        *   steps: number of steps to take to reach the end frequency */
        Intent change_mode = new Intent(MessageCode.DMM_CHANGE_MODE_REQUEST);
        change_mode.putExtra(MessageCode.MODE,MessageCode.FREQ_RESP_MODE);
        //change_mode.putExtra(MessageCode.FREQ_RESP_START_FREQ,)

        sendBroadcast(change_mode);

        /*DEBUG START*/
        for(int i = 0; i<30;i++){
            float yval = (float)(-i*0.13);
            double xval = Math.pow(10,i);
            Entry e =new Entry((float)Math.log10(xval),yval);
            entry_list.add(e);
        }
        /*DEBUG END*/
    }

    private void updateChart(){
        LineDataSet dataSet = new LineDataSet(entry_list, "Gain"); // add entries to dataset
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        LineData lineData = new LineData(dataSet);
        logchart.setData(lineData);
        logchart.invalidate(); // refresh
        logchart.notifyDataSetChanged();
    }

}
