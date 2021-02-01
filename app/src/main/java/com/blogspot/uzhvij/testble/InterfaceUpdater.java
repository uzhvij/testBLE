package com.blogspot.uzhvij.testble;

import android.bluetooth.BluetoothDevice;

import java.util.List;

interface InterfaceUpdater {
    default void updateInterface(List<String> list, List<BluetoothDevice> devices){
        System.out.println("InterfaceUpdater.updateInterface(List<String> list, List<BluetoothDevice> devices) is empty");
    };
    default void updateInterface(String data){
        System.out.println("InterfaceUpdater.updateInterface(String data) is empty");
    };
}
