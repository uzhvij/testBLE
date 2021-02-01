package com.blogspot.uzhvij.testble;

import android.bluetooth.BluetoothDevice;

import java.util.List;

interface InterfaceUpdater {
    void updateInterface(List<String> list, List<BluetoothDevice> devices);
}
