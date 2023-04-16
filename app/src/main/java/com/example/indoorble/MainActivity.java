package com.example.indoorble;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner scanner;
    private ScanCallback scanCallback;
    List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    List<Integer> rssiArray = new ArrayList<>();

    private TextView resultsView;
    //List<String> addressList = new ArrayList<>();

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button scanBtn = findViewById(R.id.scanBtn);
        this.resultsView = findViewById(R.id.resultsView);

        this.bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();

        // Check if BT adapter is enable
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //noinspection deprecation
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
        }

        // Create scanner
        this.scanner = bluetoothAdapter.getBluetoothLeScanner();

        // onClick scanBtn
        scanBtn.setOnClickListener(v -> checkSelfPermissionsAndStartScan());
    }


    @SuppressLint("MissingPermission")
    protected void checkSelfPermissionsAndStartScan() {

        scanCallback = new ScanCallback() {

            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                bluetoothDeviceList.add(device);
                rssiArray.add(result.getRssi());

                Log.i("BLE Scan", "Found " + bluetoothDeviceList.size() + " devices");
                showResults(bluetoothDeviceList, rssiArray);
            }

            @Override
            public void onScanFailed(int errorCode) {
                Toast.makeText(MainActivity.this, "Scan failed", Toast.LENGTH_SHORT).show();
                Log.e("BLE Scan", "Scan failed with error code " + errorCode);
            }
        };

        Log.d("BLE", "tryStartScan");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            Log.d("BLE", "Requesting Permissions");

            // Request permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_ADMIN,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                        }, 123);
            return;
        }

        // Start scan
        Log.d("BLE", "Start scan");
        Toast.makeText(MainActivity.this, "Scanning", Toast.LENGTH_SHORT).show();
        scanner.startScan(scanCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("BLE", "onDestroy");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
    }

    private void showResults(List<BluetoothDevice> bluetoothDeviceList, List<Integer> rssiArray) {
        this.resultsView.setMovementMethod(new ScrollingMovementMethod());
        Log.d("BLE", "showResults");

        StringBuilder sb = new StringBuilder();
        sb.append("Scanning... ").append(bluetoothDeviceList.size()).append("\n");

        for (int i = 0; i < bluetoothDeviceList.size(); i++) {
            BluetoothDevice device = bluetoothDeviceList.get(i);

            if (device.getAddress().equals("EE:43:BD:AC:6E:9D") ||
                    device.getAddress().equals("EE:97:40:40:C9:BF") ||
                    device.getAddress().equals("D6:B3:B0:46:42:F4") ||
                    device.getAddress().equals("F9:44:A2:17:50:CF") ||
                    device.getAddress().equals("D0:F0:18:78:0D:0E") ||
                    device.getAddress().equals("D0:F0:18:78:0B:C7") ||
                    device.getAddress().equals("D0:F0:18:78:0B:C9") ||
                    device.getAddress().equals("D0:F0:18:78:0D:10")) {

                String name = setBeaconName(device.getAddress());

                this.resultsView.setText("");
                sb.append("\n Device name: ").append(name);
                sb.append("\n MAC: ").append(device.getAddress());
                sb.append("\n level (RSSI): ").append(rssiArray.get(i)).append(" dBm\n");
            }
        }
        this.resultsView.setText(sb.toString());

        if (bluetoothDeviceList.size() > 99) {
            clearAndStopScanning();
        }
    }

    private String setBeaconName(String address) {
        switch (address) {
            case "EE:43:BD:AC:6E:9D":
                return "Mint Cocktail";
            case "EE:97:40:40:C9:BF":
                return "Coconut Puff";
            case "D6:B3:B0:46:42:F4":
                return "Icy Marshmallow";
            case "F9:44:A2:17:50:CF":
                return "Blueberry Pie";
            case "D0:F0:18:78:0D:0E":
                return "iNode:780D0E";
            case "D0:F0:18:78:0B:C7":
                return "BeaconID:780BC7";
            case "D0:F0:18:78:0B:C9":
                return "BeaconID:780BC9";
            case "D0:F0:18:78:0D:10":
                return "iNode:780D10";
        }
        return address;
    }

    @SuppressLint("MissingPermission")
    private void clearAndStopScanning() {
        this.bluetoothDeviceList.clear();
        this.rssiArray.clear();
        this.scanner.stopScan(scanCallback);
        Log.e("BLE", "Scan stop");
    }
}


