package com.example.work.dmm;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dcvoltage);

        //Voltage Gauge
        ColorArcProgressBar voltageGauge = (ColorArcProgressBar) findViewById(R.id.voltageGauge);
        voltageGauge.setDiameter(200);
        voltageGauge.setCurrentValues(100);

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
        if(isLogging) {
            genChart();
            loggingLineChart.setVisibility(View.VISIBLE);
        } else{
            loggingLineChart.setVisibility(View.INVISIBLE);
        }

        isLogging = !isLogging;
    }

    private void genChart() {
        //Generate new dataset
        List<Entry> entries = dataGenerator();
        LineDataSet dataSet = new LineDataSet(entries, "Shitty Point Data"); // add entries to dataset
        dataSet.setColor(ColorTemplate.COLORFUL_COLORS[1]);
        dataSet.setValueTextColor(Color.BLACK);
        LineData lineData = new LineData(dataSet);

        loggingLineChart.setData(lineData);
        loggingLineChart.invalidate(); // refresh

        loggingLineChart.notifyDataSetChanged();//Causes redraw when we add data. I imagine we'll initiate
    }

    private List<Entry> dataGenerator(){
        Point[] pointArray = new Point[100];

        //Generate Garbage data
        for(int i = 0; i < pointArray.length; i++){
            pointArray[i] = new Point(i, randInt(0, 420));
        }

        List<Entry> entries = new ArrayList<Entry>();

        //Convert into entries for storage in chart
        for (Point data : pointArray) {
            // turn data into Entry objects
            entries.add(new Entry(data.x, data.y));
        }

        return entries;
    }

    public static int randInt(int min, int max) {
        // Usually this can be a field rather than a method variable
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}
