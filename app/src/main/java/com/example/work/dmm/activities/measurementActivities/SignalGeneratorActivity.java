package com.example.work.dmm.activities.measurementActivities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.work.dmm.R;

public class SignalGeneratorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signal_generator);
        getSupportActionBar().setTitle("Signal Generator mode");
    }
}
