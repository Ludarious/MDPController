package com.mdp.mdpcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.Manifest;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.util.ArrayList;
import java.util.Set;


public class BluetoothDiscoveryService {
    private final BluetoothAdapter bluetoothAdapter;
    private final Context context;
    private ArrayList<String> newDevicesList = new ArrayList<>();
    private ArrayList<String> pairedDevicesList = new ArrayList<>();
    private DiscoveryCallback callback;


    public BluetoothDiscoveryService(Context context) {
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        registerReceivers();
    }

    @SuppressWarnings("MissingPermission")
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Check if it's already paired
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    String deviceInfo = device.getName() + "\n" + device.getAddress();
                    newDevicesList.add(deviceInfo);
                    if (callback != null) {
                        callback.onDeviceFound(deviceInfo);
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // Done searching
                if (newDevicesList.size() == 0) {
                    newDevicesList.add("No devices found");
                }
                if (callback != null) {
                    callback.onDiscoveryFinished();
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                // Pairing state has changed
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                if (state == BluetoothDevice.BOND_BONDED) {
                    // A new device has been paired
                    Intent localIntent = new Intent("PAIRING_SUCCESSFUL");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
                }
            }

        }
    };


    private void registerReceivers() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(broadcastReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(broadcastReceiver, filter);

        filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(broadcastReceiver, filter);
    }

    @SuppressWarnings("MissingPermission")
    public ArrayList<String> getPairedDevicesList() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesList.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            pairedDevicesList.add("No paired devices");
        }
        return pairedDevicesList;
    }

    public void startDiscovery() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permissions are not granted
            return;
        }

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }



    @SuppressWarnings("MissingPermission")
    public void stopDiscovery() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    public interface DiscoveryCallback {
        void onDeviceFound(String deviceInfo);
        void onDiscoveryFinished();
    }

    public void setDiscoveryCallback(DiscoveryCallback callback) {
        this.callback = callback;
    }

    @SuppressWarnings("MissingPermission")
    public void pairDevice(String deviceAddress) {
        if (BluetoothAdapter.checkBluetoothAddress(deviceAddress)) {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            if (device != null) {
                device.createBond();
            }

        }
    }


}