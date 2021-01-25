package com.blogspot.uzhvij.testble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

class Scanner {
    private BluetoothAdapter adapter;
    private final ScanCallback scanCallback;
    private BluetoothLeScanner scanner;
    private ScanSettings scanSettings;
    private List<ScanFilter> filters = null;
    private static final String TAG = "myLogs";
    private InterfaceUpdater interfaceUpdater;

    Scanner(InterfaceUpdater interfaceUpdater){
        Log.d(TAG, "Scanner: ");
        this.interfaceUpdater = interfaceUpdater;
        adapter = BluetoothAdapter.getDefaultAdapter();
        scanner = adapter.getBluetoothLeScanner();
        scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                .setReportDelay(0L)
                .build();
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                //super.onScanResult(callbackType, result);
                BluetoothDevice bluetoothDevice = result.getDevice();
                Log.d(TAG, "onScanResult: " + bluetoothDevice.toString());
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                //super.onBatchScanResults(results);
                ArrayList<String> list = new ArrayList<>();
                Log.d(TAG, "onBatchScanResults: " + results.size() + " " + results.isEmpty());
                if(!results.isEmpty()){
                    for (ScanResult result : results) {
                        list.add(printResult(result));
                    }
                }
                stopScan();
                interfaceUpdater.updateInterface(list);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(TAG, "onScanFailed: ");
            }
        };
    }

    public void scan(){
        Log.d(TAG, "scan: ");
        scanner.startScan(filters, scanSettings, scanCallback);
    }

    public void stopScan(){
        Log.d(TAG, "stopScan: ");
        scanner.stopScan(scanCallback);
    }

    String printResult(ScanResult result){
        BluetoothDevice bluetoothDevice;
        ScanRecord scanRecord;
        bluetoothDevice = result.getDevice();
        scanRecord = result.getScanRecord();
        String info = bluetoothDevice.getAddress() + " " + scanRecord.getDeviceName();
        Log.d(TAG, "onBatchScanResults: " + info);
        return info;
    }
}
