package com.example.ble;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button startButton;
    Button stopButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setButtonClickListener();

    }

    private void initializeViews(){
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
    }

    private void setButtonClickListener(){
        startButton.setOnClickListener(v -> {
            // your handler code here
            Toast.makeText(this, "start button", Toast.LENGTH_SHORT).show();
        });

        stopButton.setOnClickListener(v -> {
            // your handler code here
            Toast.makeText(this, "stop button", Toast.LENGTH_SHORT).show();
        });
    }
}