package com.blogspot.uzhvij.testble;

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


    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable discoverServicesRunnable;

    Connector(Context context, BluetoothDevice device) {
        this.device = device;
        this.context = context;

        bluetoothGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (status == GATT_SUCCESS){
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        int bondState = device.getBondState();
                        if(bondState == BOND_NONE || bondState == BOND_BONDED){
                            int delayWhenBonded = 0;
                            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.N){
                                delayWhenBonded = 1500;
                            }
                            final int delay = bondState == BOND_BONDED ? delayWhenBonded : 0;
                            discoverServicesRunnable = () -> {
                                Log.d(TAG, String.format(Locale.ENGLISH,
                                        "discovering services of '%s' with delay of %d ms", device.getAddress(), delay));
                                if(!gatt.discoverServices()){
                                    Log.d(TAG, "discoverServices failed to start");
                                }else {
                                    Log.d(TAG, "discoverServices success to start");
                                }
                                discoverServicesRunnable = null;
                            };
                            mainHandler.postDelayed(discoverServicesRunnable, delay);
                        }else if (bondState == BOND_BONDING) {
                            Log.i(TAG, "waiting for bonding to complete");
                            new Handler(Looper.myLooper()).postDelayed(() -> connect(), 150);
                        }
                    }else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                        Log.d(TAG, "onConnectionStateChange: STATE_DISCONNECTED");
                    }else if(newState == BluetoothProfile.STATE_DISCONNECTING){
                        Log.d(TAG, "onConnectionStateChange: newState STATE_DISCONNECTING" + newState);
                    }else if(newState == BluetoothProfile.STATE_CONNECTING){
                        Log.d(TAG, "onConnectionStateChange: newState STATE_CONNECTING" + newState);
                    }
                }else {
                    Log.d(TAG, "onConnectionStateChange: failed status " + status);
                    disconnect();
                }
            }
        };
    }



    void connect(){
        gatt = device.connectGatt(context, false, bluetoothGattCallback, TRANSPORT_LE);
    }

    void disconnect(){
        Log.d(TAG, "disconnect: ");
        BluetoothManager manager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();
        int deviceState  = manager.getConnectionState(device, GATT);
        if(deviceState == BluetoothProfile.STATE_CONNECTED){
            Log.d(TAG, "disconnect: STATE_CONNECTED");
            if(gatt != null) gatt.disconnect();
            new Handler(Looper.myLooper()).postDelayed(this::closeConnection, 150);
        }else if(deviceState == BluetoothProfile.STATE_CONNECTING){
            Log.d(TAG, "disconnect: STATE_CONNECTING");
            new Handler(Looper.myLooper()).postDelayed(this::disconnect, 150);
        }else if(deviceState == BluetoothProfile.STATE_DISCONNECTED){
            Log.d(TAG, "disconnect: STATE_DISCONNECTED");
            closeConnection();
        }else if(deviceState == BluetoothProfile.STATE_DISCONNECTING){
            Log.d(TAG, "disconnect: STATE_DISCONNECTED");
            new Handler(Looper.myLooper()).postDelayed(this::closeConnection, 150);
        }
    }

    void closeConnection(){
        if(gatt != null){
            Log.d(TAG, "closeConnection: y");
            gatt.close();
            gatt = null;
        }else Log.d(TAG, "closeConnection: n");
    }

    //if device was switched off or bluetooth was switched off - cache is deleted and you need do rescan for connecting to device
    boolean isDeviceCached(BluetoothDevice device){
        return device.getType() != DEVICE_TYPE_UNKNOWN;
    }
}
