package com.mdp.mdpcontroller;


import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

public class BluetoothService {

    private final BluetoothAdapter bluetoothAdapter;

    public BluetoothService() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean isBluetoothSupported() {
        return bluetoothAdapter != null;
    }

    public boolean isBluetoothEnabled() {
        return isBluetoothSupported() && bluetoothAdapter.isEnabled();
    }

    @SuppressWarnings("MissingPermission")
    public void enableBluetooth(Context context) {
        if (isBluetoothSupported() && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(enableBtIntent);
        }
    }
}