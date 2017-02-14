package com.example.work.dmm;

import android.graphics.Color;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class DCvoltageActivity extends AppCompatActivity {
    private LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dcvoltage);
        chart = (LineChart) findViewById(R.id.chart);
        genChart();
    }

    private void genChart(){
        Point[] dataObjects = {new Point(0,1), new Point(1,2), new Point(2,3)};

        List<Entry> entries = new ArrayList<Entry>();

        for (Point data : dataObjects) {
            // turn data into Entry objects
            entries.add(new Entry(data.x, data.y));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Shitty Point Data"); // add entries to dataset
        dataSet.setColor(ColorTemplate.COLORFUL_COLORS[1]);
        dataSet.setValueTextColor(Color.BLACK);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // refresh
    }
}
