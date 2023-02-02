package com.example.wheele_commander.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wheele_commander.R;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SpeedometerView speedometer = findViewById(R.id.speedometer);
    }
}