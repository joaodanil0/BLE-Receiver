package com.example.ble;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    Button startButton;
    Button stopButton;
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    private final int REQUEST_PERMISSION_BLUETOOTH_CONNECT = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permission_granted();
        initializeBluetooth();
        isBluetoothSupported();
        isBluetoothActivated();

        initializeViews();
        setButtonClickListener();
    }

    private void permission_granted(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,  new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},  1);
        }
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (Objects.requireNonNull(result.getScanRecord()).getDeviceName() != null && Objects.equals(result.getScanRecord().getDeviceName(), "TEST1")){
                Log.i("xxx", result.getScanRecord().getDeviceName() + " | " + result.getRssi() +  " | " + result.getDevice().getAddress());
            }
        }
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.i("xxx", results.toString());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    private void isBluetoothActivated() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION_BLUETOOTH_CONNECT);
                return;
            };
            resultLauncher().launch(enableBtIntent);
        }
    }
    
    private void initializeViews() {
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
    }

    private void initializeBluetooth(){
        bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    private void isBluetoothSupported(){
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setButtonClickListener() {
        startButton.setOnClickListener(v -> {

            List<ScanFilter> filters = new ArrayList<>();
            ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, 2);
                return;
            }
            bluetoothLeScanner.startScan(filters, settings, mLeScanCallback);
            Toast.makeText(this, "startScan", Toast.LENGTH_SHORT).show();
        });

        stopButton.setOnClickListener(v -> {
            // your handler code here
            bluetoothLeScanner.stopScan(mLeScanCallback);
            Toast.makeText(this, "stopScan", Toast.LENGTH_SHORT).show();
        });
    }



    private ActivityResultLauncher<Intent> resultLauncher(){

        ActivityResultLauncher<Intent> registerResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                    } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                        finish();
                        Toast.makeText(MainActivity.this, "BLE most to be activated", Toast.LENGTH_LONG).show();
                    }
                });

        return registerResult;
    }
}