package com.example.work.dmm;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frequency_response);
        logchart = (LineChart) findViewById(R.id.freqRespGraph);
        logchart.getXAxis().setValueFormatter(new Log10AxisValueFormatter());
    }

    public void onClickBeginDataAquisition(View view){
        List<Entry> entry_list = new ArrayList<>();
        for(int i = 0; i<10;i++){
            float yval = -i;
            double xval = Math.pow(10,i);
            Entry e =new Entry((float)Math.log10(xval),yval);
            entry_list.add(e);
        }
        LineDataSet dataSet = new LineDataSet(entry_list, "Voltage"); // add entries to dataset
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        LineData lineData = new LineData(dataSet);
        logchart.setData(lineData);
        logchart.invalidate(); // refresh
        logchart.notifyDataSetChanged();//Causes redraw when we add data. I imagine we'll initiate
    }

}
