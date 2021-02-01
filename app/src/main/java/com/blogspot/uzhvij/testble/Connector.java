package com.blogspot.uzhvij.testble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import static android.bluetooth.BluetoothDevice.*;
import static android.bluetooth.BluetoothGatt.*;
import static android.content.Context.BIND_ADJUST_WITH_ACTIVITY;
import static android.content.Context.BLUETOOTH_SERVICE;

class Connector implements Serializable {

    private static final String TAG = "myLogs";
    private final BluetoothDevice device;
    private final Context context;
    private BluetoothGatt gatt = null;
    private final BluetoothGattCallback bluetoothGattCallback;
    private final InterfaceUpdater interfaceUpdater;
    private final Activity activity;
    private static final int DISCONNECT = 1;
    private static final int NULL = 0;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable discoverServicesRunnable;

    Connector(Context context, BluetoothDevice device) {
        this.device = device;
        this.context = context;
        interfaceUpdater = (InterfaceUpdater) context;
        activity = (Activity) context;

        bluetoothGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (status == GATT_SUCCESS) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        runAndDisplayOnUiThread(" connected", NULL);
                        int bondState = device.getBondState();
                        if (bondState == BOND_NONE || bondState == BOND_BONDED) {
                            int delayWhenBonded = 0;
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                                delayWhenBonded = 1500;
                            }
                            final int delay = bondState == BOND_BONDED ? delayWhenBonded : 0;
                            discoverServicesRunnable = () -> {
                                runAndDisplayOnUiThread(String.format(Locale.ENGLISH,
                                        " discovering services of '%s' with delay of %d ms", device.getAddress(), delay), NULL);
                                if (!gatt.discoverServices()) {
                                    runAndDisplayOnUiThread(" discoverServices failed to start", NULL);
                                } else {
                                    runAndDisplayOnUiThread(" discoverServices success to start", NULL);
                                }
                                discoverServicesRunnable = null;
                            };
                            mainHandler.postDelayed(discoverServicesRunnable, delay);
                        } else if (bondState == BOND_BONDING) {
                            runAndDisplayOnUiThread(" waiting for bonding to complete", NULL);
                            new Handler(Looper.myLooper()).postDelayed(() -> connect(), 150);
                        }
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        runAndDisplayOnUiThread(" onConnectionStateChange: STATE_DISCONNECTED", NULL);
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                        runAndDisplayOnUiThread(" onConnectionStateChange: newState STATE_DISCONNECTING " + newState, NULL);
                    } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                        runAndDisplayOnUiThread(" onConnectionStateChange: newState STATE_CONNECTING " + newState, NULL);
                    }
                } else {
                    runAndDisplayOnUiThread(" onConnectionStateChange: failed status " + status, DISCONNECT);
                }
            }
        };
    }


    void connect() {
        interfaceUpdater.updateInterface(device.getAddress() + " connecting...");
        gatt = device.connectGatt(context, false, bluetoothGattCallback, TRANSPORT_LE);
    }

    void disconnect() {
        interfaceUpdater.updateInterface(device.getAddress() + " disconnecting");
        BluetoothManager manager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();
        int deviceState = manager.getConnectionState(device, GATT);
        if (deviceState == BluetoothProfile.STATE_CONNECTED) {
            Log.d(TAG, "disconnect: STATE_CONNECTED");
            if (gatt != null) gatt.disconnect();
            new Handler(Looper.myLooper()).postDelayed(this::closeConnection, 150);
        } else if (deviceState == BluetoothProfile.STATE_CONNECTING) {
            Log.d(TAG, "disconnect: STATE_CONNECTING");
            new Handler(Looper.myLooper()).postDelayed(this::disconnect, 150);
        } else if (deviceState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.d(TAG, "disconnect: STATE_DISCONNECTED");
            closeConnection();
        } else if (deviceState == BluetoothProfile.STATE_DISCONNECTING) {
            Log.d(TAG, "disconnect: STATE_DISCONNECTED");
            new Handler(Looper.myLooper()).postDelayed(this::closeConnection, 150);
        }
    }

    void closeConnection() {
        if (gatt != null) {
            interfaceUpdater.updateInterface(device.getAddress() + " connection close");
            gatt.close();
            gatt = null;
        } else
            interfaceUpdater.updateInterface(device.getAddress() + " connection was closed before");

    }

    void runAndDisplayOnUiThread(String data, int flag) {
        activity.runOnUiThread(() -> {
            interfaceUpdater.updateInterface(device.getAddress() + data);
        if (flag == DISCONNECT) disconnect();
        });
    }

    //if device was switched off or bluetooth was switched off - cache is deleted and you need do rescan for connecting to device
    boolean isDeviceCached(BluetoothDevice device) {
        return device.getType() != DEVICE_TYPE_UNKNOWN;
    }
}
