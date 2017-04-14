package com.example.work.dmm.displayAndVisualisationClasses;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.Locale;

public class Log10AxisValueFormatter implements IAxisValueFormatter {
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        String formated = String.format(Locale.ENGLISH, "%.2E", Math.pow(10,value));
        return formated;
    }
}
