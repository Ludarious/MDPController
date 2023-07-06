package com.mdp.mdpcontroller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class BluetoothClient extends Thread {

    private static final UUID APP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG = "BluetoothClient";
    private BluetoothSocket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private ProgressDialog progressDialog;
    private final BluetoothAdapter bluetoothAdapter;
    private Context context;
    private final String pairdeviceAddress;
    private String timeStamp;
    private BluetoothCallback callback;
    private static BluetoothClient bluetoothClientInstance = null;

    // Method to get the singleton instance of BluetoothClient
    public static synchronized BluetoothClient getInstance(String pairdeviceAddress, Context context, BluetoothCallback callback) {
        if (bluetoothClientInstance == null) {
            bluetoothClientInstance = new BluetoothClient(pairdeviceAddress, context, callback);
        }
        return bluetoothClientInstance;
    }

    private BluetoothClient(String pairdeviceAddress,Context context, BluetoothCallback callback) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(pairdeviceAddress);
        this.context = context;
        this.pairdeviceAddress = pairdeviceAddress;
        this.callback = callback;
        BluetoothSocket tmpSocket = null;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        // Get the current timestamp
        timeStamp = new SimpleDateFormat("dd/MM/yyyy HH.mm.ss").format(new Date());

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Connecting");
        progressDialog.setMessage("Please wait...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        try {
            tmpSocket = device.createRfcommSocketToServiceRecord(APP_UUID);
            tmpIn = tmpSocket.getInputStream();
            tmpOut = tmpSocket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }catch (SecurityException e) {
            Log.e(TAG, "Permission missing for createRfcommSocketToServiceRecord", e);
        }
        Log.i(TAG, "tmpSocket>>> "+ tmpSocket);
        socket = tmpSocket;
        inputStream = tmpIn;
        outputStream = tmpOut;
    }

    public void run() {
        int maxRetryCount = 3; // Maximum number of retries
        int retryCount = 0; // Current retry count
        boolean isConnected = false;

        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                progressDialog.show();
            }
        });

        while (!isConnected) {
            while (retryCount < maxRetryCount) {
                try {
                    // Adding a delay of 5 seconds before each connection attempt
//                    Thread.sleep(5000);
                    socket.connect();
                    // If the connection is successful, break from the loop
                    isConnected = true;
                    break;
                } catch (IOException connectException) {
                    retryCount++; // Increment the retry count
                    Log.e(TAG, "Could not connect the client socket. Attempt " + retryCount, connectException);
                    // If the maximum retry count is reached, clean up the resources and stop the thread
                    if (retryCount == maxRetryCount) {
                        cleanup();
                        return;
                    }
                }
//                catch (InterruptedException e) {
//                    Log.e(TAG, "Sleep interrupted", e);
//                    cleanup();
//                    return;
//                }
            catch (SecurityException e) {
                    Log.e(TAG, "Permission missing for socket connect", e);
                    cleanup();
                    return;
                }
            }

            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    progressDialog.dismiss(); // Dismiss the ProgressDialog
                    Toast.makeText(context, "Connection established", Toast.LENGTH_SHORT).show();
                }
            });

            Log.i(TAG, "socket >>> " + socket);

            if (isConnected) {
//                sendData("Test String: " + timeStamp);
                Log.d("isConnected", "here");
                receiveData();
            }
        }
    }


    public void sendData(String data) {
        Log.i(TAG, "calling sendData()...");
        if (outputStream != null) {
            try {
                outputStream.write(data.getBytes());
                Log.i(TAG, "Data sent: " + data);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
            }
        }
    }
    public void receiveData() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = inputStream.read(buffer);
                String received = new String(buffer, 0, bytes);
                // Call the callback method
                callback.onBluetoothDataReceived(received); // Trigger callback here

                Log.i(TAG, "Received data: " + received);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when receiving data", e);
                // Try to reconnect here
                Log.i(TAG, "Connection lost, attempting to reconnect...");
                reconnect();
            }
        }
    }
    @SuppressWarnings("MissingPermission")
    public void reconnect() {
        cleanup();
        int maxRetryCount = 2; // Maximum number of retries
        int retryCount = 0; // Current retry count

        while (retryCount < maxRetryCount) {
            try {
                Thread.sleep(0000); // delay 3 seconds before reconnection attempt
//                if (!BTUtils.checkBluetoothConnectionPermission(context)) {
//                    BTUtils.requestBluetoothPermissions((Activity) context);
//                }
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(pairdeviceAddress);
                Log.i(TAG,"Reconnect pairdeviceAddress: " + pairdeviceAddress);
                socket = device.createRfcommSocketToServiceRecord(APP_UUID);
                socket.connect();

                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        progressDialog.show();
                    }
                });

                if(socket.isConnected()) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(context, "Reconnected successfully", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();  //dismiss progressDialog here
                        }
                    });
                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();

                    // Update the timestamp here
                    timeStamp = new SimpleDateFormat("dd/MM/yyyy HH.mm.ss").format(new Date());

                    sendData("reconnected: " + timeStamp);
                    Log.i(TAG,"reconnected send data: " +timeStamp);
                }
                break;
            } catch (IOException e) {
                retryCount++;
                Log.e(TAG, "Couldn't reconnect on attempt " + retryCount, e);
                if (retryCount == maxRetryCount) {
                    // Handle the case when all retries fail here. For example, you can show a Toast message to inform the user.
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(context, "Reconnecting...", Toast.LENGTH_SHORT).show();
                                return;
//                            Toast.makeText(context, "Unable to reconnect after " + maxRetryCount + " attempts", Toast.LENGTH_SHORT).show();
//                            progressDialog.dismiss();  //dismiss progressDialog here
                        }
                    });
                    return; // Stop trying to reconnect
                }
            }
            catch (InterruptedException e) {
                Log.e(TAG, "Sleep interrupted", e);
            }
        }
    }

    public void stopClient() {
        cleanup();
    }

    private void cleanup() {

        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                progressDialog.dismiss(); // Dismiss the ProgressDialog
            }
        });

        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the input stream", e);
            }
        }

        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the output stream", e);
            }
        }

        if (socket != null) {
            try {
                socket.close();
                Log.i(TAG, "Client socket closed");

            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    public interface BluetoothCallback {
        void onBluetoothDataReceived(String data);
    }


}

