package com.example.work.dmm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.numetriclabz.numandroidcharts.LogarithmicLineChart;

public class FrequencyResponseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frequency_response);
        LogarithmicLineChart logchart = (LogarithmicLineChart) findViewById(R.id.logLineChart);
        logchart.setBase(10);

    }
}
