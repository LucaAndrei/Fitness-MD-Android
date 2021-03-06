package com.master.aluca.fitnessmd.bluetooth;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


import com.master.aluca.fitnessmd.Constants;
import com.master.aluca.fitnessmd.util.AlertMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by aluca on 11/7/16.
 */
public class BluetoothManager {

    public static final String LOG_TAG = "Fitness_BTManager";

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private int mState;

    private CommunicationWithDevice mCommuncationWithDevice;

    // Name for the SDP record when creating server socket
    private static final String NAME = "BluetoothManager";

    // Unique UUID for this application
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private ProgressDialog progress;
    private Context mContext;
    private BluetoothDevice mBluetoothDevice;

    private AsyncTask<Void,Void,Void> pairWithDevice;


    /**
     * Constructor. Prepares a new BluetoothManager session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothManager(Context context, Handler handler) {
        Log.d(LOG_TAG, "BluetoothManager constructor");
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = BluetoothState.NOT_CONNECTED;
        mHandler = handler;
        mContext = context;
    }

    /**
     * Set the current state of the connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setConnectionState(int state) {
        String previousState = "null";
        String newState = "null";
        if (mState == BluetoothState.NOT_CONNECTED) {
            previousState = "NOT_CONNECTED";
        } else if (mState == BluetoothState.CONNECTED) {
            previousState = "CONNECTED";
        } else if (mState == BluetoothState.CONNECTING) {
            previousState = "CONNECTING";
        }
        if (state == BluetoothState.NOT_CONNECTED) {
            newState = "NOT_CONNECTED";
        } else if (state == BluetoothState.CONNECTED) {
            newState = "CONNECTED";
        } else if (state == BluetoothState.CONNECTING) {
            newState = "CONNECTING";
        }
        Log.d(LOG_TAG, "setConnectionState() " + previousState + " -> " + newState);
        mState = state;
        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(BluetoothMessageType.CONNECTION_STATE, state, -1).sendToTarget();
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     */
    public synchronized void initiateCommunicationWithDevice(BluetoothSocket socket) {
        Log.d(LOG_TAG, "initiateCommunicationWithDevice");

        // Cancel the thread that completed the connection
        if (pairWithDevice != null) {
            pairWithDevice.cancel(true);
            pairWithDevice = null;
        }
        // Cancel any thread currently running a connection
        if (mCommuncationWithDevice != null) {
            mCommuncationWithDevice.cancel();
            mCommuncationWithDevice = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mCommuncationWithDevice = new CommunicationWithDevice(socket);
        mCommuncationWithDevice.start();

        // Send the name of the connected device back to the UI Activity
        Intent intent = new Intent(Constants.CONNECTED_DEVICE_DETAILS_INTENT);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.CONNECTED_DEVICE_ADDRESS_BUNDLE_KEY, mBluetoothDevice.getAddress());
        bundle.putString(Constants.CONNECTED_DEVICE_NAME_BUNDLE_KEY, mBluetoothDevice.getName());
        intent.putExtras(bundle);
        mContext.sendBroadcast(intent);

        setConnectionState(BluetoothState.CONNECTED);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void pairDevice(BluetoothDevice device) {
        Log.d(LOG_TAG, "Connecting to: " + device);

        if (mState == BluetoothState.CONNECTED)
            return;

        // Cancel any thread attempting to make a connection
        if (mState == BluetoothState.CONNECTING) {
            if (pairWithDevice != null) {
                pairWithDevice.cancel(true);
                pairWithDevice = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mCommuncationWithDevice != null) {
            mCommuncationWithDevice.cancel();
            mCommuncationWithDevice = null;
        }

        mBluetoothDevice = device;

        pairWithDevice = new PairWithDeviceTask();
        pairWithDevice.execute();
        setConnectionState(BluetoothState.CONNECTING);
    }

    /**
     * Stop all threads
     */
    public synchronized void closeConnection() {
        Log.d(LOG_TAG, "stop");
        if (pairWithDevice != null) {
            pairWithDevice.cancel(false);
            pairWithDevice = null;
        }
        if (mCommuncationWithDevice != null) {
            mCommuncationWithDevice.cancel();
            mCommuncationWithDevice = null;
        }
        setConnectionState(BluetoothState.NOT_CONNECTED);
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    public class PairWithDeviceTask extends AsyncTask<Void, Void, Void> {
        private BluetoothSocket mmSocket;
        AlertMessage alertMessage = new AlertMessage();

        @Override
        protected void onPreExecute() {
            Log.d(LOG_TAG, "PairWithDeviceTask onPreExecute : " + mContext);

            //progress = ProgressDialog.show(mContext, "Connecting...", "Please wait!!!");  //show a progress dialog

            alertMessage.alertbox();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(LOG_TAG, "PairWithDeviceTask doInBackground");
            try {
                if(mmSocket == null || mState != BluetoothState.CONNECTED) {
                    Log.d(LOG_TAG, "PairWithDeviceTask create&connect socket");
                    mmSocket = mBluetoothDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                    mAdapter.cancelDiscovery();
                    mmSocket.connect();//start connection
                } else {
                    Log.d(LOG_TAG, "PairWithDeviceTask could not create socket");
                }
            } catch (IOException e) {
                Log.d(LOG_TAG, "PairWithDeviceTask catch exception");
                //Constants.displayToastMessage(mContext, "Unable to connect device"); -> http://stackoverflow.com/questions/16830255/how-to-display-toast-in-asynctask-in-android
                // Close the socket
                setConnectionState(BluetoothState.NOT_CONNECTED);
                try {
                    Log.d(LOG_TAG, "PairWithDeviceTask close socket");
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(LOG_TAG, "unable to close() socket during connection failure", e2);

                }
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);
            Log.d(LOG_TAG, "PairWithDeviceTask onPostExecute");
            if (mState == BluetoothState.NOT_CONNECTED) {
                Log.d(LOG_TAG, "PairWithDeviceTask call connectionFailed");
                Constants.displayToastMessage(mContext, "Device connection was lost");
            } else {
                Log.d(LOG_TAG, "PairWithDeviceTask initiate communication with device");
                // Start the connected thread
                initiateCommunicationWithDevice(mmSocket);
            }
            Log.d(LOG_TAG, "PairWithDeviceTask dismiss progress dialog");
            alertMessage.dismiss();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.d(LOG_TAG, "PairWithDeviceTask onCancelled");
            try {
                Log.d(LOG_TAG, "PairWithDeviceTask close socket");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "close() of connect socket failed", e);
            }
        }
    }	// End of class ConnectThread

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class CommunicationWithDevice extends Thread {
        private BluetoothSocket mmSocket;
        private InputStream mmInStream;

        public CommunicationWithDevice(BluetoothSocket socket) {
            Log.d(LOG_TAG, "CommunicationWithDevice constructor");
            mmSocket = socket;

            // Get the BluetoothSocket input and output streams
            try {
                Log.d(LOG_TAG, "CommunicationWithDevice get input/output stream");
                mmInStream = socket.getInputStream();
            } catch (IOException e) {
                Log.e(LOG_TAG, "CommunicationWithDevice temp sockets not created", e);
            }
        }

        public void run() {
            Log.d(LOG_TAG, "CommunicationWithDevice run");
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    byte[] buffer = new byte[8];
                    Arrays.fill(buffer, (byte)0x00);
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the main thread
                    mHandler.obtainMessage(BluetoothMessageType.READ, bytes, -1, buffer).sendToTarget();

                } catch (IOException e) {
                    Log.e(LOG_TAG, "CommunicationWithDevice disconnected - device connection lost", e);
                    Constants.displayToastMessage(mContext, "Device connection was lost");
                    Intent intent = new Intent(Constants.DEVICE_CONNECTION_LOST);
                    mContext.sendBroadcast(intent);
                    break;
                }
            }
        }
        /* 12-17 12:27:53.470 2246-2352/? D/lights: set_light_buttons: 2
                 12-17 12:27:54.360 28194-28235/? E/bt-btm: btm_sec_disconnected - Clearing Pending flag
         12-17 12:27:54.360 28194-28266/? W/bt-btif: invalid rfc slot id: 12
                 12-17 12:27:54.370 29731-29759/? E/Fitness_BTManager: CommunicationWithDevice disconnected - device connection lost
         java.io.IOException: bt socket closed, read return: -1
         at android.bluetooth.BluetoothSocket.read(BluetoothSocket.java:429)
         at android.bluetooth.BluetoothInputStream.read(BluetoothInputStream.java:96)
         at java.io.InputStream.read(InputStream.java:162)
         at com.master.aluca.fitnessmd.bluetooth.BluetoothManager$CommunicationWithDevice.run(BluetoothManager.java:283)
                 12-17 12:27:54.370 29731-29759/? W/dalvikvm: threadid=12: thread exiting with uncaught exception (group=0x417d4c08)
         12-17 12:27:54.385 29731-29759/? E/AndroidRuntime: FATAL EXCEPTION: Thread-809
         Process: com.master.aluca.fitnessmd, PID: 29731
         java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
         at android.os.Handler.<init>(Handler.java:200)
         at android.os.Handler.<init>(Handler.java:114)
         at android.widget.Toast$TN.<init>(Toast.java:327)
         at android.widget.Toast.<init>(Toast.java:92)
         at android.widget.Toast.makeText(Toast.java:241)
         at com.master.aluca.fitnessmd.Constants.displayToastMessage(Constants.java:120)
         at com.master.aluca.fitnessmd.bluetooth.BluetoothManager$CommunicationWithDevice.run(BluetoothManager.java:290)*/
        public void cancel() {
            Log.d(LOG_TAG, "CommunicationWithDevice cancel");
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "CommunicationWithDevice close() of connect socket failed");
            }
        }

    }	// End of class ConnectedThread

}
