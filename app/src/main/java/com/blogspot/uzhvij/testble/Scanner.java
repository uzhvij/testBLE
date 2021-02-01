package com.blogspot.uzhvij.testble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

class Scanner {
    //if 0L - onScanResult, if > 0 - onBatchScanResults
    public static final long SCANNING_TIME = 0L;
    private static final String TAG = "myLogs";
    private final ScanCallback scanCallback;
    private final BluetoothLeScanner scanner;
    private final ScanSettings scanSettings;
    private final List<ScanFilter> filters = null;
    private final InterfaceUpdater interfaceUpdater;
    private final LinkedHashSet<String> resultsSet;
    final LinkedHashSet<BluetoothDevice> devicesSet;

    Scanner(InterfaceUpdater interfaceUpdater) {
        this.interfaceUpdater = interfaceUpdater;
        resultsSet = new LinkedHashSet<>();
        devicesSet = new LinkedHashSet<>();
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        scanner = adapter.getBluetoothLeScanner();
        scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                .setReportDelay(SCANNING_TIME)
                .build();

        scanCallback = new ScanCallback() {
            //works every time when single ble device getting from scanning
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                if (parseResult(result)) {
                    showScanResult();
                }
            }
            //works once - when SCANNING_TIME is over, show all getting ble devices
            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                if (!results.isEmpty()) {
                    for (ScanResult result : results) {
                        parseResult(result);
                    }
                }
                showScanResult();
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void scan() {
        resultsSet.clear();
        scanner.startScan(filters, scanSettings, scanCallback);
    }

    public void stopScan() {
        scanner.stopScan(scanCallback);
    }

    public void showScanResult() {
        interfaceUpdater.updateInterface(new ArrayList<>(resultsSet), new ArrayList<>(devicesSet));
    }

    boolean parseResult(ScanResult result) {
        BluetoothDevice bluetoothDevice;
        ScanRecord scanRecord;
        bluetoothDevice = result.getDevice();
        scanRecord = result.getScanRecord();
        /*Log.d(TAG, "parseResult: \n" + bluetoothDevice.toString() + "\n" + scanRecord.toString() + "\n" +
                bluetoothDevice.getType() + " " + bluetoothDevice.getAlias() + " " +
                bluetoothDevice.getName() + " " + bluetoothDevice.describeContents() + " " +
                bluetoothDevice.getBluetoothClass() + " " + bluetoothDevice.getBondState() + " " +
                Arrays.toString(bluetoothDevice.getUuids()) + "\n" +
                scanRecord.getDeviceName() + " " + scanRecord.getAdvertiseFlags() + " " +
                Arrays.toString(scanRecord.getBytes()) + " " + scanRecord.getManufacturerSpecificData() + " " +
                scanRecord.getServiceData() + " " + scanRecord.getServiceSolicitationUuids() + " " +
                scanRecord.getServiceUuids() + " " + scanRecord.getTxPowerLevel());*/
        return devicesSet.add(bluetoothDevice) &
                resultsSet.add(bluetoothDevice.getAddress() + " " + scanRecord.getDeviceName());
    }
}
