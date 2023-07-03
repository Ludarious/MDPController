package com.mdp.mdpcontroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.content.Context;

public class MainActivity extends AppCompatActivity{

    private ListView pairedDeviceListView;
    private ListView availableDeviceListView;
    private TextView availableDeviceLabel;
    private TextView pairedDeviceLabel;

    private TextView transmitTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Add underline to label
        availableDeviceLabel = (TextView)findViewById(R.id.availableDeviceLabel);
        availableDeviceLabel.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        pairedDeviceLabel = (TextView)findViewById(R.id.pairedDeviceLabel);
        pairedDeviceLabel.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        // Retrieve Listview
        availableDeviceListView = findViewById(R.id.availableDevice);
        pairedDeviceListView = findViewById(R.id.pairedDevice);

        // Text View for transmission
        transmitTextView= (TextView) findViewById(R.id.transmitTextView);
        transmitTextView.setMovementMethod(new ScrollingMovementMethod());


        Button btn = (Button)findViewById(R.id.setupArena);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ArenaActivity.class));
            }
        });

        //Request permission for location
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                PackageManager.PERMISSION_GRANTED);
//        Toast.makeText(this, "Amount can not be grater than invoice",
//                Toast.LENGTH_SHORT).show();

        // Enable Bluetooth
        BluetoothService bluetoothService = new BluetoothService();
        BluetoothDiscoveryService bluetoothDiscoveryService = new BluetoothDiscoveryService(this);
        if (bluetoothService.isBluetoothSupported()) {
            bluetoothService.enableBluetooth(this);
        }

        //Get Paired Devices List
        getPairDevice();


        // Scan for bluetooth devices button
        Button scanButton = findViewById(R.id.searchBluetoothDevice);
        //scanButton.setOnClickListener(this);
        scanButton.setOnClickListener(v -> scanNearbyDevice());


        pairedDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String deviceInfo = (String) parent.getItemAtPosition(position);
                showConnectDialog(deviceInfo);
            }
        });

        availableDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String deviceInfo = (String) parent.getItemAtPosition(position);
                bluetoothDiscoveryService.stopDiscovery();
                showPairingDialog(deviceInfo);
            }


        });
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        // Pairing was successful, update paired devices list
                        getPairDevice();
                    }
                }, new IntentFilter("PAIRING_SUCCESSFUL"));


    }


    public void getPairDevice(){

        BluetoothDiscoveryService bluetoothDiscoveryService = new BluetoothDiscoveryService(this);
        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, bluetoothDiscoveryService.getPairedDevicesList());
        pairedDeviceListView.setAdapter(pairedDevicesAdapter);
    }
    private void scanNearbyDevice() {
        BluetoothDiscoveryService bluetoothDiscoveryService = new BluetoothDiscoveryService(this);
        bluetoothDiscoveryService.startDiscovery();
        availableDeviceListView = findViewById(R.id.availableDevice);
        ArrayAdapter<String> availableDevicesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1);
        availableDeviceListView.setAdapter(availableDevicesAdapter);

        bluetoothDiscoveryService.setDiscoveryCallback(new BluetoothDiscoveryService.DiscoveryCallback() {
            @Override
            public void onDeviceFound(String deviceInfo) {
                availableDevicesAdapter.add(deviceInfo);
            }

            @Override
            public void onDiscoveryFinished() {
                if (availableDevicesAdapter.getCount() == 0) {
                    availableDevicesAdapter.add("No devices found");
                }
                bluetoothDiscoveryService.stopDiscovery();
                     //   Toast.makeText(getApplicationContext(), "Search Complete", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showPairingDialog(String deviceInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Pairing");
        builder.setMessage("Confirm pairing with " + deviceInfo + "?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String deviceAddress = deviceInfo.split("\n")[1];
                BluetoothDiscoveryService bluetoothDiscoveryService = new BluetoothDiscoveryService(MainActivity.this);

                // Pair the device first
                bluetoothDiscoveryService.pairDevice(deviceAddress);
                getPairDevice();

//                // Connect to the device
//                connectToPairedDevice(deviceAddress);
                dialog.dismiss();


            }
        });


        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showConnectDialog(String deviceInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Connect Bluetooth Device");
        builder.setMessage("Confirm Connect with " + deviceInfo + "?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String deviceAddress = deviceInfo.split("\n")[1];
                BluetoothDiscoveryService bluetoothDiscoveryService = new BluetoothDiscoveryService(MainActivity.this);

                connectToPairedDevice(deviceAddress);
                dialog.dismiss();


            }
        });


        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void connectToPairedDevice(String deviceAddress) {
        BluetoothDiscoveryService bluetoothDiscoveryService = new BluetoothDiscoveryService(this);
       bluetoothDiscoveryService.pairDevice(deviceAddress);
        bluetoothDiscoveryService.connectToDevice(deviceAddress);
    }

}
