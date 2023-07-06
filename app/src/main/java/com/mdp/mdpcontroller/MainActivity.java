package com.mdp.mdpcontroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

import java.util.ArrayList;
public class MainActivity extends AppCompatActivity{

    private ListView pairedDevicesListView;
    private ListView availableDevicesListView;
    private BluetoothClient bluetoothClient;

   private TextView receiveTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Request permission for location
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                PackageManager.PERMISSION_GRANTED);

        // Enable Bluetooth
        BluetoothService bluetoothService = new BluetoothService();
        BluetoothDiscoveryService bluetoothDiscoveryService = new BluetoothDiscoveryService(this);
        if (bluetoothService.isBluetoothSupported()) {
            bluetoothService.enableBluetooth(this);
        }

        // To get initial paired device list
        pairedDevicesListView = findViewById(R.id.pairedDevice);
        getPairDevice();


        // Scan for bluetooth devices button
        Button scanButton = findViewById(R.id.searchBluetoothDevice);
        scanButton.setOnClickListener(v -> scanNearbyDevice());


        // Event Listener for choosing devices in the available list
        availableDevicesListView = findViewById(R.id.availableDevice);
        availableDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String deviceInfo = (String) parent.getItemAtPosition(position);
                bluetoothDiscoveryService.stopDiscovery();
                showPairingDialog(deviceInfo);
            }
        });

        // Event Listener for choosing devices in the paired list

        pairedDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String deviceInfo = (String) parent.getItemAtPosition(position);
                showConnectDialog(deviceInfo);
            }
        });

        // Event Listener to send text from transmit data's text view
        // to the connected bluetooth device
        TextView transmitTextView = (TextView) findViewById(R.id.transmitTextView);
        Button transmitTextBtn = (Button) findViewById(R.id.transmitTextBtn);
        transmitTextBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
               String transmitText =  transmitTextView.getText().toString();
                bluetoothClient.sendData(transmitText);
                transmitTextView.setText("");

            }
        });


        // Initialise the receive data's text view
        receiveTextView = findViewById(R.id.receiveTextView);
        receiveTextView.setMovementMethod(new ScrollingMovementMethod());


        Button setUpConfig = (Button) findViewById(R.id.setupConfiguration);

        setUpConfig.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(MainActivity.this, ArenaActivity.class));
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(pairingBroadcastReceiver,
                new IntentFilter("PAIRING_SUCCESSFUL"));
    }
        private BroadcastReceiver pairingBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("PAIRING_SUCCESSFUL")) {
                    getPairDevice();
                }
            }
        };


    public void getPairDevice(){

        BluetoothDiscoveryService bluetoothDiscoveryService = new BluetoothDiscoveryService(this);
        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, bluetoothDiscoveryService.getPairedDevicesList());
        pairedDevicesListView.setAdapter(pairedDevicesAdapter);
    }
    private void scanNearbyDevice() {
        BluetoothDiscoveryService bluetoothDiscoveryService = new BluetoothDiscoveryService(this);
        bluetoothDiscoveryService.startDiscovery();
        availableDevicesListView = findViewById(R.id.availableDevice);
        ArrayAdapter<String> availableDevicesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1);
        availableDevicesListView.setAdapter(availableDevicesAdapter);

        bluetoothDiscoveryService.setDiscoveryCallback(new BluetoothDiscoveryService.DiscoveryCallback() {
            @Override
            public void onDeviceFound(String deviceInfo) {
                availableDevicesAdapter.add(deviceInfo);
            }

            @Override
            public void onDiscoveryFinished() {
                if (availableDevicesAdapter.getCount() == 0) {
                    availableDevicesAdapter.add("No devices found");
//                    Toast.makeText(getApplicationContext(), "Error, please search again", Toast.LENGTH_SHORT).show();
                }
                bluetoothDiscoveryService.stopDiscovery();
//                   Toast.makeText(getApplicationContext(), "Search Complete", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // Dialog box when pairing to device for the first time
    // *Note that it will automatically be connected straight
    private void showPairingDialog(String deviceInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Pairing");
        builder.setMessage("Confirm pairing with " + deviceInfo + "?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String deviceAddress = deviceInfo.split("\n")[1];
                BluetoothDiscoveryService bluetoothDiscoveryService = new BluetoothDiscoveryService(MainActivity.this);
                bluetoothDiscoveryService.pairDevice(deviceAddress);

              // Remove the device from the available devices list
                ArrayAdapter<String> availableDevicesAdapter =
                        (ArrayAdapter<String>)availableDevicesListView.getAdapter();
                availableDevicesAdapter.remove(deviceInfo);

                getPairDevice();
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
                connectToBluetoothDevice(deviceAddress);
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

    private void connectToBluetoothDevice(String deviceAddress) {
        bluetoothClient = new BluetoothClient(deviceAddress, this, new BluetoothClient.BluetoothCallback() {
            @Override
            public void onBluetoothDataReceived(final String data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // This code will run on the UI thread
                        receiveTextView.append(data + "\n");

                    }
                });
            }
        });
        bluetoothClient.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(pairingBroadcastReceiver);
    }
}