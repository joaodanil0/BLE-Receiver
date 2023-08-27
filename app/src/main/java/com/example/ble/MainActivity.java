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
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
    ArrayList<String> rssi_list = new ArrayList<>();

    String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportActionBar().hide();

        permission_granted();

        rssi_list.add("rssi_value, packet count ,timestamp\n");

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
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,  new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},  1);
        }
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            if (Objects.requireNonNull(result.getScanRecord()).getDeviceName() != null && Objects.equals(result.getScanRecord().getDeviceName(), "TEST1")){
                Long tsLong = System.currentTimeMillis();
                String ts = tsLong.toString();

                byte[] mScanRecord = result.getScanRecord().getBytes();
                final StringBuilder stringBuilder = new StringBuilder(mScanRecord.length);
                for (byte byteChar : mScanRecord) {
                    stringBuilder.append((char) byteChar);
                }

                String advData = stringBuilder.toString();


                Log.i("xxx",result.getScanRecord().getDeviceName() + " | " +
                                      result.getRssi() + " | " +
                                      advData.substring(11, 20) + " | " +
//                                      advData + " | " +
                                      ts + " | "
                            );
                rssi_list.add(result.getRssi() + ", " + advData.substring(11, 20) + ", " + ts + '\n');
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
            Log.i("xxx", "Print filter size" + rssi_list.size());

            a();
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

    private void writeToFile() {

        File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        root = new File(root , fileName );
        try {
            FileOutputStream fout = new FileOutputStream(root);
            for(String elem : rssi_list){
//                Log.i("xxx", elem+"  ");
                fout.write(elem.getBytes());
            }
            fout.close();
            rssi_list.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void a() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("File name");

        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", (dialog, whichButton) -> {

            // Do something with value!
            fileName = input.getText().toString() + ".csv";
            writeToFile();


        });

        alert.setNegativeButton("Cancel", (dialog, whichButton) -> {
            // Canceled.

        });

        alert.show();
    }
}